package com.techtsunagu.wakatta.feature.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.briefing.data.BriefingRepository
import com.techtsunagu.wakatta.feature.briefing.data.userMessage
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingReceipts
import com.techtsunagu.wakatta.feature.briefing.model.BriefingWorkerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class BriefingBoardUiState(
    val id: String? = null,
    val briefing: BriefingForForeman? = null,
    val receipts: BriefingReceipts? = null,
    val workerStatuses: List<BriefingWorkerStatus> = emptyList(),
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
)

sealed interface BriefingBoardEvent {
    data class Init(val id: String?) : BriefingBoardEvent
    data object Refresh : BriefingBoardEvent
    data object Retry : BriefingBoardEvent
    data class SendReexplanation(val workerId: String, val workerName: String) : BriefingBoardEvent
    data object DismissSnackbar : BriefingBoardEvent
}

sealed interface BriefingBoardEffect

@HiltViewModel
class BriefingBoardViewModel @Inject constructor(
    private val repository: BriefingRepository,
) : MviViewModel<BriefingBoardUiState, BriefingBoardEvent, BriefingBoardEffect>(BriefingBoardUiState()) {

    override fun onEvent(event: BriefingBoardEvent) {
        when (event) {
            is BriefingBoardEvent.Init -> {
                updateState { it.copy(id = event.id) }
                loadBoardData(event.id)
            }
            is BriefingBoardEvent.Refresh -> loadBoardData(state.value.id)
            is BriefingBoardEvent.Retry -> {
                updateState { it.copy(error = null) }
                loadBoardData(state.value.id)
            }
            is BriefingBoardEvent.SendReexplanation -> sendReexplanation(event.workerId, event.workerName)
            is BriefingBoardEvent.DismissSnackbar -> updateState { it.copy(snackbarMessage = null) }
        }
    }

    private fun loadBoardData(id: String?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val briefingResult = repository.getBriefingForForeman(id)) {
                is AppResult.Success -> {
                    val briefing = briefingResult.value
                    val receiptsResult = repository.getReceipts(briefing.id)
                    val receipts = (receiptsResult as? AppResult.Success)?.value
                    val statusesResult = repository.getWorkerStatuses(briefing.id)
                    val statuses = (statusesResult as? AppResult.Success)?.value ?: emptyList()

                    updateState {
                        it.copy(
                            id = briefing.id,
                            briefing = briefing,
                            receipts = receipts,
                            workerStatuses = statuses,
                            isLoading = false,
                            isEmpty = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            isEmpty = true,
                            error = briefingResult.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun sendReexplanation(workerId: String, workerName: String) {
        val briefingId = state.value.briefing?.id ?: return
        viewModelScope.launch {
            when (repository.sendReexplanation(briefingId, workerId)) {
                is AppResult.Success -> {
                    updateState { it.copy(snackbarMessage = workerName) }
                }
                is AppResult.Failure -> {
                    // Ignored in simulated flow
                }
            }
        }
    }
}

@Composable
fun BriefingBoardScreen(
    id: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: BriefingBoardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.onEvent(BriefingBoardEvent.Init(id))
    }

    BriefingBoardContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@Composable
fun BriefingBoardContent(
    state: BriefingBoardUiState,
    onEvent: (BriefingBoardEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { name ->
            snackbarHostState.showSnackbar(name)
            onEvent(BriefingBoardEvent.DismissSnackbar)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.briefing_board_screen_title),
                onBackClick = onBackClick,
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
                    LoadingState(message = stringResource(R.string.briefing_loading))
                }
                state.error != null && state.briefing == null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(BriefingBoardEvent.Retry) },
                        retryLabel = stringResource(R.string.briefing_retry),
                    )
                }
                state.isEmpty || state.briefing == null -> {
                    EmptyState(
                        title = stringResource(R.string.briefing_board_screen_title),
                        message = stringResource(R.string.briefing_empty),
                        actionLabel = stringResource(R.string.briefing_retry),
                        onActionClick = { onEvent(BriefingBoardEvent.Retry) },
                    )
                }
                else -> {
                    BoardDataContent(
                        briefing = state.briefing,
                        workerStatuses = state.workerStatuses,
                        onSendReexplanation = { workerId, workerName ->
                            onEvent(BriefingBoardEvent.SendReexplanation(workerId, workerName))
                        },
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardDataContent(
    briefing: BriefingForForeman,
    workerStatuses: List<BriefingWorkerStatus>,
    onSendReexplanation: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalWorkers = workerStatuses.size
    val confirmedCount = workerStatuses.count { it.status != WakattaStatus.WAIT }
    val greenCount = workerStatuses.count { it.status == WakattaStatus.GREEN }
    val orangeCount = workerStatuses.count { it.status == WakattaStatus.ORANGE }
    val redCount = workerStatuses.count { it.status == WakattaStatus.RED }
    val waitCount = workerStatuses.count { it.status == WakattaStatus.WAIT }

    val progress = if (totalWorkers > 0) confirmedCount.toFloat() / totalWorkers else 0f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Summary Header Card
        item(key = "summary_card", contentType = "summary") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Progress indicator ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(72.dp),
                        ) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxSize(),
                                color = StatusColors.Green,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 6.dp,
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$confirmedCount/$totalWorkers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "確認済",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pick(briefing.focusText, "ja"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = briefing.createdAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status counts row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (greenCount > 0) {
                            StatusCountChip(
                                label = stringResource(R.string.briefing_board_status_green, greenCount),
                                color = StatusColors.Green,
                                containerColor = StatusColors.GreenContainer,
                            )
                        }
                        if (orangeCount > 0) {
                            StatusCountChip(
                                label = stringResource(R.string.briefing_board_status_orange, orangeCount),
                                color = StatusColors.Orange,
                                containerColor = StatusColors.OrangeContainer,
                            )
                        }
                        if (redCount > 0) {
                            StatusCountChip(
                                label = stringResource(R.string.briefing_board_status_red, redCount),
                                color = StatusColors.Red,
                                containerColor = StatusColors.RedContainer,
                            )
                        }
                        if (waitCount > 0) {
                            StatusCountChip(
                                label = stringResource(R.string.briefing_board_status_wait, waitCount),
                                color = StatusColors.Grey,
                                containerColor = StatusColors.GreyContainer,
                            )
                        }
                    }
                }
            }
        }

        // Attendance & Understanding Header
        item(key = "workers_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_board_workers_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Worker list
        items(
            items = workerStatuses,
            key = { it.worker.id },
            contentType = { "worker_row" },
        ) { statusItem ->
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name = statusItem.worker.displayName)
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = statusItem.worker.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val attendedText = if (statusItem.hasAttended) {
                                stringResource(R.string.briefing_board_attended)
                            } else {
                                stringResource(R.string.briefing_board_unattended)
                            }
                            val attendedBg = if (statusItem.hasAttended) {
                                StatusColors.GreenContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                            val attendedColor = if (statusItem.hasAttended) {
                                StatusColors.OnGreenContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = attendedBg,
                            ) {
                                Text(
                                    text = attendedText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = attendedColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                )
                            }

                            statusItem.answerText?.let { ans ->
                                Text(
                                    text = ans,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        StatusChip(status = statusItem.status)
                        // "Explain again" stays hidden until a backend route re-explains a Briefing (M6):
                        // RealBriefingRepository.sendReexplanation does nothing yet, so the button would lie.
                    }
                }
            }
        }

        // Check Question Section Header
        item(key = "question_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_board_question_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Check Question Card
        item(key = "question_card", contentType = "question") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pick(briefing.question.prompt, "ja"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val correctOpt = briefing.question.options.getOrNull(briefing.question.correctIndex)
                    val correctText = pick(correctOpt, "ja")
                    Text(
                        text = stringResource(R.string.briefing_board_correct_answer_label, correctText),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusColors.Green,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCountChip(
    label: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
