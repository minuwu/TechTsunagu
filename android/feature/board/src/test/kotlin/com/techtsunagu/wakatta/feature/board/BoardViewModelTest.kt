package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.feature.board.data.FakeTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private lateinit var viewModel: BoardViewModel
    private val noWorkers = object : com.techtsunagu.wakatta.core.model.WorkerRepository {
        override suspend fun getWorkers() = com.techtsunagu.wakatta.core.model.AppResult.Success(emptyList<com.techtsunagu.wakatta.core.model.WorkerInfo>())
    }
    private val noMachines = object : com.techtsunagu.wakatta.core.model.MachineRepository {
        override suspend fun getMachines() = com.techtsunagu.wakatta.core.model.AppResult.Success(emptyList<com.techtsunagu.wakatta.core.model.Machine>())
        override suspend fun getMachine(id: String) = com.techtsunagu.wakatta.core.model.AppResult.Failure(com.techtsunagu.wakatta.core.model.AppError.NotFound)
        override suspend fun scan(machineId: String) = com.techtsunagu.wakatta.core.model.AppResult.Success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository(delayMs = 0L)
        sessionStore = TestSessionStore()
        viewModel = BoardViewModel(repository, sessionStore, noWorkers, noMachines)
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
        assertNull(state.error)
        assertTrue(state.isForeman)
        assertEquals("ja", state.currentUserLang)
        assertEquals(3, state.todoTasks.size)
        assertEquals(1, state.doingTasks.size)
        assertEquals(1, state.doneTasks.size)
        assertEquals(5, state.totalTasks)
    }

    @Test
    fun workerSessionUpdatesUiState() = runTest {
        advanceUntilIdle()
        sessionStore.setWorkerSession()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isForeman)
        assertEquals("bn", state.currentUserLang)
        assertEquals("u_rahim", state.currentUserId)
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
                dueAt = "Today 16:00",
                machineId = "mc2"
            )
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isAddTaskSheetOpen)
        assertFalse(state.isSubmittingTask)
        assertEquals(6, state.totalTasks)
        assertEquals(4, state.todoTasks.size)
        assertEquals("Task added", state.messageInfo)
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
        assertNotNull(state.messageInfo)
        assertTrue(state.messageInfo!!.contains("Conflict"))
    }

    @Test
    fun errorOnLoadShowsErrorAndRetryRecovers() = runTest {
        advanceUntilIdle()
        repository.simulateError(AppError.Offline)

        viewModel.onEvent(BoardEvent.Retry)
        advanceUntilIdle()

        val errorState = viewModel.state.value
        assertNotNull(errorState.error)

        // Clear error and retry
        repository.simulateError(null)
        viewModel.onEvent(BoardEvent.Retry)
        advanceUntilIdle()

        val recoveredState = viewModel.state.value
        assertNull(recoveredState.error)
        assertEquals(5, recoveredState.totalTasks)
    }

    @Test
    fun clearMessageResetsMessageInfo() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(BoardEvent.SubmitNewTask("Test", emptyList(), null))
        advanceUntilIdle()
        assertEquals("Task added", viewModel.state.value.messageInfo)

        viewModel.onEvent(BoardEvent.ClearMessage)
        assertNull(viewModel.state.value.messageInfo)
    }

    @Test
    fun clickFilterEmitsShowSnackbarEffect() = runTest {
        advanceUntilIdle()
        var effectReceived: BoardEffect? = null
        val job = launch {
            effectReceived = viewModel.effects.first()
        }

        viewModel.onEvent(BoardEvent.ClickFilter)
        advanceUntilIdle()

        assertNotNull(effectReceived)
        assertTrue(effectReceived is BoardEffect.ShowSnackbar)
        job.cancel()
    }
}
