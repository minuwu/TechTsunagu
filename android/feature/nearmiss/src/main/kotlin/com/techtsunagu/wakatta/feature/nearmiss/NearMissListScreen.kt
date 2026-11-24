package com.techtsunagu.wakatta.feature.nearmiss

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class NearMissListUiState(
    val filter: NearMissStatus = NearMissStatus.OPEN,
    val openCount: Int = 0,
    val seenCount: Int = 0,
    val allReports: List<NearMissDto> = emptyList(),
    val displayedReports: List<NearMissDto> = emptyList(),
    val machines: Map<String, Machine> = emptyMap(),
    val workers: Map<String, WorkerInfo> = emptyMap(),
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val userLang: String = "ja",
    val foremanName: String = "",
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
)

sealed interface NearMissListEvent {
    data object Init : NearMissListEvent
    data class SetFilter(val filter: NearMissStatus) : NearMissListEvent
    data object Retry : NearMissListEvent
    data object Refresh : NearMissListEvent
}

sealed interface NearMissListEffect

@HiltViewModel
class NearMissListViewModel @Inject constructor(
    private val nearMissRepository: NearMissRepository,
    private val machineRepository: MachineRepository,
    private val workerRepository: WorkerRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<NearMissListUiState, NearMissListEvent, NearMissListEffect>(NearMissListUiState()) {

    override fun onEvent(event: NearMissListEvent) {
        when (event) {
            is NearMissListEvent.Init -> loadData()
            is NearMissListEvent.SetFilter -> {
                updateState {
                    val filtered = it.allReports.filter { r -> r.status == event.filter }
                    it.copy(
                        filter = event.filter,
                        displayedReports = filtered,
                        isEmpty = filtered.isEmpty(),
                    )
                }
            }
            is NearMissListEvent.Retry -> loadData()
            is NearMissListEvent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            val session = sessionStore.session.firstOrNull()
            val isForeman = session?.user?.role == Role.FOREMAN
            val currentUserId = session?.user?.id.orEmpty()
            val userLang = session?.user?.language ?: "ja"

            val machinesResult = machineRepository.getMachines()
            val machinesMap = if (machinesResult is AppResult.Success) {
                machinesResult.value.associateBy { it.id }
            } else {
                emptyMap()
            }

            val workersResult = workerRepository.getWorkers()
            val workersMap = if (workersResult is AppResult.Success) {
                workersResult.value.associateBy { it.id }
            } else {
                emptyMap()
            }

            when (val reportsResult = nearMissRepository.getNearMisses()) {
                is AppResult.Success -> {
                    val relevantReports = reportsResult.value.filter { isForeman || it.userId == currentUserId }
                    val openCount = relevantReports.count { it.status == NearMissStatus.OPEN }
                    val seenCount = relevantReports.count { it.status == NearMissStatus.ACK }
                    val filtered = relevantReports.filter { it.status == state.value.filter }
                    updateState {
                        it.copy(
                            isLoading = false,
                            isForeman = isForeman,
                            currentUserId = currentUserId,
                            userLang = userLang,
                            machines = machinesMap,
                            workers = workersMap,
                            allReports = relevantReports,
                            displayedReports = filtered,
                            openCount = openCount,
                            seenCount = seenCount,
                            isEmpty = filtered.isEmpty(),
                            error = null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = reportsResult.error.toString(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearMissListScreen(
    onReportClick: (String) -> Unit = {},
    onNewReportClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: NearMissListViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(NearMissListEvent.Init)
    }

    NearMissListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onReportClick = onReportClick,
        onNewReportClick = onNewReportClick,
        onBackClick = onBackClick,
    )
}

@Composable
fun NearMissListContent(
    state: NearMissListUiState,
    onEvent: (NearMissListEvent) -> Unit,
    modifier: Modifier = Modifier,
    onReportClick: (String) -> Unit = {},
    onNewReportClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.nearmiss_list_screen_title),
                onBackClick = onBackClick,
            )
        },
        floatingActionButton = {
            if (!state.isForeman) {
                FloatingActionButton(
                    onClick = onNewReportClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.nearmiss_new_report_fab),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Filter chips (Open / Seen with counts)
            FilterChipsRow(
                currentFilter = state.filter,
                openCount = state.openCount,
                seenCount = state.seenCount,
                onFilterSelect = { onEvent(NearMissListEvent.SetFilter(it)) },
            )

            when {
                state.isLoading && state.allReports.isEmpty() -> {
                    LoadingState(message = stringResource(R.string.nearmiss_loading))
                }
                state.error != null -> {
                    EmptyState(
                        title = stringResource(R.string.nearmiss_error),
                        message = stringResource(R.string.nearmiss_error_hint),
                        actionLabel = stringResource(R.string.nearmiss_retry),
                        onActionClick = { onEvent(NearMissListEvent.Retry) },
                    )
                }
                state.isEmpty -> {
                    val emptyMsg = if (state.filter == NearMissStatus.OPEN) {
                        stringResource(R.string.nearmiss_empty_open)
                    } else {
                        stringResource(R.string.nearmiss_empty_ack)
                    }
                    EmptyState(
                        title = stringResource(R.string.nearmiss_list_screen_title),
                        message = emptyMsg,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = state.displayedReports,
                            key = { it.id },
                            contentType = { "nearmiss_row" },
                        ) { report ->
                            val machine = state.machines[report.machineId]
                            val worker = state.workers[report.userId]
                            NearMissItemRow(
                                report = report,
                                machine = machine,
                                worker = worker,
                                isForeman = state.isForeman,
                                userLang = state.userLang,
                                onClick = { onReportClick(report.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    currentFilter: NearMissStatus,
    openCount: Int,
    seenCount: Int,
    onFilterSelect: (NearMissStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WakattaChip(
            selected = currentFilter == NearMissStatus.OPEN,
            onClick = { onFilterSelect(NearMissStatus.OPEN) },
            label = {
                Text("${stringResource(R.string.nearmiss_filter_open)} $openCount")
            },
        )
        WakattaChip(
            selected = currentFilter == NearMissStatus.ACK,
            onClick = { onFilterSelect(NearMissStatus.ACK) },
            label = {
                Text("${stringResource(R.string.nearmiss_filter_ack)} $seenCount")
            },
        )
    }
}

@Composable
private fun NearMissItemRow(
    report: NearMissDto,
    machine: Machine?,
    worker: WorkerInfo?,
    isForeman: Boolean,
    userLang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barColor = when (report.severity) {
        NearMissSeverity.LOW -> Color(0xFF2E7D32)
        NearMissSeverity.MID -> Color(0xFFED6C02)
        NearMissSeverity.HIGH -> Color(0xFFD32F2F)
        null -> MaterialTheme.colorScheme.outline
    }

    val machineName = machine?.let { pick(it.name, userLang).ifEmpty { it.code } } ?: report.machineId
    val workerName = worker?.displayName ?: report.userName
    val displayText = if (isForeman) {
        report.text["ja"] ?: report.text["en"] ?: ""
    } else {
        pick(report.text, userLang).ifEmpty { report.text["en"] ?: "" }
    }
    val formattedTime = formatTokyoTime(report.createdAt)

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Severity bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(barColor, RoundedCornerShape(2.dp)),
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = machineName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (isForeman) {
                        Avatar(name = workerName, size = 18.dp)
                        Text(
                            text = workerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    report.severity?.let { SeverityChip(severity = it) }
                }
            }

            // Thumbnail if photo present
            if (report.photoUrl != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(
    severity: NearMissSeverity,
    modifier: Modifier = Modifier,
) {
    val (labelRes, dotColor) = when (severity) {
        NearMissSeverity.LOW -> R.string.nearmiss_severity_low to Color(0xFF2E7D32)
        NearMissSeverity.MID -> R.string.nearmiss_severity_mid to Color(0xFFED6C02)
        NearMissSeverity.HIGH -> R.string.nearmiss_severity_high to Color(0xFFD32F2F)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape),
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatTokyoTime(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        val zone = ZoneId.of("Asia/Tokyo")
        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(zone)
        formatter.format(instant)
    } catch (_: Exception) {
        isoString.takeLast(8)
    }
}
