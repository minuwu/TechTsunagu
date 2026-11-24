package com.techtsunagu.wakatta.feature.nearmiss

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class NearMissDetailUiState(
    val id: String = "",
    val report: NearMissDto? = null,
    val machine: Machine? = null,
    val worker: WorkerInfo? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val foremanName: String = "",
    val userLang: String = "ja",
    val isLoading: Boolean = false,
    val isCreatingTask: Boolean = false,
    val isAcknowledging: Boolean = false,
    @StringRes val toastMessage: Int? = null,
    val error: String? = null,
)

sealed interface NearMissDetailEvent {
    data class Init(val id: String) : NearMissDetailEvent
    data object Acknowledge : NearMissDetailEvent
    data object CreateTask : NearMissDetailEvent
    data object ClearToast : NearMissDetailEvent
    data object Retry : NearMissDetailEvent
    data object Refresh : NearMissDetailEvent
}

sealed interface NearMissDetailEffect

@HiltViewModel
class NearMissDetailViewModel @Inject constructor(
    private val nearMissRepository: NearMissRepository,
    private val taskRepository: TaskRepository,
    private val machineRepository: MachineRepository,
    private val workerRepository: WorkerRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<NearMissDetailUiState, NearMissDetailEvent, NearMissDetailEffect>(NearMissDetailUiState()) {

    private var unlinkedTask: Pair<String, String>? = null

    override fun onEvent(event: NearMissDetailEvent) {
        when (event) {
            is NearMissDetailEvent.Init -> loadDetail(event.id)
            is NearMissDetailEvent.Acknowledge -> acknowledgeReport()
            is NearMissDetailEvent.CreateTask -> createTaskFromReport()
            is NearMissDetailEvent.ClearToast -> updateState { it.copy(toastMessage = null) }
            is NearMissDetailEvent.Retry -> loadDetail(state.value.id)
            is NearMissDetailEvent.Refresh -> loadDetail(state.value.id)
        }
    }

    private fun loadDetail(reportId: String) {
        viewModelScope.launch {
            updateState { it.copy(id = reportId, isLoading = true, error = null) }
            val session = sessionStore.session.firstOrNull()
            val isForeman = session?.user?.role == Role.FOREMAN
            val currentUserId = session?.user?.id.orEmpty()
            val userLang = session?.user?.language ?: "ja"

            when (val result = nearMissRepository.getNearMiss(reportId)) {
                is AppResult.Success -> {
                    val report = result.value
                    val machineResult = machineRepository.getMachine(report.machineId)
                    val machine = if (machineResult is AppResult.Success) {
                        Machine(
                            id = machineResult.value.id,
                            code = machineResult.value.code,
                            name = machineResult.value.name,
                        )
                    } else {
                        null
                    }

                    val workersResult = workerRepository.getWorkers()
                    val worker = if (workersResult is AppResult.Success) {
                        workersResult.value.find { it.id == report.userId }
                    } else {
                        null
                    }

                    updateState {
                        it.copy(
                            isLoading = false,
                            report = report,
                            machine = machine,
                            worker = worker,
                            isForeman = isForeman,
                            currentUserId = currentUserId,
                            userLang = userLang,
                            error = null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = result.error.toString(),
                        )
                    }
                }
            }
        }
    }

    private fun acknowledgeReport() {
        val currentReport = state.value.report ?: return
        if (currentReport.status == NearMissStatus.ACK) return

        viewModelScope.launch {
            updateState { it.copy(isAcknowledging = true) }
            when (val ackResult = nearMissRepository.acknowledge(currentReport.id, state.value.currentUserId)) {
                is AppResult.Success -> {
                    val updated = currentReport.copy(
                        status = NearMissStatus.ACK,
                        acknowledgedAt = ackResult.value.acknowledgedAt,
                        acknowledgedBy = ackResult.value.acknowledgedBy,
                    )
                    updateState {
                        it.copy(
                            report = updated,
                            isAcknowledging = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isAcknowledging = false, toastMessage = R.string.nearmiss_action_failed) }
                }
            }
        }
    }

    private fun createTaskFromReport() {
        val currentReport = state.value.report ?: return
        if (currentReport.taskId != null) return

        viewModelScope.launch {
            updateState { it.copy(isCreatingTask = true) }
            unlinkedTask?.takeIf { it.first == currentReport.id }?.let { (_, taskId) ->
                linkTask(currentReport, taskId)
                return@launch
            }
            val machine = state.value.machine
            val taskTitle = mapOf(
                "ja" to "ヒヤリハット対応 · ${machine?.name?.get("ja") ?: machine?.code ?: currentReport.machineId}",
                "en" to "Near-miss follow-up · ${machine?.name?.get("en") ?: machine?.code ?: currentReport.machineId}",
                "bn" to "হিয়ারী-হাত্টো পদক্ষেপ · ${machine?.name?.get("bn") ?: machine?.code ?: currentReport.machineId}",
            )
            when (val taskRes = taskRepository.createTask(title = taskTitle, assignees = listOf(state.value.currentUserId), dueAt = "Today")) {
                is AppResult.Success -> linkTask(currentReport, taskRes.value.id)
                is AppResult.Failure -> updateState { it.copy(isCreatingTask = false, toastMessage = R.string.nearmiss_action_failed) }
            }
        }
    }

    /** A Task is made once; when the link fails, the next tap links the same Task. */
    private suspend fun linkTask(report: NearMissDto, taskId: String) {
        unlinkedTask = report.id to taskId
        when (val link = nearMissRepository.linkTask(report.id, taskId)) {
            is AppResult.Success -> {
                unlinkedTask = null
                updateState { it.copy(report = link.value, isCreatingTask = false, toastMessage = R.string.nearmiss_task_created_toast) }
                acknowledgeReport() // a report with a Task has been seen
            }
            is AppResult.Failure -> updateState { it.copy(isCreatingTask = false, toastMessage = R.string.nearmiss_action_failed) }
        }
    }
}

@Composable
fun NearMissDetailScreen(
    id: String,
    onBackClick: () -> Unit = {},
    viewModel: NearMissDetailViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.onEvent(NearMissDetailEvent.Init(id))
    }

    NearMissDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@Composable
fun NearMissDetailContent(
    state: NearMissDetailUiState,
    onEvent: (NearMissDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val resources = LocalResources.current

    LaunchedEffect(state.toastMessage) {
        val msg = state.toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(resources.getString(msg))
            onEvent(NearMissDetailEvent.ClearToast)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.nearmiss_detail_screen_title),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.isForeman && state.report != null) {
                ForemanDetailBottomBar(
                    hasTask = state.report.taskId != null,
                    isAcked = state.report.status == NearMissStatus.ACK,
                    isCreatingTask = state.isCreatingTask,
                    isAcknowledging = state.isAcknowledging,
                    onCreateTask = { onEvent(NearMissDetailEvent.CreateTask) },
                    onAcknowledge = { onEvent(NearMissDetailEvent.Acknowledge) },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading && state.report == null -> {
                    LoadingState(message = stringResource(R.string.nearmiss_loading))
                }
                state.error != null -> {
                    EmptyState(
                        title = stringResource(R.string.nearmiss_error),
                        message = stringResource(R.string.nearmiss_error_hint),
                        actionLabel = stringResource(R.string.nearmiss_retry),
                        onActionClick = { onEvent(NearMissDetailEvent.Retry) },
                    )
                }
                state.report == null -> {
                    EmptyState(
                        title = stringResource(R.string.nearmiss_detail_screen_title),
                        message = stringResource(R.string.nearmiss_not_found),
                    )
                }
                else -> {
                    DetailContentBody(
                        report = state.report,
                        machine = state.machine,
                        worker = state.worker,
                        isForeman = state.isForeman,
                        foremanName = state.foremanName,
                        userLang = state.userLang,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailContentBody(
    report: NearMissDto,
    machine: Machine?,
    worker: WorkerInfo?,
    isForeman: Boolean,
    foremanName: String,
    userLang: String,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Photo with ring mark
        if (report.photoUrl != null) {
            DetailPhotoView(
                photoUrl = report.photoUrl,
                photoMark = report.photoMark,
            )
        }

        // Status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            report.severity?.let { DetailSeverityChip(severity = it) }

            val status = when {
                report.status == NearMissStatus.ACK -> WakattaStatus.GREEN
                report.isQueuedOffline -> WakattaStatus.WAIT
                else -> WakattaStatus.ORANGE
            }
            val statusLabel = when {
                report.status == NearMissStatus.ACK -> {
                    if (foremanName.isNotEmpty()) stringResource(R.string.nearmiss_status_seen, foremanName)
                    else stringResource(R.string.nearmiss_filter_ack)
                }
                report.isQueuedOffline -> stringResource(R.string.nearmiss_status_waiting)
                else -> stringResource(R.string.nearmiss_status_sent)
            }
            StatusChip(status = status, label = statusLabel)
        }

        // Metadata Card
        val machineName = machine?.let { pick(it.name, userLang).ifEmpty { it.code } } ?: report.machineId
        val workerName = worker?.displayName ?: report.userName
        val formattedTime = formatTokyoFullTime(report.createdAt)

        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = machineName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(name = workerName, size = 22.dp)
                    Text(
                        text = workerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Translation Cards
        if (isForeman) {
            Text(
                text = stringResource(R.string.nearmiss_header_ja_trans),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = report.text["ja"] ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Text(
                text = stringResource(R.string.nearmiss_header_original),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = report.text["en"] ?: report.text["bn"] ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.nearmiss_header_worker_said),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = pick(report.text, userLang).ifEmpty { report.text["en"] ?: "" },
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = 26.sp,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Text(
                text = if (foremanName.isNotEmpty()) {
                    stringResource(R.string.nearmiss_header_foreman_reads, foremanName)
                } else {
                    stringResource(R.string.nearmiss_header_ja_trans)
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = report.text["ja"] ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        // Seen at timestamp
        if (report.status == NearMissStatus.ACK && report.acknowledgedAt != null) {
            Text(
                text = stringResource(R.string.nearmiss_seen_at, formatTokyoFullTime(report.acknowledgedAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailPhotoView(
    photoUrl: String,
    photoMark: PhotoMark?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 2f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(56.dp),
            )
        }

        if (photoMark != null) {
            val ringSize = 56.dp
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = ((photoMark.x / 100f) * constraints.maxWidth - (ringSize.toPx() / 2)).roundToInt(),
                            y = ((photoMark.y / 100f) * constraints.maxHeight - (ringSize.toPx() / 2)).roundToInt(),
                        )
                    }
                    .size(ringSize)
                    .border(width = 3.dp, color = MaterialTheme.colorScheme.error, shape = CircleShape),
            )
        }
    }
}

@Composable
private fun DetailSeverityChip(
    severity: NearMissSeverity,
    modifier: Modifier = Modifier,
) {
    val (labelRes, dotColor) = when (severity) {
        NearMissSeverity.LOW -> R.string.nearmiss_severity_low to Color(0xFF2E7D32)
        NearMissSeverity.MID -> R.string.nearmiss_severity_mid to Color(0xFFED6C02)
        NearMissSeverity.HIGH -> R.string.nearmiss_severity_high to Color(0xFFD32F2F)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ForemanDetailBottomBar(
    hasTask: Boolean,
    isAcked: Boolean,
    isCreatingTask: Boolean,
    isAcknowledging: Boolean,
    onCreateTask: () -> Unit,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilledTonalButton(
                onClick = onCreateTask,
                enabled = !hasTask && !isCreatingTask,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasTask) {
                        stringResource(R.string.nearmiss_btn_task_created)
                    } else {
                        stringResource(R.string.nearmiss_btn_make_task)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }

            Button(
                onClick = onAcknowledge,
                enabled = !isAcked && !isAcknowledging,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
            ) {
                Icon(
                    imageVector = if (isAcked) Icons.Default.Check else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAcked) {
                        stringResource(R.string.nearmiss_btn_acked)
                    } else {
                        stringResource(R.string.nearmiss_btn_ack)
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun formatTokyoFullTime(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val zone = ZoneId.of("Asia/Tokyo")
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(zone)
        formatter.format(instant)
    } catch (_: Exception) {
        isoString
    }
}
