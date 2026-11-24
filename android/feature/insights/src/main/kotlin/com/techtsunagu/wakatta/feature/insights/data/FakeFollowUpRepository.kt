package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.feature.insights.domain.FollowUp
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData
import com.techtsunagu.wakatta.feature.insights.domain.LoopStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeFollowUpRepository(private val delayMs: Long) : FollowUpRepository {

    @Inject
    constructor() : this(300L)

    override suspend fun getFollowUps(): AppResult<FollowUpsData> {
        delay(delayMs)
        val items = InsightsSeed.followUps.map { s ->
            FollowUp(
                id = s.id,
                workerId = s.workerId,
                workerName = DemoSeed.users.firstOrNull { it.id == s.workerId }?.displayName ?: s.workerId,
                term = s.term,
                termEn = InsightsSeed.termEn[s.term].orEmpty(),
                question = s.question,
                wrong = s.wrong,
                status = s.status,
                ageMinutes = s.ageMinutes,
            )
        }
        return AppResult.Success(FollowUpsData(items, loopStatsOf(items)))
    }
}

internal fun loopStatsOf(items: List<FollowUp>): LoopStats {
    val explained = items.count { it.status != FollowUpStatus.OPEN }
    val understood = items.count { it.status == FollowUpStatus.UNDERSTOOD }
    return LoopStats(items.size, explained, understood, InsightsSeed.SIGN_OFF_RECORDS + understood)
}
