package com.techtsunagu.wakatta.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.feature.insights.domain.LearningEntry
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory

@Composable
fun LearningScreen(
    userId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: LearningViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(userId) { viewModel.onEvent(LearningEvent.Init(userId)) }
    LearningContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick)
}

@Composable
fun LearningContent(
    state: LearningUiState,
    onEvent: (LearningEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val history = state.history
    val title = if (state.isForeman && history != null) {
        stringResource(R.string.insights_learning_title_of, history.workerName.substringBefore(' '))
    } else {
        stringResource(R.string.insights_learning_screen_title)
    }
    InsightsScaffold(
        title = title,
        onBackClick = onBackClick,
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.isEmpty,
        emptyMessage = stringResource(R.string.insights_learning_empty),
        onRetry = { onEvent(LearningEvent.Retry) },
        modifier = modifier,
    ) {
        if (history != null) LearningList(history, state.isForeman)
    }
}

@Composable
private fun LearningList(history: LearningHistory, isForeman: Boolean) {
    val gaps = history.entries.filter { it.status != FollowUpStatus.UNDERSTOOD }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "stats", contentType = "stats") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(Icons.Filled.Verified, history.mastered, R.string.insights_learning_mastered, Modifier.weight(1f))
                StatCard(Icons.AutoMirrored.Filled.HelpOutline, history.gaps, R.string.insights_learning_gaps, Modifier.weight(1f))
                StatCard(Icons.Filled.LocalFireDepartment, history.streak, R.string.insights_learning_streak, Modifier.weight(1f))
            }
        }
        item(key = "gaps-header", contentType = "header") { SectionHeader(stringResource(R.string.insights_learning_gaps)) }
        if (gaps.isEmpty()) {
            item(key = "gaps-none", contentType = "none") {
                Text(stringResource(R.string.insights_learning_no_gaps), style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(gaps, key = { "gap:" + it.term }, contentType = { "entry" }) { EntryRow(it, isForeman) }
        item(key = "timeline-header", contentType = "header") { SectionHeader(stringResource(R.string.insights_learning_timeline)) }
        items(history.entries, key = { "tl:" + it.term }, contentType = { "entry" }) { EntryRow(it, isForeman) }
    }
}

@Composable
private fun StatCard(icon: ImageVector, number: Int, label: Int, modifier: Modifier) {
    WakattaCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("$number", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                stringResource(label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EntryRow(entry: LearningEntry, isForeman: Boolean) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isForeman && entry.termEn.isNotEmpty()) {
                    Text(entry.termEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(entryMeta(entry), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(entry.status.chipStatus(), label = stringResource(entry.status.labelRes()))
        }
    }
}

@Composable
private fun entryMeta(entry: LearningEntry): String {
    val missed = if (entry.missed > 0) stringResource(R.string.insights_learning_missed, entry.missed) else ""
    val ago = entry.ageMinutes?.let { agoText(it) }.orEmpty()
    return listOf(missed, ago).filter { it.isNotEmpty() }.joinToString(" · ")
}
