package com.techtsunagu.wakatta.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData

@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit = {},
    onOpenLearning: (String) -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnalyticsContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick, onOpenLearning = onOpenLearning)
}

@Composable
fun AnalyticsContent(
    state: AnalyticsUiState,
    onEvent: (AnalyticsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOpenLearning: (String) -> Unit = {},
) {
    InsightsScaffold(
        title = stringResource(R.string.insights_analytics_screen_title),
        onBackClick = onBackClick,
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.isEmpty,
        emptyMessage = stringResource(R.string.insights_analytics_empty),
        onRetry = { onEvent(AnalyticsEvent.Retry) },
        modifier = modifier,
    ) {
        state.data?.let { AnalyticsList(it, state, onEvent, onOpenLearning) }
    }
}

@Composable
private fun AnalyticsList(
    data: AnalyticsData,
    state: AnalyticsUiState,
    onEvent: (AnalyticsEvent) -> Unit,
    onOpenLearning: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "kpis", contentType = "kpis") { KpiRow(data.cleanAiShare, data.firstTryPassShare) }
        item(key = "trend-h", contentType = "header") { SectionHeader(stringResource(R.string.insights_trend_title)) }
        item(key = "trend", contentType = "trend") {
            TrendCard(data.trend, state.selectedPoint) { onEvent(AnalyticsEvent.SelectPoint(it)) }
        }
        item(key = "funnel-h", contentType = "header") { SectionHeader(stringResource(R.string.insights_funnel_title)) }
        item(key = "funnel", contentType = "funnel") { FunnelCard(data) }
        item(key = "bars-h", contentType = "header") { SectionHeader(stringResource(R.string.insights_bars_title)) }
        item(key = "bars", contentType = "bars") { BarsCard(data.bars, onOpenLearning) }
        item(key = "vocab-h", contentType = "header") { SectionHeader(stringResource(R.string.insights_vocab_title)) }
        item(key = "vocab", contentType = "vocab") {
            VocabCard(data.terms, state.pinningTerm, state.pinError) { onEvent(AnalyticsEvent.PinTerm(it)) }
        }
        item(key = "heat-h", contentType = "header") { SectionHeader(stringResource(R.string.insights_heat_title)) }
        item(key = "heat", contentType = "heat") {
            HeatmapCard(data, state.selectedTerm, state.selectedWorkerId) { term, id ->
                onEvent(AnalyticsEvent.SelectCell(term, id))
            }
        }
        item(key = "value", contentType = "value") { ValueCard(data.stats) }
    }
}
