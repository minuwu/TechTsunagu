package com.techtsunagu.wakatta.feature.board

import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
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
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.model.pick
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val taskId: String = "",
    val isLoading: Boolean = false,
    val task: Task? = null,
    val machine: Machine? = null,
    val machines: List<Machine> = emptyList(),
    val isConflict: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val isUpdating: Boolean = false,
    val currentUserLang: String = "",
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val users: List<User> = emptyList(),
    @StringRes val messageRes: Int? = null,
) {
    val isEmpty: Boolean get() = task == null && !isLoading && errorRes == null
}

sealed interface TaskDetailEvent {
    data class Init(val taskId: String) : TaskDetailEvent
    data object Refresh : TaskDetailEvent
    data object Retry : TaskDetailEvent
    data class ChangeColumn(val newColumn: TaskColumn) : TaskDetailEvent
    data object ClearMessage : TaskDetailEvent
}

sealed interface TaskDetailEffect {
    data class ShowSnackbar(val message: String) : TaskDetailEffect
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionStore: SessionStore,
    private val machineRepository: MachineRepository,
    private val workerRepository: WorkerRepository,
    private val languageManager: LanguageManager,
) : MviViewModel<TaskDetailUiState, TaskDetailEvent, TaskDetailEffect>(TaskDetailUiState()) {

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                if (session == null) return@collect
                val isFm = session.user.role == Role.FOREMAN
                updateState {
                    it.copy(
                        isForeman = isFm,
                        currentUserId = session.user.id,
                        currentUserName = session.user.displayName,
                        users = if (isFm) it.users else listOf(session.user),
                    )
                }
                if (isFm) {
                    loadUsers()
                }
            }
        }
        viewModelScope.launch {
            languageManager.currentLanguage.collect { lang ->
                updateState { it.copy(currentUserLang = lang) }
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            val sessionUser = sessionStore.session.value?.user
            if (sessionUser?.role != Role.FOREMAN) return@launch
            val workers = (workerRepository.getWorkers() as? AppResult.Success)?.value.orEmpty()
            val workerUsers = workers.map { User(it.id, it.displayName, Role.WORKER, it.language) }
            val allUsers = if (workerUsers.none { it.id == sessionUser.id }) {
                listOf(sessionUser) + workerUsers
            } else {
                workerUsers
            }
            updateState { it.copy(users = allUsers) }
        }
    }

    override fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.Init -> {
                updateState { it.copy(taskId = event.taskId) }
                loadTask(event.taskId)
            }
            is TaskDetailEvent.Refresh -> loadTask(state.value.taskId, isRefresh = true)
            is TaskDetailEvent.Retry -> loadTask(state.value.taskId)
            is TaskDetailEvent.ChangeColumn -> changeColumn(event.newColumn)
            is TaskDetailEvent.ClearMessage -> updateState { it.copy(messageRes = null) }
        }
    }

    private fun loadTask(taskId: String, isRefresh: Boolean = false) {
        if (taskId.isBlank()) return
        updateState {
            if (isRefresh && it.task != null) {
                it.copy(errorRes = null, isConflict = false)
            } else {
                it.copy(isLoading = true, errorRes = null, isConflict = false)
            }
        }
        viewModelScope.launch {
            when (val result = taskRepository.getTask(taskId)) {
                is AppResult.Success -> {
                    val task = result.value
                    val machines = (machineRepository.getMachines() as? AppResult.Success)?.value.orEmpty()
                    val machine = machines.find { it.id == task.machineId }
                    updateState {
                        it.copy(
                            isLoading = false,
                            task = task,
                            machine = machine,
                            machines = machines,
                            errorRes = null,
                            isConflict = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            errorRes = appErrorToStringRes(result.error),
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
                            errorRes = null,
                            messageRes = R.string.board_task_moved,
                        )
                    }
                }
                is AppResult.Failure -> {
                    if (result.error is AppError.PreconditionFailed) {
                        updateState {
                            it.copy(
                                isUpdating = false,
                                isConflict = true,
                                errorRes = R.string.board_conflict_message,
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                isUpdating = false,
                                errorRes = appErrorToStringRes(result.error),
                            )
                        }
                    }
                }
            }
        }
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
    RefreshOnReturn { viewModel.onEvent(TaskDetailEvent.Refresh) }
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.messageRes) {
        state.messageRes?.let { msgRes ->
            snackbarHostState.showSnackbar(context.getString(msgRes))
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
                state.errorRes != null && state.task == null && !state.isConflict -> {
                    ErrorState(
                        message = stringResource(state.errorRes),
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

        if (task.fromMessageId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = stringResource(R.string.board_task_from_chat),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

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

        AssigneesSection(
            task = task,
            users = state.users,
            isForeman = state.isForeman,
            currentUserId = state.currentUserId,
            currentUserName = state.currentUserName,
        )
        Spacer(modifier = Modifier.height(16.dp))

        DueAndMachineCard(
            task = task,
            machine = state.machine,
            lang = state.currentUserLang,
            onMachineClick = { task.machineId?.let(onNavigateToMachine) },
        )
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
private fun AssigneesSection(
    task: Task,
    users: List<User>,
    isForeman: Boolean,
    currentUserId: String,
    currentUserName: String,
) {
    val assigneeLabels = resolveAssigneeDisplayNames(
        assigneeIds = task.assignees,
        isForeman = isForeman,
        currentUserId = currentUserId,
        currentUserName = currentUserName,
        users = users,
    )
    if (assigneeLabels.isEmpty()) return
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
        assigneeLabels.forEach { label ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Avatar(name = label, size = 32.dp)
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
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
                supportingContent = { Text(pick(machine.name, lang).ifBlank { machine.code }) },
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
