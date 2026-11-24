package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ConditionalGet
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateResult
import com.techtsunagu.wakatta.feature.machines.domain.PanelWorkerStatus

/**
 * Panel over `GET /machines/{id}/panel` with ETag and 304 (Foreman). A Worker may not read that route, so a
 * Worker's Panel is their own Lock or Ready from the Skill Passport. The "UNKNOWN after failed polls or old
 * data" safety rule lives in PanelViewModel, not here: this class only reports what the server said.
 */
internal class RealPanelRepository(
    private val api: () -> MachinesApi,
    private val session: SessionStore,
    private val workers: WorkerRepository,
    private val signOffs: SignOffRepository,
    private val cache: CacheStore,
) : PanelRepository {

    private val conditional = ConditionalGet(cache)

    private val user get() = session.session.value?.user

    override suspend fun getPanelState(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> =
        if (user?.role == Role.FOREMAN) foremanPanel(machineId, ifNoneMatch) else workerPanel(machineId, ifNoneMatch)

    private suspend fun foremanPanel(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> {
        val key = "panel:$machineId"
        // A first load or a refresh has no ETag: it must hear from the server, never from the stored body.
        if (ifNoneMatch == null) cache.evict(key)
        var notModified = false
        val panel = when (
            val result = conditional.get(key, PanelDto.serializer()) { etag ->
                api().panel(machineId, etag).also { notModified = it.code() == 304 }
            }
        ) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val full = ifNoneMatch == null
        val info = if (full) machineInfo(machineId) else null
        val statuses = if (full) workerStatuses(machineId, panel.worker?.id, info?.current?.versionNo) else emptyList()
        return AppResult.Success(
            PanelStateResult(
                panel = PanelStateDto(
                    machineId = machineId,
                    machineName = info?.name.orEmpty(),
                    state = panelState(panel.state),
                    activeWorkerId = panel.worker?.id,
                    activeWorkerName = panel.worker?.displayName,
                    override = panel.override?.let { toOverride(machineId, it) },
                    workerStatuses = statuses,
                ),
                etag = cache.get(key)?.etag.orEmpty(),
                isNotModified = notModified,
            ),
        )
    }

    /** shortcut: a Worker cannot see presence or an override on the Panel (403), wire when the backend ships a Worker view. */
    private suspend fun workerPanel(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> {
        val me = user ?: return AppResult.Failure(AppError.SessionExpired)
        val passport = when (val result = signOffs.getPassport(me.id)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val mine = passport.signOffs.find { it.machineId == machineId }
        val ready = mine?.status == SkillStatus.VALID || mine?.status == SkillStatus.EXPIRING
        val info = if (ifNoneMatch == null) machineInfo(machineId) else null
        val row = PanelWorkerStatus(
            workerId = me.id,
            displayName = me.displayName,
            isReady = ready,
            hasSignOff = ready,
            signOffVersion = info?.current?.versionNo?.let { "v$it" },
            signOffAt = mine?.passedAt?.take(10),
        )
        return AppResult.Success(
            PanelStateResult(
                panel = PanelStateDto(
                    machineId = machineId,
                    machineName = info?.name.orEmpty(),
                    state = if (ready) PanelState.READY else PanelState.LOCKED,
                    workerStatuses = listOf(row),
                ),
                etag = "",
            ),
        )
    }

    private suspend fun machineInfo(machineId: String): MachineInfoDto? =
        (safeApiCall { api().machine(machineId) } as? AppResult.Success)?.value

    /** One row per active Worker, from their own Passport; a Passport that cannot be read leaves the Worker out. */
    private suspend fun workerStatuses(machineId: String, presentId: String?, versionNo: Int?): List<PanelWorkerStatus> =
        (workers.getWorkers() as? AppResult.Success)?.value.orEmpty().mapNotNull { worker ->
            val list = (signOffs.getSignOffs(worker.id) as? AppResult.Success)?.value ?: return@mapNotNull null
            val mine = list.find { it.machineId == machineId }
            val ready = mine?.status == SkillStatus.VALID || mine?.status == SkillStatus.EXPIRING
            PanelWorkerStatus(
                workerId = worker.id,
                displayName = worker.displayName,
                isReady = ready,
                hasSignOff = ready,
                signOffVersion = versionNo?.takeIf { ready }?.let { "v$it" },
                signOffAt = mine?.passedAt?.take(10),
                isPresent = worker.id == presentId,
            )
        }

    override suspend fun requestOverride(
        machineId: String,
        workerId: String?,
        minutes: Int,
        reason: String,
        idempotencyKey: String,
    ): AppResult<PanelOverrideDto> {
        val body = OverrideInDto(workerId, minutes, reason)
        return safeApiCall { api().startOverride(machineId, IdempotencyKey(idempotencyKey), body) }.mapOk { dto ->
            PanelOverrideDto(dto.id, dto.machineId, dto.workerId, dto.by, dto.reason, minutes, dto.startsAt, dto.endsAt, dto.revokedAt)
        }
    }

    override suspend fun revokeOverride(machineId: String, overrideId: String): AppResult<Unit> {
        if (overrideId.isEmpty()) return AppResult.Failure(AppError.NotFound)
        return safeApiCall { api().endOverride(machineId, overrideId) }.mapOk { }
    }

    /** The server takes the Worker from the token, so [workerId] is not sent. */
    override suspend fun recordPresence(machineId: String, workerId: String): AppResult<Unit> =
        safeApiCallUnit { api().scan(machineId) }

    override suspend fun remindWorker(machineId: String, workerId: String): AppResult<Unit> =
        safeApiCallUnit { api().remind(machineId, RemindInDto(workerId)) }

    private fun toOverride(machineId: String, info: PanelOverrideInfoDto) = PanelOverrideDto(
        id = info.id,
        machineId = machineId,
        by = info.by,
        reason = info.reason,
        minutes = 0,
        startsAt = "",
        endsAt = info.until,
    )

    private fun panelState(wire: String): PanelState =
        PanelState.entries.firstOrNull { it.name.equals(wire, ignoreCase = true) } ?: PanelState.UNKNOWN
}
