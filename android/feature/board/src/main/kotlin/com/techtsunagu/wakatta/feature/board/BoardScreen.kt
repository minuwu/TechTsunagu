package com.techtsunagu.wakatta.feature.board

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
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
import com.techtsunagu.wakatta.feature.board.data.TaskCreator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class BoardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val todoTasks: List<Task> = emptyList(),
    val doingTasks: List<Task> = emptyList(),
    val doneTasks: List<Task> = emptyList(),
    @StringRes val errorRes: Int? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserLang: String = "",
    val isAddTaskSheetOpen: Boolean = false,
    val isSubmittingTask: Boolean = false,
    val selectedColumnPage: Int = 0,
    @StringRes val messageRes: Int? = null,
    val users: List<User> = emptyList(),
    val machines: List<Machine> = emptyList(),
    val sheetUsers: List<User> = emptyList(),
    val sheetMachines: List<Machine> = emptyList(),
) {
    val totalTasks: Int get() = todoTasks.size + doingTasks.size + doneTasks.size
    val isEmpty: Boolean get() = totalTasks == 0
}

sealed interface BoardEvent {
    data object LoadTasks : BoardEvent
    data object Refresh : BoardEvent
    data object Retry : BoardEvent
    data class SelectColumnPage(val page: Int) : BoardEvent
    data class MoveTask(val taskId: String, val targetColumn: TaskColumn) : BoardEvent
    data object OpenAddTaskSheet : BoardEvent
    data object CloseAddTaskSheet : BoardEvent
    data class SubmitNewTask(
        val titleText: String,
        val assignees: List<String>,
        val dueAt: String?,
        val machineId: String? = null,
    ) : BoardEvent
    data object ClearMessage : BoardEvent
}

