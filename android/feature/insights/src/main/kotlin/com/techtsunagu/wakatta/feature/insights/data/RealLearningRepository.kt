package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.insights.domain.LearningEntry
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import java.time.Duration
import java.time.Instant

internal class RealLearningRepository(
    private val api: () -> InsightsApi,
    private val sessionStore: SessionStore,
    private val now: () -> Instant = Instant::now,
) : LearningRepository {

    override suspend fun getHistory(workerId: String): AppResult<LearningHistory> {
        val user = sessionStore.session.value?.user
        // shortcut: there is no route for Foreman to get another Worker's learning history, wire when the backend ships it
        if (user != null && user.role == Role.FOREMAN && workerId != user.id) {
            return AppResult.Success(
                LearningHistory(
                    workerId = workerId,
                    workerName = "",
                    mastered = 0,
                    gaps = 0,
                    streak = 0,
                    entries = emptyList(),
                ),
            )
        }

        val learningDto = when (val r = safeApiCall { api().myLearning(limit = 200, cursor = null) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }

        val currentTime = now()
        // Group by termJa to prevent LazyColumn duplicate keys and compute missed count
        val grouped = learningDto.items.groupBy { it.termJa }
        val entries = grouped.map { (term, items) ->
            val latest = items.first()
            val status = when (latest.status.lowercase()) {
                "open" -> FollowUpStatus.OPEN
                "clarified" -> FollowUpStatus.CLARIFIED
                "understood" -> FollowUpStatus.UNDERSTOOD
                else -> FollowUpStatus.UNKNOWN
            }
            val ageMinutes = try {
                val inst = Instant.parse(latest.missedAt)
                Duration.between(inst, currentTime).toMinutes().coerceAtLeast(0)
            } catch (_: Exception) {
                null
            }
            LearningEntry(
                term = term,
                termEn = "",
                missed = items.size,
                status = status,
                ageMinutes = ageMinutes,
            )
        }

        val mastered = entries.count { it.status == FollowUpStatus.UNDERSTOOD }
        val gaps = entries.count { it.status != FollowUpStatus.UNDERSTOOD }
        val workerName = user?.displayName.orEmpty()

        return AppResult.Success(
            LearningHistory(
                workerId = user?.id ?: workerId,
                workerName = workerName,
                mastered = mastered,
                gaps = gaps,
                streak = learningDto.streakDays,
                entries = entries,
            ),
        )
    }
}
