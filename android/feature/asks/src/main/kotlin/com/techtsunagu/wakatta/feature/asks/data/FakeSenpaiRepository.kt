package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreatedDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakeSenpaiRepository(
    private var simulatedLatencyMs: Long = 250L,
) : SenpaiRepository {

    @Inject
    constructor() : this(250L)

    fun setLatency(latencyMs: Long) {
        simulatedLatencyMs = latencyMs
    }

    private val pairsFlow = MutableStateFlow<List<SenpaiPairDto>>(createInitialPairs())

    override fun getSenpaiPairForWorker(workerId: String): Flow<SenpaiPairDto?> {
        return pairsFlow.map { list -> list.firstOrNull { it.workerId == workerId } }
    }

    override fun getAllSenpaiPairs(): Flow<List<SenpaiPairDto>> = pairsFlow.asStateFlow()

    override suspend fun createSenpaiPair(request: SenpaiPairCreateRequest): AppResult<SenpaiPairCreatedDto> {
        delay(simulatedLatencyMs)
        val current = pairsFlow.value.toMutableList()
        if (current.any { it.workerId == request.workerId }) {
            return AppResult.Failure(AppError.Conflict("ALREADY_EXISTS"))
        }

        val pairId = "sp_" + System.currentTimeMillis().toString(36)
        val token = "tok_" + System.currentTimeMillis().toString(36) + "xyz"
        val newPair = SenpaiPairDto(
            id = pairId,
            workerId = request.workerId,
            workerName = resolveWorkerName(request.workerId),
            senpaiName = request.senpaiName,
            senpaiEnName = if (request.senpaiName == "佐藤 美咲") "Sato Misaki" else request.senpaiName,
            kind = request.kind,
            meetupCount = 0,
            lastMeetupDate = null,
            createdAt = "2026-11-24",
        )
        current.add(newPair)
        pairsFlow.value = current

        return AppResult.Success(
            SenpaiPairCreatedDto(
                id = pairId,
                workerId = request.workerId,
                senpaiName = request.senpaiName,
                kind = request.kind,
                token = token,
            ),
        )
    }

    override suspend fun deleteSenpaiPair(pairId: String): AppResult<Unit> {
        delay(simulatedLatencyMs)
        val current = pairsFlow.value.toMutableList()
        val removed = current.removeIf { it.id == pairId }
        if (!removed) return AppResult.Failure(AppError.NotFound)
        pairsFlow.value = current
        return AppResult.Success(Unit)
    }

    private fun resolveWorkerName(workerId: String): String = when (workerId) {
        "u_rahim" -> "Rahim Uddin"
        "u_nguyen" -> "Nguyen Van An"
        "u_siti" -> "Siti Rahma"
        else -> "Worker $workerId"
    }

    private fun createInitialPairs(): List<SenpaiPairDto> = listOf(
        SenpaiPairDto(
            id = "sp1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            workerLanguage = "bn",
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            kind = "coworker",
            meetupCount = 2,
            lastMeetupDate = "2026-11-05",
            createdAt = "2026-10-15",
        ),
        SenpaiPairDto(
            id = "sp2",
            workerId = "u_nguyen",
            workerName = "Nguyen Van An",
            workerLanguage = "vi",
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            kind = "coworker",
            meetupCount = 1,
            lastMeetupDate = "2026-11-12",
            createdAt = "2026-10-15",
        ),
        SenpaiPairDto(
            id = "sp3",
            workerId = "u_siti",
            workerName = "Siti Rahma",
            workerLanguage = "id",
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            kind = "coworker",
            meetupCount = 1,
            lastMeetupDate = "2026-11-16",
            createdAt = "2026-10-15",
        ),
    )
}
