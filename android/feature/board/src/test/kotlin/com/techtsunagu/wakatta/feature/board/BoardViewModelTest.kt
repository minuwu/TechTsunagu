package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.board.data.FakeTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var languageManager: TestLanguageManager
    private lateinit var viewModel: BoardViewModel
    private val noWorkers = object : com.techtsunagu.wakatta.core.model.WorkerRepository {
        override suspend fun getWorkers() = AppResult.Success(emptyList<WorkerInfo>())
    }
    private val noMachines = object : com.techtsunagu.wakatta.core.model.MachineRepository {
        override suspend fun getMachines() = AppResult.Success(emptyList<com.techtsunagu.wakatta.core.model.Machine>())
        override suspend fun getMachine(id: String) = AppResult.Failure(AppError.NotFound)
        override suspend fun scan(machineId: String) = AppResult.Success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository(delayMs = 0L)
        sessionStore = TestSessionStore()
        languageManager = TestLanguageManager("ja")
        viewModel = BoardViewModel(repository, sessionStore, noWorkers, noMachines, languageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsTasksForForeman() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertNull(state.errorRes)
        assertTrue(state.isForeman)
        assertEquals("ja", state.currentUserLang)
        assertEquals(3, state.todoTasks.size)
        assertEquals(1, state.doingTasks.size)
        assertEquals(1, state.doneTasks.size)
        assertEquals(5, state.totalTasks)
    }

    @Test
    fun stateDefaultsAreEmptyListsAndEmptyStrings() {
        val defaultState = BoardUiState()
        assertEquals("", defaultState.currentUserId)
        assertEquals("", defaultState.currentUserName)
        assertEquals("", defaultState.currentUserLang)
        assertFalse(defaultState.isForeman)
        assertTrue(defaultState.todoTasks.isEmpty())
        assertTrue(defaultState.doingTasks.isEmpty())
        assertTrue(defaultState.doneTasks.isEmpty())
        assertTrue(defaultState.machines.isEmpty())
        assertTrue(defaultState.users.isEmpty())
        assertTrue(defaultState.sheetUsers.isEmpty())
        assertTrue(defaultState.sheetMachines.isEmpty())
    }

    @Test
    fun workerSessionUpdatesUiState() = runTest {
        advanceUntilIdle()
        sessionStore.setWorkerSession()
        languageManager.setLanguage("bn")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isForeman)
        assertEquals("bn", state.currentUserLang)
        assertEquals("u_rahim", state.currentUserId)
        assertEquals("Rahim Uddin", state.currentUserName)
    }

    @Test
    fun refreshUpdatesTasks() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(BoardEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isRefreshing)
        assertEquals(5, state.totalTasks)
    }

    @Test
    fun selectColumnPageUpdatesSelectedPage() = runTest {
        advanceUntilIdle()
        assertEquals(0, viewModel.state.value.selectedColumnPage)

        viewModel.onEvent(BoardEvent.SelectColumnPage(1))
        assertEquals(1, viewModel.state.value.selectedColumnPage)

        viewModel.onEvent(BoardEvent.SelectColumnPage(2))
        assertEquals(2, viewModel.state.value.selectedColumnPage)
    }

    @Test
    fun openAndCloseAddTaskSheet() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isAddTaskSheetOpen)

        viewModel.onEvent(BoardEvent.OpenAddTaskSheet)
        assertTrue(viewModel.state.value.isAddTaskSheetOpen)

        viewModel.onEvent(BoardEvent.CloseAddTaskSheet)
        assertFalse(viewModel.state.value.isAddTaskSheetOpen)
    }

    @Test
    fun submitNewTaskAddsTaskAndClosesSheet() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(BoardEvent.OpenAddTaskSheet)
        assertTrue(viewModel.state.value.isAddTaskSheetOpen)

        viewModel.onEvent(
            BoardEvent.SubmitNewTask(
                titleText = "新規プレス点検",
                assignees = listOf("u_rahim"),
                dueAt = "Today",
                machineId = "mc2"
            )
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isAddTaskSheetOpen)
        assertFalse(state.isSubmittingTask)
        assertEquals(6, state.totalTasks)
        assertEquals(4, state.todoTasks.size)
        assertEquals(R.string.board_task_added, state.messageRes)
    }

    @Test
    fun moveTaskUpdatesColumn() = runTest {
        advanceUntilIdle()
        val todoBefore = viewModel.state.value.todoTasks.find { it.id == "k3" }
        assertNotNull(todoBefore)

        viewModel.onEvent(BoardEvent.MoveTask("k3", TaskColumn.DOING))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.todoTasks.find { it.id == "k3" })
        assertNotNull(state.doingTasks.find { it.id == "k3" })
    }

    @Test
    fun moveTaskWithConflictHandlesPreconditionFailed() = runTest {
        advanceUntilIdle()
        // Simulate someone else updating k2 version in the background
        repository.moveTask("k2", TaskColumn.DONE, null, null, 1)

        // ViewModel tries to move k2 assuming old version 1
        viewModel.onEvent(BoardEvent.MoveTask("k2", TaskColumn.TODO))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(R.string.board_conflict_reloading, state.messageRes)
    }

    @Test
    fun errorOnLoadShowsErrorAndRetryRecovers() = runTest {
        advanceUntilIdle()
        repository.simulateError(AppError.Offline)

        viewModel.onEvent(BoardEvent.Retry)
        advanceUntilIdle()

        val errorState = viewModel.state.value
        assertNotNull(errorState.errorRes)
        assertEquals(R.string.board_error_offline, errorState.errorRes)

        // Clear error and retry
        repository.simulateError(null)
        viewModel.onEvent(BoardEvent.Retry)
        advanceUntilIdle()

        val recoveredState = viewModel.state.value
        assertNull(recoveredState.errorRes)
        assertEquals(5, recoveredState.totalTasks)
    }

    @Test
    fun clearMessageResetsMessageInfo() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(BoardEvent.SubmitNewTask("Test", emptyList(), null))
        advanceUntilIdle()
        assertEquals(R.string.board_task_added, viewModel.state.value.messageRes)

        viewModel.onEvent(BoardEvent.ClearMessage)
        assertNull(viewModel.state.value.messageRes)
    }

    @Test
    fun workerViewOfAssigneesShowsOwnNameAndPlusNForOthers() {
        val result = resolveAssigneeDisplayNames(
            assigneeIds = listOf("u_rahim", "3", "4"),
            isForeman = false,
            currentUserId = "u_rahim",
            currentUserName = "Rahim Uddin",
            users = emptyList(),
        )
        assertEquals(listOf("Rahim Uddin", "+2"), result)
        // Verify an ID is never shown as a name
        assertFalse(result.contains("3"))
        assertFalse(result.contains("4"))
    }

    @Test
    fun workerViewOfAssigneesWithoutSelfShowsOnlyPlusN() {
        val result = resolveAssigneeDisplayNames(
            assigneeIds = listOf("3", "4", "5"),
            isForeman = false,
            currentUserId = "u_rahim",
            currentUserName = "Rahim Uddin",
            users = emptyList(),
        )
        assertEquals(listOf("+3"), result)
        assertFalse(result.contains("3"))
        assertFalse(result.contains("4"))
        assertFalse(result.contains("5"))
    }

    @Test
    fun foremanViewOfAssigneesShowsResolvedDisplayNames() {
        val users = listOf(
            User("1", "Rahim Uddin", Role.WORKER, "bn"),
            User("2", "Nguyen Van", Role.WORKER, "vi")
        )
        val result = resolveAssigneeDisplayNames(
            assigneeIds = listOf("1", "2"),
            isForeman = true,
            currentUserId = "u_tanaka",
            currentUserName = "Tanaka",
            users = users,
        )
        assertEquals(listOf("Rahim Uddin", "Nguyen Van"), result)
        assertFalse(result.contains("1"))
        assertFalse(result.contains("2"))
    }
}
