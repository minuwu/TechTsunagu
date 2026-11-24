package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.insights.domain.FollowUp
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData
import com.techtsunagu.wakatta.feature.insights.domain.LoopStats
import java.time.Duration
import java.time.Instant

internal class RealFollowUpRepository(
    private val api: () -> InsightsApi,
    private val now: () -> Instant = Instant::now,
) : FollowUpRepository {

    override suspend fun getFollowUps(): AppResult<FollowUpsData> {
        val page = when (val r = safeApiCall { api().followUps(limit = 200) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }

        val currentTime = now()
        val items = page.items.map { dto ->
            val status = when (dto.status.lowercase()) {
                "open" -> FollowUpStatus.OPEN
                "clarified" -> FollowUpStatus.CLARIFIED
                "understood" -> FollowUpStatus.UNDERSTOOD
                else -> FollowUpStatus.UNKNOWN
            }
            val createdInstant = try {
                Instant.parse(dto.createdAt)
            } catch (_: Exception) {
                null
            }
            val ageMinutes = if (createdInstant != null) {
                Duration.between(createdInstant, currentTime).toMinutes().coerceAtLeast(0)
            } else 0L

            FollowUp(
                id = dto.id,
                workerId = dto.userId,
                workerName = dto.displayName,
                term = dto.termJa,
                termEn = "",
                question = dto.note,
                wrong = null,
                status = status,
                ageMinutes = ageMinutes,
            )
        }

        val explained = items.count { it.status != FollowUpStatus.OPEN }
        val understood = items.count { it.status == FollowUpStatus.UNDERSTOOD }
        val stats = LoopStats(
            flagged = items.size,
            explained = explained,
            understood = understood,
            records = -1, // Server does not provide training records count; -1 renders as dash in ValueCard
        )

        return AppResult.Success(FollowUpsData(items, stats))
    }

    override suspend fun clarify(followUpId: String, note: String): AppResult<Unit> =
        when (val r = safeApiCall { api().updateFollowUp(followUpId, FollowUpUpdateDto(status = "clarified", note = note)) }) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> r
        }
}
