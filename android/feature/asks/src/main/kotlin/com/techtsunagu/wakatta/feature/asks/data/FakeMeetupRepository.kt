package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class FakeMeetupRepository(
    private var simulatedLatencyMs: Long = 250L,
) : MeetupRepository {

    @Inject
    constructor() : this(250L)

    fun setLatency(latencyMs: Long) {
        simulatedLatencyMs = latencyMs
    }

    private val meetupsFlow = MutableStateFlow<List<MeetupDto>>(createInitialMeetups())

    override fun getMeetupsForWorker(workerId: String): Flow<List<MeetupDto>> {
        return meetupsFlow.map { list ->
            list.filter { it.workerId == workerId }.sortedByDescending { it.date }
        }
    }

    override fun getAllMeetups(): Flow<List<MeetupDto>> {
        return meetupsFlow.map { list -> list.sortedByDescending { it.date } }
    }

    override suspend fun logMeetup(workerId: String, request: MeetupCreateRequest): AppResult<MeetupDto> {
        delay(simulatedLatencyMs)
        val targetWorker = request.workerId ?: workerId
        if (targetWorker == "u_unpaired") {
            return AppResult.Failure(AppError.Conflict("NO_ACTIVE_PAIR"))
        }

        // Rule E-101: 422 if future date
        if (request.date > "2026-11-24") {
            return AppResult.Failure(AppError.Validation(listOf("date")))
        }

        val id = "mu_" + System.currentTimeMillis().toString(36)
        val newMeetup = MeetupDto(
            id = id,
            workerId = targetWorker,
            workerName = resolveWorkerName(targetWorker),
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = request.kind,
            date = request.date,
            note = request.note.trim(),
            loggedBy = if (workerId == "u_tanaka") "senpai" else "worker",
        )

        val current = meetupsFlow.value.toMutableList()
        current.add(0, newMeetup)
        meetupsFlow.value = current
        return AppResult.Success(newMeetup)
    }

    private fun resolveWorkerName(workerId: String): String = when (workerId) {
        "u_rahim" -> "Rahim Uddin"
        "u_nguyen" -> "Nguyen Van An"
        "u_siti" -> "Siti Rahma"
        else -> "Rahim Uddin"
    }

    private fun createInitialMeetups(): List<MeetupDto> = listOf(
        MeetupDto(
            id = "mu1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-10-27",
            note = "カフェで日本語の練習",
            loggedBy = "senpai",
        ),
        MeetupDto(
            id = "mu2",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-11-05",
            note = "給料明細の見かたを説明",
            loggedBy = "senpai",
        ),
        MeetupDto(
            id = "mu3",
            workerId = "u_nguyen",
            workerName = "Nguyen Van An",
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-11-12",
            note = "病院の行きかたを相談",
            loggedBy = "senpai",
        ),
        MeetupDto(
            id = "mu4",
            workerId = "u_siti",
            workerName = "Siti Rahma",
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = "drill",
            date = "2026-11-16",
            note = "駅前で避難経路を確認",
            loggedBy = "senpai",
        ),
    )
}
