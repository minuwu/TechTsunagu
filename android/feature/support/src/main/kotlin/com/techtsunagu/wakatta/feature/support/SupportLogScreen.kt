package com.techtsunagu.wakatta.feature.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.ShareFile
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import com.techtsunagu.wakatta.feature.support.domain.SupportLogData
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportMeetupDto
import com.techtsunagu.wakatta.feature.support.domain.SupportNoticeDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class SupportLogUiState(
    val workerId: String = "all",
    val selectedMonth: String = DEFAULT_MONTH,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val error: SupportError? = null,
    val logData: SupportLogData? = null,
) {
    val isEmpty: Boolean
        get() = logData == null || (
            logData.dueInterviews.isEmpty() &&
                logData.meetups.isEmpty() &&
                logData.notices.isEmpty()
            )

    companion object {
        const val DEFAULT_MONTH = "2026-11"
    }
}

internal sealed interface SupportLogEvent {
    data class Init(val workerId: String?) : SupportLogEvent
    data class SelectWorker(val workerId: String) : SupportLogEvent
    data class SelectMonth(val month: String) : SupportLogEvent
    data object ExportCsv : SupportLogEvent
    data object Retry : SupportLogEvent
}

internal sealed interface SupportLogEffect {
    data object Exported : SupportLogEffect
    data class Failed(val error: SupportError) : SupportLogEffect
}

@HiltViewModel
internal class SupportLogViewModel @Inject constructor(
    private val supportLogRepository: SupportLogRepository,
    private val shareFile: ShareFile,
) : MviViewModel<SupportLogUiState, SupportLogEvent, SupportLogEffect>(SupportLogUiState()) {

    private var loadJob: Job? = null

    override fun onEvent(event: SupportLogEvent) {
        when (event) {
            is SupportLogEvent.Init -> load(event.workerId?.takeIf { it.isNotBlank() } ?: "all", state.value.selectedMonth)
            is SupportLogEvent.SelectWorker -> load(event.workerId, state.value.selectedMonth)
            is SupportLogEvent.SelectMonth -> load(state.value.workerId, event.month)
            is SupportLogEvent.ExportCsv -> exportCsv()
            is SupportLogEvent.Retry -> load(state.value.workerId, state.value.selectedMonth)
        }
    }

    private fun load(workerId: String, month: String) {
        loadJob?.cancel()
        updateState { it.copy(workerId = workerId, selectedMonth = month, isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            when (val res = supportLogRepository.getSupportLog(workerId, month)) {
                is AppResult.Success -> updateState { it.copy(isLoading = false, logData = res.value) }
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = res.error.toSupportError()) }
            }
        }
    }

    private fun exportCsv() {
        if (state.value.isExporting) return
        updateState { it.copy(isExporting = true) }
        viewModelScope.launch {
            val s = state.value
            when (val res = supportLogRepository.exportSupportLogCsv(s.workerId, s.selectedMonth)) {
                is AppResult.Success -> {
                    updateState { it.copy(isExporting = false) }
                    shareFile.share(res.value, "text/csv")
                    sendEffect(SupportLogEffect.Exported)
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isExporting = false) }
                    sendEffect(SupportLogEffect.Failed(res.error.toSupportError()))
                }
            }
        }
    }
}

