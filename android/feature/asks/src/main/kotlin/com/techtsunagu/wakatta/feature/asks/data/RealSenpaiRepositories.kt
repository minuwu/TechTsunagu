package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreatedDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val PAGE_LIMIT = 200

private fun SessionStore.worker(): User? = session.value?.user?.takeIf { it.role == Role.WORKER }

/** The app says "festival", the server says "event". */
private fun String.toWire() = if (this == "festival") "event" else this
private fun String.fromWire() = if (this == "event") "festival" else this

private suspend fun AsksApi.allMeetups() = allPages { c -> meetups(PAGE_LIMIT, c) }.orThrow().sortedByDescending { it.date }

/** The shop's pairs (Foreman); the server sends Worker ids, [workers] has their names. */
private suspend fun AsksApi.shopPairs(workers: WorkerRepository, meetups: List<MeetupOutDto>): List<SenpaiPairDto> {
    val pairs = allPages { c -> pairs(PAGE_LIMIT, c) }.orThrow()
    val byId = workers.getWorkers().orThrow().associateBy { it.id }
    return pairs.map { pair ->
        val worker = byId[pair.workerId]
        pair.toPair(pair.workerId, worker?.displayName.orEmpty(), worker?.language.orEmpty(), meetups)
    }
}

private fun SenpaiPairOutDto.toPair(workerId: String, workerName: String, language: String, meetups: List<MeetupOutDto>): SenpaiPairDto {
    val mine = meetups.filter { it.pairId == id }
    return SenpaiPairDto(
        id = id,
        workerId = workerId,
        workerName = workerName,
        workerLanguage = language,
        senpaiName = senpaiName,
        kind = kind,
        meetupCount = mine.size,
        lastMeetupDate = mine.maxOfOrNull { it.date },
        createdAt = createdAt,
        expiresAt = expiresAt,
    )
}

/** The flows load once per collection and throw [LoadFailed] when the server cannot be read. */
internal class RealSenpaiRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
    private val workers: WorkerRepository,
) : SenpaiRepository {

    override fun getSenpaiPairForWorker(workerId: String): Flow<SenpaiPairDto?> = flow {
        val user = session.worker() ?: return@flow emit(null)
        emit(api().pairOrNull()?.toPair(user.id, user.displayName, user.language, api().allMeetups()))
    }

    override fun getAllSenpaiPairs(): Flow<List<SenpaiPairDto>> = flow { emit(api().shopPairs(workers, api().allMeetups())) }

    override suspend fun createSenpaiPair(request: SenpaiPairCreateRequest): AppResult<SenpaiPairCreatedDto> =
        safeApiCall { api().createPair(request) }

    override suspend fun deleteSenpaiPair(pairId: String): AppResult<Unit> = safeApiCallUnit { api().revokePair(pairId) }
}

/** Meet-ups: a Worker reads and logs their own, a Foreman reads the shop's (the server lets only a Worker log one). */
internal class RealMeetupRepository(
    private val api: () -> AsksApi,
    private val session: SessionStore,
    private val workers: WorkerRepository,
) : MeetupRepository {

    override fun getMeetupsForWorker(workerId: String): Flow<List<MeetupDto>> = flow {
        val user = session.worker() ?: return@flow emit(emptyList())
        val senpai = api().pairOrNull()?.senpaiName.orEmpty()
        emit(api().allMeetups().map { it.toMeetup(user.id, user.displayName, senpai) })
    }

    override fun getAllMeetups(): Flow<List<MeetupDto>> = flow {
        val all = api().allMeetups()
        val byPair = api().shopPairs(workers, all).associateBy { it.id }
        emit(all.map { m -> byPair[m.pairId].let { m.toMeetup(it?.workerId.orEmpty(), it?.workerName.orEmpty(), it?.senpaiName.orEmpty()) } })
    }

    override suspend fun logMeetup(workerId: String, request: MeetupCreateRequest): AppResult<MeetupDto> {
        val user = session.worker() ?: return AppResult.Failure(AppError.Forbidden)
        val body = MeetupInDto(request.kind.toWire(), request.date, request.note)
        return safeApiCall { api().logMeetup(IdempotencyKey(request.idempotencyKey), body) }
            .map { it.toMeetup(user.id, user.displayName, "") }
    }

    private fun MeetupOutDto.toMeetup(workerId: String, workerName: String, senpaiName: String) =
        MeetupDto(id, workerId, workerName, pairId, senpaiName, kind.fromWire(), date, note, loggedBy)
}
