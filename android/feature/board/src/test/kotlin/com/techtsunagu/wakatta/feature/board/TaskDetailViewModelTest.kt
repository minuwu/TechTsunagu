package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
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
class TaskDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTaskRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var languageManager: TestLanguageManager
    private lateinit var viewModel: TaskDetailViewModel

    private val sampleMachine = Machine(
        id = "mc3",
        code = "press-3",
        name = mapOf("ja" to "3号機", "en" to "Machine 3"),
        currentVersionId = "v1",
        isReady = true,
    )

    private val testMachines = object : MachineRepository {
        override suspend fun getMachines() = AppResult.Success(listOf(sampleMachine))
        override suspend fun getMachine(id: String): AppResult<MachineDetail> = AppResult.Failure(AppError.NotFound)
        override suspend fun scan(machineId: String) = AppResult.Success(Unit)
    }

    private val testWorkers = object : WorkerRepository {
        override suspend fun getWorkers() = AppResult.Success(
            listOf(
                WorkerInfo(id = "u_siti", displayName = "Siti Rahma", language = "id"),
                WorkerInfo(id = "u_nguyen", displayName = "Nguyen Van", language = "vi"),
                WorkerInfo(id = "u_rahim", displayName = "Rahim Uddin", language = "bn"),
            )
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository(delayMs = 0L)
        sessionStore = TestSessionStore()
        languageManager = TestLanguageManager("ja")
        viewModel = TaskDetailViewModel(repository, sessionStore, testMachines, testWorkers, languageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsTaskWithDetails() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.errorRes)
        assertFalse(state.isConflict)
        assertNotNull(state.task)
        assertEquals("k1", state.task!!.id)
        assertEquals(TaskColumn.DONE, state.task!!.column)

        // Machine details resolved via MachineRepository
        assertNotNull(state.machine)
        assertEquals("mc3", state.machine!!.id)
    }

    @Test
    fun initWithInvalidIdShowsError() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("missing_id"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.task)
        assertEquals(R.string.board_error_not_found, state.errorRes)
    }

    @Test
    fun changeColumnUpdatesTaskAndIncrementsVersion() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k3"))
        advanceUntilIdle()
        assertEquals(TaskColumn.TODO, viewModel.state.value.task!!.column)

        viewModel.onEvent(TaskDetailEvent.ChangeColumn(TaskColumn.DOING))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isUpdating)
        assertFalse(state.isConflict)
        assertNotNull(state.task)
        assertEquals(TaskColumn.DOING, state.task!!.column)
        assertEquals(2, state.task!!.version)
        assertEquals(R.string.board_task_moved, state.messageRes)
    }

    @Test
    fun changeColumnWithStaleVersionTriggersConflictState() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k2"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.task!!.version)

        // Simulate concurrent modification in repository
        repository.moveTask("k2", TaskColumn.DONE, null, null, 1)

        // ViewModel attempts to change column using stale version 1
        viewModel.onEvent(TaskDetailEvent.ChangeColumn(TaskColumn.TODO))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isUpdating)
        assertTrue(state.isConflict)
        assertEquals(R.string.board_conflict_message, state.errorRes)
    }

    @Test
    fun refreshAfterConflictReloadsLatestVersion() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k2"))
        advanceUntilIdle()

        // Background change
        repository.moveTask("k2", TaskColumn.DONE, null, null, 1)

        // Trigger conflict
        viewModel.onEvent(TaskDetailEvent.ChangeColumn(TaskColumn.TODO))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isConflict)

        // User clicks refresh / reload
        viewModel.onEvent(TaskDetailEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isConflict)
        assertNull(state.errorRes)
        assertEquals(2, state.task!!.version)
        assertEquals(TaskColumn.DONE, state.task!!.column)
    }

    @Test
    fun clearMessageResetsMessageInfo() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k3"))
        advanceUntilIdle()
        viewModel.onEvent(TaskDetailEvent.ChangeColumn(TaskColumn.DOING))
        advanceUntilIdle()
        assertEquals(R.string.board_task_moved, viewModel.state.value.messageRes)

        viewModel.onEvent(TaskDetailEvent.ClearMessage)
        assertNull(viewModel.state.value.messageRes)
    }

    @Test
    fun retryReloadsTaskAfterFailure() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k1"))
        advanceUntilIdle()

        repository.simulateError(AppError.Offline)
        viewModel.onEvent(TaskDetailEvent.Retry)
        advanceUntilIdle()

        val errorState = viewModel.state.value
        assertEquals(R.string.board_error_offline, errorState.errorRes)

        repository.simulateError(null)
        viewModel.onEvent(TaskDetailEvent.Retry)
        advanceUntilIdle()

        val recoveredState = viewModel.state.value
        assertNull(recoveredState.errorRes)
        assertNotNull(recoveredState.task)
    }

    @Test
    fun workerViewOfAssigneesShowsOnlySelfAndOthersCount() {
        val assignees = listOf("u_rahim", "u_siti", "u_nguyen")
        val result = resolveAssigneeDisplayNames(
            assigneeIds = assignees,
            isForeman = false,
            currentUserId = "u_rahim",
            currentUserName = "Rahim Uddin",
            users = emptyList(),
        )
        assertEquals(listOf("Rahim Uddin", "+2"), result)
        // IDs are never displayed
        assertFalse(result.contains("u_siti"))
        assertFalse(result.contains("u_nguyen"))
    }
}