sealed interface BoardEffect {
    data class ShowSnackbar(val message: String) : BoardEffect
}

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val sessionStore: SessionStore,
    private val workerRepository: WorkerRepository,
    private val machineRepository: MachineRepository,
    private val languageManager: LanguageManager,
) : MviViewModel<BoardUiState, BoardEvent, BoardEffect>(BoardUiState()) {

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
                    loadForemanUsers()
                }
            }
        }
        viewModelScope.launch {
            languageManager.currentLanguage.collect { lang ->
                updateState { it.copy(currentUserLang = lang) }
            }
        }
        loadTasks()
        loadMachines()
    }

    private fun loadMachines() {
        viewModelScope.launch {
            val machines = (machineRepository.getMachines() as? AppResult.Success)?.value.orEmpty()
            updateState { it.copy(machines = machines, sheetMachines = machines) }
        }
    }

    /** Foreman only: a Worker may not list accounts; a Worker's own card is assigned to them. */
    private fun loadForemanUsers() {
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
            updateState { it.copy(users = allUsers, sheetUsers = allUsers) }
        }
    }

    override fun onEvent(event: BoardEvent) {
        when (event) {
            is BoardEvent.LoadTasks -> loadTasks()
            is BoardEvent.Refresh -> refreshTasks()
            is BoardEvent.Retry -> loadTasks()
            is BoardEvent.SelectColumnPage -> updateState { it.copy(selectedColumnPage = event.page) }
            is BoardEvent.MoveTask -> moveTask(event.taskId, event.targetColumn)
            is BoardEvent.OpenAddTaskSheet -> updateState { it.copy(isAddTaskSheetOpen = true) }
            is BoardEvent.CloseAddTaskSheet -> updateState { it.copy(isAddTaskSheetOpen = false) }
            is BoardEvent.SubmitNewTask -> submitNewTask(event.titleText, event.assignees, event.dueAt, event.machineId)
            is BoardEvent.ClearMessage -> updateState { it.copy(messageRes = null) }
        }
    }

    private fun loadTasks() {
        updateState { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            when (val result = taskRepository.getTasks()) {
                is AppResult.Success -> {
                    val all = result.value
                    updateState {
                        it.copy(
                            isLoading = false,
                            todoTasks = all.filter { t -> t.column == TaskColumn.TODO },
                            doingTasks = all.filter { t -> t.column == TaskColumn.DOING },
                            doneTasks = all.filter { t -> t.column == TaskColumn.DONE },
                            errorRes = null,
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

    private fun refreshTasks() {
        updateState { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            when (val result = taskRepository.getTasks()) {
                is AppResult.Success -> {
                    val all = result.value
                    updateState {
                        it.copy(
                            isRefreshing = false,
                            todoTasks = all.filter { t -> t.column == TaskColumn.TODO },
                            doingTasks = all.filter { t -> t.column == TaskColumn.DOING },
                            doneTasks = all.filter { t -> t.column == TaskColumn.DONE },
                            errorRes = null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isRefreshing = false, errorRes = appErrorToStringRes(result.error)) }
                }
            }
        }
    }

    private fun moveTask(taskId: String, targetColumn: TaskColumn) {
        val allTasks = state.value.todoTasks + state.value.doingTasks + state.value.doneTasks
        val task = allTasks.find { it.id == taskId } ?: return
        viewModelScope.launch {
            when (val result = taskRepository.moveTask(taskId, targetColumn, null, null, task.version)) {
                is AppResult.Success -> {
                    loadTasks()
                }
                is AppResult.Failure -> {
                    if (result.error is AppError.PreconditionFailed) {
                        updateState { it.copy(messageRes = R.string.board_conflict_reloading) }
                        loadTasks()
                    } else {
                        updateState { it.copy(errorRes = appErrorToStringRes(result.error)) }
                    }
                }
            }
        }
    }

    private fun submitNewTask(titleText: String, assignees: List<String>, dueAt: String?, machineId: String?) {
        updateState { it.copy(isSubmittingTask = true) }
        viewModelScope.launch {
            val tri = mapOf("ja" to titleText, "en" to titleText, "bn" to titleText)
            val effectiveAssignees = if (assignees.isEmpty()) {
                if (state.value.currentUserId.isNotBlank()) listOf(state.value.currentUserId) else emptyList()
            } else assignees
            val result = (taskRepository as? TaskCreator)
                ?.createTask(tri, effectiveAssignees, dueAt, machineId)
                ?: taskRepository.createTask(tri, effectiveAssignees, dueAt)

            when (result) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(
                            isSubmittingTask = false,
                            isAddTaskSheetOpen = false,
                            messageRes = R.string.board_task_added,
                        )
                    }
                    loadTasks()
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isSubmittingTask = false,
                            errorRes = appErrorToStringRes(result.error),
                        )
                    }
                }
            }
        }
    }
}

internal fun appErrorToStringRes(error: AppError): Int = when (error) {
    is AppError.Offline -> R.string.board_error_offline
    is AppError.Timeout -> R.string.board_error_timeout
    is AppError.PreconditionFailed -> R.string.board_error_conflict
    is AppError.NotFound -> R.string.board_error_not_found
    is AppError.Forbidden -> R.string.board_error_forbidden
    is AppError.Conflict -> R.string.board_error_conflict
    else -> R.string.board_error_generic
}

