package com.techtsunagu.wakatta.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.feature.insights.domain.FollowUp
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData

private val FILTERS = listOf(FollowUpStatus.OPEN, FollowUpStatus.CLARIFIED, FollowUpStatus.UNDERSTOOD)

@Composable
fun FollowUpsScreen(
    onBackClick: () -> Unit = {},
    viewModel: FollowUpsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FollowUpsContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick)
}

@Composable
fun FollowUpsContent(
    state: FollowUpsUiState,
    onEvent: (FollowUpsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    InsightsScaffold(
        title = stringResource(R.string.insights_followups_screen_title),
        onBackClick = onBackClick,
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.isEmpty,
        emptyMessage = stringResource(R.string.insights_followups_none_open),
        onRetry = { onEvent(FollowUpsEvent.Retry) },
        modifier = modifier,
    ) {
        state.data?.let { FollowUpsList(it, state.filter, onEvent, state.clarifying, state.clarifyError != null) }
    }
}

@Composable
private fun FollowUpsList(
    data: FollowUpsData,
    filter: FollowUpStatus,
    onEvent: (FollowUpsEvent) -> Unit,
    clarifying: Set<String> = emptySet(),
    clarifyFailed: Boolean = false,
) {
    val shown = data.items.filter { it.status == filter }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "filters", contentType = "filters") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FILTERS.forEach { status ->
                    FilterChip(
                        selected = status == filter,
                        onClick = { onEvent(FollowUpsEvent.SelectFilter(status)) },
                        label = {
                            Text(stringResource(status.labelRes()) + " " + data.items.count { it.status == status })
                        },
                    )
                }
            }
        }
        if (clarifyFailed) {
            item(key = "clarify_error", contentType = "error") {
                Text(
                    text = stringResource(R.string.insights_followup_clarify_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (shown.isEmpty()) {
            item(key = "none", contentType = "none") {
                Text(
                    text = stringResource(
                        if (filter == FollowUpStatus.OPEN) R.string.insights_followups_none_open else R.string.insights_followups_none_yet,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(shown, key = { it.id }, contentType = { "followup" }) {
            FollowUpRow(it, clarifying = it.id in clarifying, onClarify = { onEvent(FollowUpsEvent.Clarify(it.id)) })
        }
        item(key = "value", contentType = "value") { ValueCard(data.stats) }
    }
}

@Composable
private fun FollowUpRow(item: FollowUp, clarifying: Boolean = false, onClarify: () -> Unit = {}) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Avatar(name = item.workerName)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (item.termEn.isNotEmpty()) {
                    Text(item.termEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(item.question, style = MaterialTheme.typography.bodyMedium)
                item.wrong?.let {
                    Text(
                        text = stringResource(R.string.insights_followup_wrong, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                // M10: the Foreman explained it; the Worker's next right answer makes it understood
                if (item.status == FollowUpStatus.OPEN) {
                    OutlinedButton(onClick = onClarify, enabled = !clarifying, modifier = Modifier.padding(top = 4.dp)) {
                        Text(stringResource(R.string.insights_followup_clarified_button))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(item.status.chipStatus(), label = stringResource(item.status.labelRes()))
                Text(agoText(item.ageMinutes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
