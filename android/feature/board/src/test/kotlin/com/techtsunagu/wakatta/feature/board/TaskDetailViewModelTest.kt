package com.techtsunagu.wakatta.feature.board

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.TaskColumn
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
    private lateinit var viewModel: TaskDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTaskRepository(delayMs = 0L)
        sessionStore = TestSessionStore()
        viewModel = TaskDetailViewModel(repository, sessionStore)
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
        assertNull(state.error)
        assertFalse(state.isConflict)
        assertNotNull(state.task)
        assertEquals("k1", state.task!!.id)
        assertEquals(TaskColumn.DONE, state.task!!.column)

        // Machine details resolved
        assertNotNull(state.machine)
        assertEquals("mc3", state.machine!!.id)

        // Source message resolved
        assertNotNull(state.sourceMessageText)
        assertEquals("田中 健一", state.sourceMessageSender)
        assertEquals("08:02", state.sourceMessageAt)
        assertEquals("t_line2", state.sourceThreadId)
    }

    @Test
    fun initWithInvalidIdShowsError() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("missing_id"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.task)
        assertNotNull(state.error)
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
        assertEquals("Task moved", state.messageInfo)
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
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Conflict"))
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
        assertNull(state.error)
        assertEquals(2, state.task!!.version)
        assertEquals(TaskColumn.DONE, state.task!!.column)
    }

    @Test
    fun clickCommentsSetsMessageInfo() = runTest {
        viewModel.onEvent(TaskDetailEvent.ClickComments)
        advanceUntilIdle()

        assertEquals("Comments are not in the prototype", viewModel.state.value.messageInfo)
    }

    @Test
    fun clearMessageResetsMessageInfo() = runTest {
        viewModel.onEvent(TaskDetailEvent.ClickComments)
        advanceUntilIdle()
        assertEquals("Comments are not in the prototype", viewModel.state.value.messageInfo)

        viewModel.onEvent(TaskDetailEvent.ClearMessage)
        assertNull(viewModel.state.value.messageInfo)
    }

    @Test
    fun retryReloadsTaskAfterFailure() = runTest {
        viewModel.onEvent(TaskDetailEvent.Init("k1"))
        advanceUntilIdle()

        repository.simulateError(AppError.Offline)
        viewModel.onEvent(TaskDetailEvent.Retry)
        advanceUntilIdle()

        val errorState = viewModel.state.value
        assertNotNull(errorState.error)

        repository.simulateError(null)
        viewModel.onEvent(TaskDetailEvent.Retry)
        advanceUntilIdle()

        val recoveredState = viewModel.state.value
        assertNull(recoveredState.error)
        assertNotNull(recoveredState.task)
    }
}