@Composable
fun BoardScreen(
    onTaskClick: (String) -> Unit = {},
    viewModel: BoardViewModel = viewModel(),
) {
    RefreshOnReturn { viewModel.onEvent(BoardEvent.Refresh) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    BoardContent(
        state = state,
        onEvent = viewModel::onEvent,
        onTaskClick = onTaskClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardContent(
    state: BoardUiState,
    onEvent: (BoardEvent) -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.messageRes) {
        state.messageRes?.let { msgRes ->
            snackbarHostState.showSnackbar(context.getString(msgRes))
            onEvent(BoardEvent.ClearMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.board_screen_title),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(BoardEvent.OpenAddTaskSheet) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.board_add_task),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading && state.isEmpty -> {
                    LoadingState(message = stringResource(R.string.board_loading))
                }
                state.errorRes != null && state.isEmpty -> {
                    ErrorState(
                        message = stringResource(state.errorRes),
                        onRetry = { onEvent(BoardEvent.Retry) },
                    )
                }
                else -> {
                    BoardPagerContent(
                        state = state,
                        onEvent = onEvent,
                        onTaskClick = onTaskClick,
                    )
                }
            }

            if (state.isAddTaskSheetOpen) {
                AddTaskBottomSheet(
                    sheetState = sheetState,
                    users = state.sheetUsers,
                    machines = state.sheetMachines,
                    lang = state.currentUserLang,
                    onDismissRequest = { onEvent(BoardEvent.CloseAddTaskSheet) },
                    onSubmit = { title, assignees, due, machineId ->
                        onEvent(BoardEvent.SubmitNewTask(title, assignees, due, machineId))
                    },
                )
            }
        }
    }
}

@Composable
private fun BoardPagerContent(
    state: BoardUiState,
    onEvent: (BoardEvent) -> Unit,
    onTaskClick: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = state.selectedColumnPage,
        pageCount = { 3 },
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.selectedColumnPage) {
            onEvent(BoardEvent.SelectColumnPage(pagerState.currentPage))
        }
    }

    LaunchedEffect(state.selectedColumnPage) {
        if (pagerState.currentPage != state.selectedColumnPage) {
            pagerState.animateScrollToPage(state.selectedColumnPage)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BoardColumnTabs(
            selectedPage = pagerState.currentPage,
            todoCount = state.todoTasks.size,
            doingCount = state.doingTasks.size,
            doneCount = state.doneTasks.size,
            onTabSelected = { page ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(page)
                }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            when (page) {
                0 -> KanbanColumnPage(
                    column = TaskColumn.TODO,
                    tasks = state.todoTasks,
                    lang = state.currentUserLang,
                    machines = state.machines,
                    users = state.users,
                    isForeman = state.isForeman,
                    currentUserId = state.currentUserId,
                    currentUserName = state.currentUserName,
                    onTaskClick = onTaskClick,
                    onMoveTask = { id, col -> onEvent(BoardEvent.MoveTask(id, col)) },
                )
                1 -> KanbanColumnPage(
                    column = TaskColumn.DOING,
                    tasks = state.doingTasks,
                    lang = state.currentUserLang,
                    machines = state.machines,
                    users = state.users,
                    isForeman = state.isForeman,
                    currentUserId = state.currentUserId,
                    currentUserName = state.currentUserName,
                    onTaskClick = onTaskClick,
                    onMoveTask = { id, col -> onEvent(BoardEvent.MoveTask(id, col)) },
                )
                2 -> KanbanColumnPage(
                    column = TaskColumn.DONE,
                    tasks = state.doneTasks,
                    lang = state.currentUserLang,
                    machines = state.machines,
                    users = state.users,
                    isForeman = state.isForeman,
                    currentUserId = state.currentUserId,
                    currentUserName = state.currentUserName,
                    onTaskClick = onTaskClick,
                    onMoveTask = { id, col -> onEvent(BoardEvent.MoveTask(id, col)) },
                )
            }
        }
    }
}

@Composable
private fun BoardColumnTabs(
    selectedPage: Int,
    todoCount: Int,
    doingCount: Int,
    doneCount: Int,
    onTabSelected: (Int) -> Unit,
) {
    TabRow(
        selectedTabIndex = selectedPage,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Tab(
            selected = selectedPage == 0,
            onClick = { onTabSelected(0) },
            text = { Text("${stringResource(R.string.board_col_todo)} ($todoCount)") },
        )
        Tab(
            selected = selectedPage == 1,
            onClick = { onTabSelected(1) },
            text = { Text("${stringResource(R.string.board_col_doing)} ($doingCount)") },
        )
        Tab(
            selected = selectedPage == 2,
            onClick = { onTabSelected(2) },
            text = { Text("${stringResource(R.string.board_col_done)} ($doneCount)") },
        )
    }
}
