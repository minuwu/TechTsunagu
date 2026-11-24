package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.LearningEntry
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

/** Builds the history from Confusions and Follow-ups, like the prototype learnData. */
@Singleton
class FakeLearningRepository(
    private val delayMs: Long,
    private val followUps: FollowUpRepository,
) : LearningRepository {

    @Inject
    constructor(followUps: FollowUpRepository) : this(300L, followUps)

    private class Acc(var missed: Int, var order: Int, var status: FollowUpStatus, var age: Long?)

    override suspend fun getHistory(workerId: String): AppResult<LearningHistory> {
        delay(delayMs)
        val user = DemoSeed.users.firstOrNull { it.id == workerId }
            ?: return AppResult.Failure(AppError.NotFound)
        val fu = when (val r = followUps.getFollowUps()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.value.items.filter { it.workerId == workerId }
        }
        val terms = LinkedHashMap<String, Acc>()
        InsightsSeed.confusions.filter { it.workerId == workerId }.forEachIndexed { i, c ->
            val a = terms.getOrPut(c.term) { Acc(0, i, FollowUpStatus.UNDERSTOOD, null) }
            a.missed++
            a.order = i
            a.age = c.daysAgo * InsightsSeed.MINUTES_PER_DAY
        }
        fu.forEachIndexed { i, f ->
            val a = terms.getOrPut(f.term) { Acc(0, 100 + i, FollowUpStatus.UNDERSTOOD, null) }
            a.status = f.status
            a.age = f.ageMinutes
            a.order = maxOf(a.order, 100 + i)
            a.missed = maxOf(a.missed, 1)
        }
        val entries = terms.entries.sortedByDescending { it.value.order }.map { (term, a) ->
            LearningEntry(term, InsightsSeed.termEn[term].orEmpty(), a.missed, a.status, a.age)
        }
        return AppResult.Success(
            LearningHistory(
                workerId = user.id,
                workerName = user.displayName,
                mastered = entries.count { it.status == FollowUpStatus.UNDERSTOOD },
                gaps = entries.count { it.status != FollowUpStatus.UNDERSTOOD },
                streak = InsightsSeed.streak[workerId] ?: 0,
                entries = entries,
            ),
        )
    }
}
