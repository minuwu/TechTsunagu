package com.techtsunagu.wakatta.feature.insights

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.insights.domain.LoopStats

/** Text for an error a ViewModel reported; ViewModels never build user-visible text themselves. */
@StringRes
internal fun AppError.insightsMessageRes(): Int = when (this) {
    AppError.Offline, AppError.Timeout -> R.string.insights_error_offline
    AppError.Forbidden -> R.string.insights_error_forbidden
    AppError.NotFound -> R.string.insights_error_not_found
    else -> R.string.insights_error_generic
}

internal fun FollowUpStatus.chipStatus(): WakattaStatus = when (this) {
    FollowUpStatus.OPEN -> WakattaStatus.RED
    FollowUpStatus.CLARIFIED -> WakattaStatus.ORANGE
    FollowUpStatus.UNDERSTOOD -> WakattaStatus.GREEN
    FollowUpStatus.UNKNOWN -> WakattaStatus.WAIT
}

@StringRes
internal fun FollowUpStatus.labelRes(): Int = when (this) {
    FollowUpStatus.OPEN -> R.string.insights_status_open
    FollowUpStatus.CLARIFIED -> R.string.insights_status_clarified
    FollowUpStatus.UNDERSTOOD -> R.string.insights_status_understood
    FollowUpStatus.UNKNOWN -> R.string.insights_status_unknown
}

@Composable
internal fun agoText(minutes: Long): String = when {
    minutes < 1 -> stringResource(R.string.insights_ago_now)
    minutes < 60 -> stringResource(R.string.insights_ago_minutes, minutes.toInt())
    minutes < 1440 -> stringResource(R.string.insights_ago_hours, (minutes / 60).toInt())
    else -> stringResource(R.string.insights_ago_days, (minutes / 1440).toInt())
}

/** Top bar plus the shared loading, error and empty states; [content] shows when there is data. */
@Composable
internal fun InsightsScaffold(
    title: String,
    onBackClick: () -> Unit,
    isLoading: Boolean,
    error: AppError?,
    isEmpty: Boolean,
    emptyMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { WakattaTopBar(title = title, onBackClick = onBackClick) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                isLoading -> LoadingState(message = stringResource(R.string.insights_loading))
                error != null -> ErrorState(
                    message = stringResource(error.insightsMessageRes()),
                    onRetry = onRetry,
                    retryLabel = stringResource(R.string.insights_retry),
                )
                isEmpty -> EmptyState(title = title, message = emptyMessage)
                else -> content()
            }
        }
    }
}

@Composable
internal fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 8.dp).semantics { heading() },
    )
}

/** "One instruction, three wins": what the loop gives the Worker, the Foreman and the employer. */
@Composable
internal fun ValueCard(stats: LoopStats, modifier: Modifier = Modifier) {
    WakattaCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.insights_value_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValueColumn(R.string.insights_role_worker, R.string.insights_value_worker, "${stats.understood}", Modifier.weight(1f))
                ValueColumn(R.string.insights_role_foreman, R.string.insights_value_foreman, "${stats.explained}/${stats.flagged}", Modifier.weight(1f))
                ValueColumn(R.string.insights_role_employer, R.string.insights_value_employer, "${stats.records}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ValueColumn(@StringRes who: Int, @StringRes line: Int, number: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(who), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(number, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(line), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
