package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateResult
import com.techtsunagu.wakatta.feature.machines.domain.PanelWorkerStatus
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakePanelRepository(
    private val delayMs: Long = 200L
) : PanelRepository {

    @Inject
    constructor() : this(200L)

    private data class InternalMachinePanel(
        val machineId: String,
        var state: PanelState,
        var activeWorkerId: String? = null,
        var activeWorkerName: String? = null,
        var activeOverride: PanelOverrideDto? = null,
        var versionCounter: Int = 1,
    )

    private val panels = mutableMapOf<String, InternalMachinePanel>().apply {
        put("mc1", InternalMachinePanel("mc1", PanelState.READY, activeWorkerId = "u_rahim", activeWorkerName = "Rahim Uddin", versionCounter = 1))
        put("mc2", InternalMachinePanel("mc2", PanelState.LOCKED, activeWorkerId = null, activeWorkerName = null, versionCounter = 1))
        put("mc3", InternalMachinePanel("mc3", PanelState.READY, activeWorkerId = "u_siti", activeWorkerName = "Siti Rahma", versionCounter = 1))
    }

    private val overridesByKey = mutableMapOf<String, PanelOverrideDto>()

    private fun generateEtag(p: InternalMachinePanel): String {
        val raw = "${p.machineId}_${p.state}_${p.activeWorkerId}_${p.activeOverride?.id}_${p.versionCounter}"
        val bytes = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return "\"" + bytes.joinToString("") { "%02x".format(it) } + "\""
    }

    override suspend fun getPanelState(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> {
        if (delayMs > 0) delay(delayMs)
        val panel = panels[machineId] ?: return AppResult.Failure(AppError.NotFound)

        val etag = generateEtag(panel)
        if (ifNoneMatch != null && ifNoneMatch == etag) {
            return AppResult.Success(
                PanelStateResult(
                    panel = buildDto(panel),
                    etag = etag,
                    isNotModified = true
                )
            )
        }

        return AppResult.Success(
            PanelStateResult(
                panel = buildDto(panel),
                etag = etag,
                isNotModified = false
            )
        )
    }

    private fun buildDto(internal: InternalMachinePanel): PanelStateDto {
        val machine = DemoSeed.machines.find { it.id == internal.machineId }
        val name = machine?.name ?: mapOf("ja" to "機械", "en" to "Machine")

        val workerStatuses = listOf(
            PanelWorkerStatus(
                workerId = "u_rahim",
                displayName = "Rahim Uddin",
                isReady = internal.machineId != "mc2" || internal.state == PanelState.OVERRIDE,
                hasSignOff = internal.machineId != "mc2",
                signOffVersion = if (internal.machineId == "mc1") "v2" else if (internal.machineId == "mc3") "v4" else null,
                signOffAt = if (internal.machineId != "mc2") "2026-11-20" else null,
                isPresent = internal.activeWorkerId == "u_rahim"
            ),
            PanelWorkerStatus(
                workerId = "u_nguyen",
                displayName = "Nguyen Van An",
                isReady = true,
                hasSignOff = true,
                signOffVersion = "v3",
                signOffAt = "2026-11-14",
                isPresent = internal.activeWorkerId == "u_nguyen"
            ),
            PanelWorkerStatus(
                workerId = "u_siti",
                displayName = "Siti Rahma",
                isReady = internal.machineId == "mc3" || internal.state == PanelState.OVERRIDE,
                hasSignOff = internal.machineId == "mc3",
                signOffVersion = if (internal.machineId == "mc3") "v4" else null,
                signOffAt = if (internal.machineId == "mc3") "2026-11-18" else null,
                isPresent = internal.activeWorkerId == "u_siti"
            )
        )

        return PanelStateDto(
            machineId = internal.machineId,
            machineName = name,
            state = internal.state,
            activeWorkerId = internal.activeWorkerId,
            activeWorkerName = internal.activeWorkerName,
            override = internal.activeOverride,
            workerStatuses = workerStatuses
        )
    }

    override suspend fun requestOverride(
        machineId: String,
        workerId: String?,
        minutes: Int,
        reason: String,
        idempotencyKey: String,
    ): AppResult<PanelOverrideDto> {
        if (delayMs > 0) delay(delayMs)
        // A retry with the same key returns the first result instead of starting a second override.
        overridesByKey[idempotencyKey]?.let { return AppResult.Success(it) }
        val trimmedReason = reason.trim()
        if (trimmedReason.length < 3 || trimmedReason.length > 200) {
            return AppResult.Failure(AppError.Validation(listOf("Reason must be between 3 and 200 characters")))
        }
        if (minutes < 1 || minutes > 480) {
            return AppResult.Failure(AppError.Validation(listOf("Override duration must be between 1 and 480 minutes")))
        }

        val panel = panels[machineId] ?: return AppResult.Failure(AppError.NotFound)
        val overrideId = "ov_${System.currentTimeMillis()}"
        val overrideDto = PanelOverrideDto(
            id = overrideId,
            machineId = machineId,
            workerId = workerId,
            by = "田中 健一",
            reason = trimmedReason,
            minutes = minutes,
            startsAt = "2026-11-23T10:00:00Z",
            endsAt = "2026-11-23T18:00:00Z"
        )

        overridesByKey[idempotencyKey] = overrideDto
        panel.activeOverride = overrideDto
        panel.state = PanelState.OVERRIDE
        panel.versionCounter++

        return AppResult.Success(overrideDto)
    }

    override suspend fun revokeOverride(machineId: String, overrideId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        val panel = panels[machineId] ?: return AppResult.Failure(AppError.NotFound)
        if (panel.activeOverride?.id == overrideId) {
            panel.activeOverride = null
            panel.state = if (machineId == "mc2") PanelState.LOCKED else PanelState.READY
            panel.versionCounter++
        }
        return AppResult.Success(Unit)
    }

    override suspend fun recordPresence(machineId: String, workerId: String): AppResult<Unit> {
        if (delayMs > 0) delay(delayMs)
        val panel = panels[machineId] ?: return AppResult.Failure(AppError.NotFound)
        val user = DemoSeed.users.find { it.id == workerId }
        panel.activeWorkerId = workerId
        panel.activeWorkerName = user?.displayName ?: workerId
        panel.versionCounter++
        return AppResult.Success(Unit)
    }
}
