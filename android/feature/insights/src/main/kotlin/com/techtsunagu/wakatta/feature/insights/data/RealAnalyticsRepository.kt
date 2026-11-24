package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.HeatCell
import com.techtsunagu.wakatta.feature.insights.domain.HeatRow
import com.techtsunagu.wakatta.feature.insights.domain.LoopStats
import com.techtsunagu.wakatta.feature.insights.domain.TermStat
import com.techtsunagu.wakatta.feature.insights.domain.WorkerBar
import com.techtsunagu.wakatta.feature.insights.domain.WorkerRef
import kotlin.math.roundToInt

internal class RealAnalyticsRepository(
    private val api: () -> InsightsApi,
    private val followUps: FollowUpRepository,
    private val glossary: GlossaryRepository,
) : AnalyticsRepository {

    override suspend fun getAnalytics(): AppResult<AnalyticsData> {
        val understanding = when (val r = safeApiCall { api().understanding(days = 30) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }

        val heatmap = when (val r = safeApiCall { api().heatmap() }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> null
        }

        val fuStats = when (val r = followUps.getFollowUps()) {
            is AppResult.Success -> r.value.stats
            is AppResult.Failure -> LoopStats(flagged = -1, explained = -1, understood = -1, records = -1)
        }

        val pinnedTerms = when (val r = glossary.getTerms()) {
            is AppResult.Success -> r.value.map { it.ja }.toSet()
            is AppResult.Failure -> emptySet()
        }

        // Aggregate 30 daily counts into weekly understanding rates
        // Sort oldest day first
        val sortedTrend = understanding.trend.sortedBy { it.day }
        // Group into weekly chunks of 7 days
        val chunks = sortedTrend.chunked(7)
        val weeklyTrend = chunks.mapNotNull { chunk ->
            val total = chunk.sumOf { it.green + it.orange + it.red }
            if (total > 0) {
                (100.0 * chunk.sumOf { it.green } / total).roundToInt().coerceIn(0, 100)
            } else {
                null
            }
        }

        val sent = understanding.trend.sumOf { it.green + it.orange + it.red }
        val understood = understanding.trend.sumOf { it.green }

        val bars = understanding.perWorker.map {
            WorkerBar(
                workerId = it.workerId,
                name = it.displayName,
                green = it.green,
                orange = it.orange,
                red = it.red,
            )
        }

        val terms = understanding.topTerms.map {
            TermStat(
                term = it.termJa,
                termEn = "",
                workerCount = 0,
                count = it.count,
                trend = 0,
                inGlossary = it.termJa in pinnedTerms,
                canPin = false,
            )
        }

        val heatWorkers = heatmap?.machines?.map {
            WorkerRef(
                id = it.id,
                name = pick(it.name, "ja").ifEmpty { it.code },
            )
        } ?: emptyList()

        val heat = heatmap?.rows?.map { row ->
            HeatRow(
                term = row.ja,
                cells = row.cells.map { cell ->
                    HeatCell(
                        workerId = cell.machineId,
                        count = cell.count,
                        agesMinutes = emptyList(),
                    )
                },
            )
        } ?: emptyList()

        return AppResult.Success(
            AnalyticsData(
                trend = weeklyTrend,
                sent = sent,
                understood = understood,
                stats = fuStats,
                timeSavedHours = -1.0, // Server does not provide hours saved; -1 renders as dash in KPI card
                reworkAvoided = -1, // Server does not provide rework avoided; -1 renders as dash in KPI card
                bars = bars,
                terms = terms,
                heatWorkers = heatWorkers,
                heat = heat,
            ),
        )
    }

    override suspend fun pinTerm(term: String, termEn: String): AppResult<Unit> {
        // shortcut: POST /glossary requires reading and bn, which are not available from pinTerm; action is hidden
        return AppResult.Failure(AppError.Validation())
    }
}