@Composable
internal fun SupportLogScreen(
    workerId: String? = null,
    onBackClick: () -> Unit = {},
    onNavigateToInterview: (String) -> Unit = {},
    viewModel: SupportLogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(workerId) { viewModel.onEvent(SupportLogEvent.Init(workerId)) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            val message = when (effect) {
                SupportLogEffect.Exported -> context.getString(R.string.support_log_export_success)
                is SupportLogEffect.Failed -> context.getString(effect.error.message)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    SupportLogContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onNavigateToInterview = onNavigateToInterview,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun SupportLogContent(
    state: SupportLogUiState,
    onEvent: (SupportLogEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToInterview: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.support_log_screen_title),
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = { onEvent(SupportLogEvent.ExportCsv) },
                        enabled = !state.isExporting,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.support_log_export_csv),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.support_loading),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.error != null -> {
                    ErrorState(
                        message = stringResource(state.error.message),
                        onRetry = { onEvent(SupportLogEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.support_log_screen_title),
                        message = stringResource(R.string.support_empty),
                        actionLabel = stringResource(R.string.support_retry),
                        onActionClick = { onEvent(SupportLogEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.logData != null -> {
                    SupportLogBody(
                        data = state.logData,
                        selectedWorkerId = state.workerId,
                        selectedMonth = state.selectedMonth,
                        isExporting = state.isExporting,
                        onEvent = onEvent,
                        onNavigateToInterview = onNavigateToInterview,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportLogBody(
    data: SupportLogData,
    selectedWorkerId: String,
    selectedMonth: String,
    isExporting: Boolean,
    onEvent: (SupportLogEvent) -> Unit,
    onNavigateToInterview: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "filters", contentType = "filters") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Worker filter
                Text(
                    text = stringResource(R.string.support_log_filter_worker),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WakattaChip(
                        selected = selectedWorkerId == "all",
                        onClick = { onEvent(SupportLogEvent.SelectWorker("all")) },
                        label = { Text(stringResource(R.string.support_log_filter_all_workers)) },
                    )
                    data.workers.forEach { worker ->
                        WakattaChip(
                            selected = selectedWorkerId == worker.id,
                            onClick = { onEvent(SupportLogEvent.SelectWorker(worker.id)) },
                            label = { Text(worker.displayName.split(" ").first()) },
                        )
                    }
                }

                // Month filter
                Text(
                    text = stringResource(R.string.support_log_filter_month),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    data.availableMonths.forEach { m ->
                        val label = monthLabel(m)
                        WakattaChip(
                            selected = selectedMonth == m,
                            onClick = { onEvent(SupportLogEvent.SelectMonth(m)) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }

        // Summary Card
        item(key = "summary", contentType = "summary") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.support_log_summary,
                            monthLabel(data.selectedMonth),
                            data.summary.interviewCount,
                            data.summary.meetupCount,
                            data.summary.noticeOkCount,
                            data.summary.noticeTotalCount,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.support_log_target,
                            data.workers.find { it.id == selectedWorkerId }?.displayName
                                ?: stringResource(R.string.support_log_target_all),
                            stringResource(R.string.support_log_desc_reporting),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Export button
        item(key = "export_btn", contentType = "action") {
            Button(
                onClick = { onEvent(SupportLogEvent.ExportCsv) },
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.support_log_exporting))
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.support_log_export_csv))
                }
            }
        }

        // Section 1: Periodic Interviews
        item(key = "section_iv_header", contentType = "header") {
            Text(
                text = stringResource(R.string.support_log_section_interviews),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (data.dueInterviews.isEmpty()) {
            item(key = "empty_iv", contentType = "empty") {
                Text(
                    text = stringResource(R.string.support_log_empty_interviews),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = data.dueInterviews,
                key = { "iv_${it.workerId}" },
                contentType = { "interview_row" },
            ) { iv ->
                InterviewDueRow(
                    due = iv,
                    onClick = { onNavigateToInterview(iv.workerId) },
                )
            }
        }

        // Section 2: Meet-ups with Senpai
        item(key = "section_mu_header", contentType = "header") {
            Text(
                text = stringResource(R.string.support_log_section_meetups, data.meetups.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (data.meetups.isEmpty()) {
            item(key = "empty_mu", contentType = "empty") {
                Text(
                    text = stringResource(R.string.support_log_empty_meetups),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = data.meetups,
                key = { "mu_${it.id}" },
                contentType = { "meetup_row" },
            ) { mu ->
                MeetupRow(meetup = mu)
            }
        }

        // Section 3: Notice Confirmations
        item(key = "section_notices_header", contentType = "header") {
            Text(
                text = stringResource(R.string.support_log_section_notices, data.summary.noticeOkCount, data.summary.noticeTotalCount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (data.notices.isEmpty()) {
            item(key = "empty_notices", contentType = "empty") {
                Text(
                    text = stringResource(R.string.support_log_empty_notices),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = data.notices,
                key = { "notice_${it.id}" },
                contentType = { "notice_row" },
            ) { notice ->
                NoticeConfirmationRow(
                    notice = notice,
                    workers = data.workers,
                    filterWorkerId = selectedWorkerId,
                )
            }
        }

        item(key = "bottom_spacer", contentType = "spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InterviewDueRow(
    due: InterviewDueDto,
    onClick: () -> Unit,
) {
    WakattaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                name = due.workerName,
                size = 40.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = due.workerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                val lastStr = due.lastDate ?: stringResource(R.string.support_log_none)
                val dueStr = due.dueDate?.let { stringResource(R.string.support_log_due_interview, it) } ?: ""
                Text(
                    text = stringResource(R.string.support_log_last_interview, lastStr) + (if (dueStr.isNotEmpty()) " · $dueStr" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val wakattaStatus = when (due.status) {
                InterviewStatus.OK -> WakattaStatus.GREEN
                InterviewStatus.SOON -> WakattaStatus.ORANGE
                InterviewStatus.OVERDUE, InterviewStatus.UNKNOWN -> WakattaStatus.RED
            }
            StatusChip(
                status = wakattaStatus,
                label = dueLabel(due),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MeetupRow(meetup: SupportMeetupDto) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (icon, kindLabel) = when (meetup.kind) {
                "coffee" -> Icons.Default.Coffee to R.string.support_meetup_coffee
                "drill" -> Icons.Default.Warning to R.string.support_meetup_drill
                "festival" -> Icons.Default.Event to R.string.support_meetup_festival
                else -> Icons.Default.Groups to R.string.support_meetup_other
            }
            Icon(
                imageVector = icon,
                contentDescription = stringResource(kindLabel),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${meetup.workerName} · ${meetup.senpaiName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = meetup.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (meetup.noteJa.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = meetup.noteJa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoticeConfirmationRow(
    notice: SupportNoticeDto,
    workers: List<com.techtsunagu.wakatta.core.model.WorkerInfo>,
    filterWorkerId: String,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notice.titleJa,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val displayWorkers = if (filterWorkerId == "all") {
                    workers
                } else {
                    workers.filter { it.id == filterWorkerId }
                }
                displayWorkers.forEach { w ->
                    val status = notice.workerStatuses[w.id] ?: WakattaStatus.WAIT
                    val shortName = w.displayName.split(" ").first()
                    StatusChip(
                        status = status,
                        label = "$shortName: ${statusLabel(status)}",
                    )
                }
            }
        }
    }
}

@Composable
private fun statusLabel(status: WakattaStatus): String {
    return when (status) {
        WakattaStatus.GREEN -> stringResource(R.string.support_log_status_ok)
        WakattaStatus.ORANGE -> stringResource(R.string.support_log_status_soon)
        WakattaStatus.RED -> stringResource(R.string.support_log_status_overdue)
        WakattaStatus.WAIT, WakattaStatus.UNKNOWN -> stringResource(R.string.support_log_status_wait)
    }
}
