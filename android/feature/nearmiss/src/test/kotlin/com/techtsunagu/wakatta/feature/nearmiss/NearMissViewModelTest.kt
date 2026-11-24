package com.techtsunagu.wakatta.feature.nearmiss

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Task
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.feature.nearmiss.data.FakeNearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.data.QueuedNearMiss
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
class NearMissViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var nearMissRepository: FakeNearMissRepository
    private lateinit var machineRepository: TestMachineRepository
    private lateinit var workerRepository: TestWorkerRepository
    private lateinit var taskRepository: TestTaskRepository
    private lateinit var photoCapture: TestPhotoCapture
    private lateinit var outboxWriter: TestOutboxWriter
    private lateinit var sessionStore: TestSessionStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        nearMissRepository = FakeNearMissRepository(delayMs = 0L)
        machineRepository = TestMachineRepository()
        workerRepository = TestWorkerRepository()
        taskRepository = TestTaskRepository()
        photoCapture = TestPhotoCapture()
        outboxWriter = TestOutboxWriter()
        sessionStore = TestSessionStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- NearMissNewViewModel Tests ---

    @Test
    fun newViewModelInitLoadsMachinesAndRecentReports() = runTest {
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.Init("mc1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("mc1", state.machineId)
        assertEquals(3, state.machines.size)
        assertFalse(state.isForeman)
        assertTrue(state.recentReports.isNotEmpty())
    }

    @Test
    fun newViewModelSelectMachineAndText() = runTest {
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc2"))
        viewModel.onEvent(NearMissNewEvent.UpdateText("Oil spilled"))
        viewModel.onEvent(NearMissNewEvent.SelectSeverity(NearMissSeverity.HIGH))

        val state = viewModel.state.value
        assertEquals("mc2", state.machineId)
        assertEquals("Oil spilled", state.text)
        assertEquals(NearMissSeverity.HIGH, state.severity)
    }

    @Test
    fun changingTheMachineKeepsTheTextAndPhoto() = runTest { // R6
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )
        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc1"))
        viewModel.onEvent(NearMissNewEvent.UpdateText("Oil spilled"))
        viewModel.onEvent(NearMissNewEvent.AddPhoto(url = "https://example.com/photo.jpg"))

        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc2"))

        val state = viewModel.state.value
        assertEquals("mc2", state.machineId)
        assertEquals("Oil spilled", state.text)
        assertTrue(state.hasPhoto)
        assertEquals("https://example.com/photo.jpg", state.photoUrl)
    }

    @Test
    fun newViewModelPhotoAnnotation() = runTest {
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.AddPhoto(url = "https://example.com/photo.jpg"))
        var state = viewModel.state.value
        assertTrue(state.hasPhoto)
        assertEquals("https://example.com/photo.jpg", state.photoUrl)

        viewModel.onEvent(NearMissNewEvent.UpdatePhotoMark(70f, 30f))
        state = viewModel.state.value
        assertEquals(70f, state.photoMark.x)
        assertEquals(30f, state.photoMark.y)

        viewModel.onEvent(NearMissNewEvent.RemovePhoto)
        state = viewModel.state.value
        assertFalse(state.hasPhoto)
        assertNull(state.photoUrl)
    }

    @Test
    fun newViewModelSendReportOnlineSuccess() = runTest {
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc1"))
        viewModel.onEvent(NearMissNewEvent.UpdateText("A metal chip flew out"))
        viewModel.onEvent(NearMissNewEvent.SendReport)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSent)
        assertFalse(state.isQueuedOffline)
        assertFalse(state.isLoading)
    }

    @Test
    fun newViewModelSendReportOfflineQueuesToOutbox() = runTest {
        nearMissRepository.simulateError(AppError.Offline)
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc2"))
        viewModel.onEvent(NearMissNewEvent.UpdateText("Emergency \"jam\" clearing issue"))
        viewModel.onEvent(NearMissNewEvent.SendReport)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSent)
        assertTrue(state.isQueuedOffline)
        assertEquals(1, outboxWriter.enqueued.size)
        val (kind, _, payload) = outboxWriter.enqueued[0]
        assertEquals("nearmiss:${DemoSeed.demoWorker.id}", kind)
        assertEquals("Emergency \"jam\" clearing issue", QueuedNearMiss.decode(payload)?.text)

        // The same report, after a refusal by the server, comes back into a new form.
        outboxWriter.failed = payload
        nearMissRepository.simulateError(null)
        val next = NearMissNewViewModel(nearMissRepository, machineRepository, photoCapture, outboxWriter, sessionStore)
        next.onEvent(NearMissNewEvent.Refresh)
        advanceUntilIdle()
        assertTrue(next.state.value.sendFailed)
        assertEquals("mc2", next.state.value.machineId)
    }

    @Test
    fun newViewModelRefusedReportIsNotQueued() = runTest {
        nearMissRepository.simulateError(AppError.Validation())
        val viewModel = NearMissNewViewModel(nearMissRepository, machineRepository, photoCapture, outboxWriter, sessionStore)
        viewModel.onEvent(NearMissNewEvent.SelectMachine("mc2"))
        viewModel.onEvent(NearMissNewEvent.UpdateText("Report text"))
        viewModel.onEvent(NearMissNewEvent.SendReport)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.sendFailed)
        assertFalse(viewModel.state.value.isSent)
        assertTrue(outboxWriter.enqueued.isEmpty())
    }

    @Test
    fun newViewModelResetClearsForm() = runTest {
        val viewModel = NearMissNewViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            photoCapture = photoCapture,
            outboxWriter = outboxWriter,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissNewEvent.UpdateText("Some text"))
        viewModel.onEvent(NearMissNewEvent.Reset)

        val state = viewModel.state.value
        assertEquals("", state.text)
        assertFalse(state.isSent)
        assertFalse(state.hasPhoto)
    }

    // --- NearMissListViewModel Tests ---

    @Test
    fun listViewModelInitLoadsCountsAndFilters() = runTest {
        val viewModel = NearMissListViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissListEvent.Init)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.allReports.isNotEmpty())
        assertEquals(NearMissStatus.OPEN, state.filter)
        assertEquals(state.openCount, state.displayedReports.size)
    }

    @Test
    fun listViewModelSetFilterSwitchesOpenAndSeen() = runTest {
        val viewModel = NearMissListViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissListEvent.Init)
        advanceUntilIdle()

        viewModel.onEvent(NearMissListEvent.SetFilter(NearMissStatus.ACK))
        val state = viewModel.state.value
        assertEquals(NearMissStatus.ACK, state.filter)
        assertEquals(state.seenCount, state.displayedReports.size)
    }

    @Test
    fun listViewModelForemanSeesAllReports() = runTest {
        sessionStore.setForeman()
        val viewModel = NearMissListViewModel(
            nearMissRepository = nearMissRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissListEvent.Init)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isForeman)
        assertEquals(3, state.allReports.size)
    }

    // --- NearMissDetailViewModel Tests ---

    @Test
    fun detailViewModelInitLoadsReport() = runTest {
        val viewModel = NearMissDetailViewModel(
            nearMissRepository = nearMissRepository,
            taskRepository = taskRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissDetailEvent.Init("nm_s1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.report)
        assertEquals("nm_s1", state.report?.id)
        assertNotNull(state.machine)
        assertEquals("mc3", state.machine?.id)
    }

    @Test
    fun detailViewModelAcknowledgeMarksReportSeen() = runTest {
        sessionStore.setForeman()
        val viewModel = NearMissDetailViewModel(
            nearMissRepository = nearMissRepository,
            taskRepository = taskRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissDetailEvent.Init("nm_s1"))
        advanceUntilIdle()

        viewModel.onEvent(NearMissDetailEvent.Acknowledge)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(NearMissStatus.ACK, state.report?.status)
        assertNotNull(state.report?.acknowledgedAt)
    }

    @Test
    fun detailViewModelCreateTaskCreatesBoardCard() = runTest {
        sessionStore.setForeman()
        val viewModel = NearMissDetailViewModel(
            nearMissRepository = nearMissRepository,
            taskRepository = taskRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissDetailEvent.Init("nm_s1"))
        advanceUntilIdle()

        viewModel.onEvent(NearMissDetailEvent.CreateTask)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.report?.taskId)
        assertEquals(NearMissStatus.ACK, state.report?.status)
        assertEquals(1, taskRepository.createdTasks.size)
        assertEquals(R.string.nearmiss_task_created_toast, state.toastMessage)
    }

    @Test
    fun detailViewModelNotFoundSetsError() = runTest {
        val viewModel = NearMissDetailViewModel(
            nearMissRepository = nearMissRepository,
            taskRepository = taskRepository,
            machineRepository = machineRepository,
            workerRepository = workerRepository,
            sessionStore = sessionStore,
        )

        viewModel.onEvent(NearMissDetailEvent.Init("unknown_nm"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertNull(state.report)
    }
}

// --- Test Fakes ---

private class TestSessionStore(initialUser: User = DemoSeed.demoWorker) : SessionStore {
    private val _session = MutableStateFlow<Session?>(Session(token = "tok_test", user = initialUser))
    override val session: StateFlow<Session?> = _session.asStateFlow()
    override suspend fun save(session: Session) { _session.value = session }
    override suspend fun clear() { _session.value = null }
    override fun getCachedToken(): String? = _session.value?.token
    fun setForeman() { _session.value = Session(token = "tok_fm", user = DemoSeed.demoForeman) }
}

private class TestMachineRepository : MachineRepository {
    override suspend fun getMachines(): AppResult<List<Machine>> = AppResult.Success(DemoSeed.machines)
    override suspend fun getMachine(id: String): AppResult<MachineDetail> =
        AppResult.Success(MachineDetail(id = id, code = id, name = mapOf("ja" to "旋盤1", "en" to "Lathe 1")))
    override suspend fun scan(machineId: String): AppResult<Unit> = AppResult.Success(Unit)
}

private class TestWorkerRepository : WorkerRepository {
    override suspend fun getWorkers(): AppResult<List<WorkerInfo>> =
        AppResult.Success(DemoSeed.users.map { WorkerInfo(it.id, it.displayName, it.language) })
}

private class TestTaskRepository : TaskRepository {
    val createdTasks = mutableListOf<Task>()
    override suspend fun getTasks(): AppResult<List<Task>> = AppResult.Success(createdTasks)
    override suspend fun getTask(id: String): AppResult<Task> =
        createdTasks.find { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)
    override suspend fun createTask(title: Tri, assignees: List<String>, dueAt: String?): AppResult<Task> {
        val task = Task(id = "k_test_" + createdTasks.size, title = title, column = TaskColumn.TODO, assignees = assignees, dueAt = dueAt)
        createdTasks.add(task)
        return AppResult.Success(task)
    }
    override suspend fun moveTask(id: String, column: TaskColumn, assignees: List<String>?, dueAt: String?, version: Int): AppResult<Task> =
        AppResult.Failure(AppError.NotFound)
}

private class TestPhotoCapture : PhotoCapture {
    override suspend fun captureOrPick(): File? = null
}

private class TestOutboxWriter : OutboxWriter {
    val enqueued = mutableListOf<Triple<String, String, String>>()
    override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
        enqueued.add(Triple(kind, idempotencyKey, payloadJson))
    }
    override fun pendingCount(kind: String): Flow<Int> = emptyFlow()
    var failed: String? = null
    override suspend fun takeFailed(kind: String): String? = failed.also { failed = null }
}
