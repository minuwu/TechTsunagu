package com.techtsunagu.wakatta.feature.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val taskId: String = "",
    val isLoading: Boolean = false,
    val task: Task? = null,
    val machine: Machine? = null,
    val sourceMessageText: String? = null,
    val sourceMessageSender: String? = null,
    val sourceMessageAt: String? = null,
    val sourceThreadId: String? = null,
    val isConflict: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val currentUserLang: String = "ja",
    val isForeman: Boolean = true,
    val messageInfo: String? = null,
) {
    val isEmpty: Boolean get() = task == null && !isLoading && error == null
}

sealed interface TaskDetailEvent {
    data class Init(val taskId: String) : TaskDetailEvent
    data object Refresh : TaskDetailEvent
    data object Retry : TaskDetailEvent
    data class ChangeColumn(val newColumn: TaskColumn) : TaskDetailEvent
    data object ClickComments : TaskDetailEvent
    data object ClearMessage : TaskDetailEvent
}

sealed interface TaskDetailEffect {
    data class ShowSnackbar(val message: String) : TaskDetailEffect
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<TaskDetailUiState, TaskDetailEvent, TaskDetailEffect>(TaskDetailUiState()) {

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                val role = session?.user?.role ?: Role.FOREMAN
                val lang = session?.user?.language ?: "ja"
                updateState { it.copy(isForeman = role == Role.FOREMAN, currentUserLang = lang) }
            }
        }
    }

    override fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.Init -> {
                updateState { it.copy(taskId = event.taskId) }
                loadTask(event.taskId)
            }
            is TaskDetailEvent.Refresh -> loadTask(state.value.taskId)
            is TaskDetailEvent.Retry -> loadTask(state.value.taskId)
            is TaskDetailEvent.ChangeColumn -> changeColumn(event.newColumn)
            is TaskDetailEvent.ClickComments -> updateState {
                it.copy(messageInfo = "Comments are not in the prototype")
            }
            is TaskDetailEvent.ClearMessage -> updateState { it.copy(messageInfo = null) }
        }
    }

    private fun loadTask(taskId: String) {
        if (taskId.isBlank()) return
        updateState { it.copy(isLoading = true, error = null, isConflict = false) }
        viewModelScope.launch {
            when (val result = taskRepository.getTask(taskId)) {
                is AppResult.Success -> {
                    val task = result.value
                    val machine = DemoSeed.machines.find { it.id == task.machineId }
                    val isM1 = task.fromMessageId == "m1"
                    updateState {
                        it.copy(
                            isLoading = false,
                            task = task,
                            machine = machine,
                            sourceMessageText = if (isM1) "今日は3号機の油の点検を先にしてください。オイルが少ないときは、私に言ってください。" else null,
                            sourceMessageSender = if (isM1) "田中 健一" else null,
                            sourceMessageAt = if (isM1) "08:02" else null,
                            sourceThreadId = if (isM1) "t_line2" else null,
                            error = null,
                            isConflict = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = formatError(result.error),
                        )
                    }
                }
            }
        }
    }

    private fun changeColumn(newColumn: TaskColumn) {
        val currentTask = state.value.task ?: return
        if (currentTask.column == newColumn) return
        updateState { it.copy(isUpdating = true) }
        viewModelScope.launch {
            val result = taskRepository.moveTask(
                id = currentTask.id,
                column = newColumn,
                assignees = currentTask.assignees,
                dueAt = currentTask.dueAt,
                version = currentTask.version,
            )
            when (result) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(
                            isUpdating = false,
                            task = result.value,
                            isConflict = false,
                            error = null,
                            messageInfo = "Task moved",
                        )
                    }
                }
                is AppResult.Failure -> {
                    if (result.error is AppError.PreconditionFailed) {
                        updateState {
                            it.copy(
                                isUpdating = false,
                                isConflict = true,
                                error = "Conflict: Task was modified by someone else.",
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                isUpdating = false,
                                error = formatError(result.error),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun formatError(error: AppError): String = when (error) {
        is AppError.Offline -> "Offline. Please check network connection."
        is AppError.PreconditionFailed -> "Conflict (HTTP 412): Task was modified."
        is AppError.NotFound -> "Task not found."
        else -> "An unexpected error occurred."
    }
}

@Composable
fun TaskDetailScreen(
    taskId: String,
    onBackClick: () -> Unit = {},
    onNavigateToMachine: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    viewModel: TaskDetailViewModel = viewModel(),
) {
    LaunchedEffect(taskId) {
        viewModel.onEvent(TaskDetailEvent.Init(taskId))
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    TaskDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onNavigateToMachine = onNavigateToMachine,
        onNavigateToChat = onNavigateToChat,
    )
}

@Composable
fun TaskDetailContent(
    state: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToMachine: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.messageInfo) {
        state.messageInfo?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onEvent(TaskDetailEvent.ClearMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.board_task_screen_title),
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
                state.isLoading && state.task == null -> {
                    LoadingState(message = stringResource(R.string.board_loading))
                }
                state.error != null && state.task == null && !state.isConflict -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(TaskDetailEvent.Retry) },
                    )
                }
                state.task != null -> {
                    TaskDetailBody(
                        state = state,
                        onEvent = onEvent,
                        onNavigateToMachine = onNavigateToMachine,
                        onNavigateToChat = onNavigateToChat,
                    )
                }
                else -> {
                    EmptyState(
                        title = stringResource(R.string.board_task_screen_title),
                        message = stringResource(R.string.board_empty),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailBody(
    state: TaskDetailUiState,
    onEvent: (TaskDetailEvent) -> Unit,
    onNavigateToMachine: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
) {
    val task = state.task ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        if (state.isConflict) {
            ConflictBanner(onReloadClick = { onEvent(TaskDetailEvent.Refresh) })
            Spacer(modifier = Modifier.height(16.dp))
        }

        TriText(
            tri = task.title,
            lang = state.currentUserLang,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.board_task_status),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        TaskColumnSegmentedControl(
            selectedColumn = task.column,
            onColumnSelected = { col -> onEvent(TaskDetailEvent.ChangeColumn(col)) },
            enabled = !state.isUpdating,
        )
        Spacer(modifier = Modifier.height(20.dp))

        AssigneesSection(task = task)
        Spacer(modifier = Modifier.height(16.dp))

        DueAndMachineCard(
            task = task,
            machine = state.machine,
            lang = state.currentUserLang,
            onMachineClick = { task.machineId?.let(onNavigateToMachine) },
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.sourceMessageText != null) {
            SourceMessageSection(
                sender = state.sourceMessageSender ?: "",
                time = state.sourceMessageAt ?: "",
                text = state.sourceMessageText,
                onOpenChat = { state.sourceThreadId?.let(onNavigateToChat) },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        CommentsSection(onClick = { onEvent(TaskDetailEvent.ClickComments) })
    }
}

@Composable
private fun ConflictBanner(
    onReloadClick: () -> Unit,
) {
    WakattaCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(R.string.board_conflict_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.board_conflict_message),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = onReloadClick) {
                Text(text = stringResource(R.string.board_reload))
            }
        }
    }
}

@Composable
private fun AssigneesSection(task: Task) {
    Text(
        text = stringResource(R.string.board_task_assigned_to),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        task.assignees.forEach { userId ->
            val user = DemoSeed.users.find { it.id == userId }
            val name = user?.displayName ?: userId
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Avatar(name = name, size = 32.dp)
                    Text(text = name, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun DueAndMachineCard(
    task: Task,
    machine: Machine?,
    lang: String,
    onMachineClick: () -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        WakattaListItem(
            headlineContent = { Text(stringResource(R.string.board_task_due)) },
            supportingContent = { Text(task.dueAt ?: "-") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        if (machine != null) {
            WakattaListItem(
                headlineContent = { Text(stringResource(R.string.board_task_machine)) },
                supportingContent = { TriText(tri = machine.name, lang = lang) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                    )
                },
                onClick = onMachineClick,
            )
        }
    }
}

@Composable
private fun SourceMessageSection(
    sender: String,
    time: String,
    text: String,
    onOpenChat: () -> Unit,
) {
    Text(
        text = stringResource(R.string.board_task_source_message),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Avatar(name = sender, size = 20.dp)
                    Text(
                        text = "$sender · $time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = onOpenChat,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
        Text(
            text = stringResource(R.string.board_task_open_in_chat),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun CommentsSection(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.board_task_comments),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.board_task_add_comment),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
