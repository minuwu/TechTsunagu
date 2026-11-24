package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.HeatCell
import com.techtsunagu.wakatta.feature.insights.domain.HeatRow
import com.techtsunagu.wakatta.feature.insights.domain.TermStat
import com.techtsunagu.wakatta.feature.insights.domain.WorkerBar
import com.techtsunagu.wakatta.feature.insights.domain.WorkerRef
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val VOCAB_ROWS = 5
private const val HEAT_ROWS = 6

@Singleton
class FakeAnalyticsRepository(
    private val delayMs: Long,
    private val followUps: FollowUpRepository,
    private val glossary: GlossaryRepository,
) : AnalyticsRepository {

    @Inject
    constructor(followUps: FollowUpRepository, glossary: GlossaryRepository) : this(300L, followUps, glossary)

    override suspend fun getAnalytics(): AppResult<AnalyticsData> {
        delay(delayMs)
        val fu = when (val r = followUps.getFollowUps()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.value
        }
        val pinned = when (val r = glossary.getTerms()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.value.map { it.ja }.toSet()
        }
        val workers = DemoSeed.users.filter { it.role == Role.WORKER }
        val terms = termStats(pinned)
        val stats = fu.stats
        val sent = InsightsSeed.BASE_SENT
        val pct = (100.0 * (InsightsSeed.BASE_OK + stats.understood) / sent).roundToInt().coerceAtMost(100)
        return AppResult.Success(
            AnalyticsData(
                trend = InsightsSeed.baseTrend + pct,
                sent = sent,
                understood = InsightsSeed.BASE_OK,
                stats = stats,
                cleanAiShare = InsightsSeed.CLEAN_AI_SHARE,
                firstTryPassShare = InsightsSeed.FIRST_TRY_PASS_SHARE,
                bars = workers.map { w ->
                    val (g, o, r) = InsightsSeed.baseBars[w.id] ?: Triple(0, 0, 0)
                    WorkerBar(w.id, w.displayName, g, o, r)
                },
                terms = terms.take(VOCAB_ROWS),
                heatWorkers = workers.map { WorkerRef(it.id, it.displayName) },
                heat = terms.take(HEAT_ROWS).map { t -> HeatRow(t.term, workers.map { heatCell(t.term, it.id) }) },
            ),
        )
    }

    override suspend fun pinTerm(term: String, termEn: String): AppResult<Unit> {
        delay(delayMs)
        return when (val r = glossary.addTerm(term, "", "", termEn)) {
            is AppResult.Failure -> r
            is AppResult.Success -> AppResult.Success(Unit)
        }
    }

    private fun heatCell(term: String, workerId: String): HeatCell {
        val ages = InsightsSeed.confusions.filter { it.term == term && it.workerId == workerId }
            .map { it.daysAgo * InsightsSeed.MINUTES_PER_DAY }
        return HeatCell(workerId, ages.size, ages)
    }

    /** Count, workers affected and trend (later half minus earlier half, oldest first) per term, most confused first. */
    private fun termStats(pinned: Set<String>): List<TermStat> {
        val rows = InsightsSeed.confusions.sortedByDescending { it.daysAgo }
        val early = rows.take((rows.size + 1) / 2).groupingBy { it.term }.eachCount()
        return rows.groupBy { it.term }.map { (term, list) ->
            val late = list.size - (early[term] ?: 0)
            TermStat(
                term = term,
                termEn = InsightsSeed.termEn[term].orEmpty(),
                workerCount = list.map { it.workerId }.distinct().size,
                count = list.size,
                trend = late - (early[term] ?: 0),
                inGlossary = term in pinned,
            )
        }.sortedByDescending { it.count }
    }
}
