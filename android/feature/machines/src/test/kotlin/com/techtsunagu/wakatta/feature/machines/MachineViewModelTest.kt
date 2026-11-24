package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.feature.machines.data.FakeShiftAssignmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class MachineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var machineRepository: TestMachineRepository
    private lateinit var signOffRepository: TestSignOffRepository
    private lateinit var shiftAssignmentRepository: FakeShiftAssignmentRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var viewModel: MachineViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        machineRepository = TestMachineRepository()
        signOffRepository = TestSignOffRepository()
        shiftAssignmentRepository = FakeShiftAssignmentRepository()
        sessionStore = TestSessionStore()
        viewModel = MachineViewModel(
            machineRepository = machineRepository,
            signOffRepository = signOffRepository,
            shiftAssignmentRepository = shiftAssignmentRepository,
            workerRepository = TestWorkerRepository(),
            taskRepository = TestTaskRepository(),
            sessionStore = sessionStore
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsMachineDetails() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("mc1", state.machineId)
        assertEquals("lathe-1", state.code)
        assertEquals("旋盤1号機", state.nameJa)
        assertTrue(state.isReady)
        assertEquals(listOf("t1"), state.tasks.map { it.id })
        assertEquals(listOf("Rahim Uddin", "Nguyen Van An", "Siti Rahma"), state.workers.map { it.displayName })
        assertTrue(state.workers.all { it.isReady })
        assertTrue(state.qrCodePayload.contains("/m/mc1?s="))
    }

    @Test
    fun machineWithoutSignOffIsLockedAndHasNoDemoDefault() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc2"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isReady)
        assertEquals(SkillStatus.NONE, state.skillStatus)
        assertTrue(state.tasks.none { it.id == "t1" })
    }

    @Test
    fun loadFailureSetsErrorState() = runTest {
        machineRepository.shouldFail = true
        viewModel.onEvent(MachineEvent.Init("unknown_machine"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.NotFound, state.error)
    }

    @Test
    fun retryReloadsMachine() = runTest {
        machineRepository.shouldFail = true
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)

        machineRepository.shouldFail = false
        viewModel.onEvent(MachineEvent.Retry)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.error)
        assertFalse(state.isLoading)
        assertEquals("mc1", state.machineId)
    }

    @Test
    fun openPanelEmitsNavigateToPanelEffect() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()

        var effectReceived: MachineEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(MachineEvent.OpenPanel)
        advanceUntilIdle()

        assertEquals(MachineEffect.NavigateToPanel("mc1"), effectReceived)
        job.cancel()
    }

    @Test
    fun openNoteEmitsNavigateToNoteEffect() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc2"))
        advanceUntilIdle()

        var effectReceived: MachineEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(MachineEvent.OpenNote)
        advanceUntilIdle()

        assertEquals(MachineEffect.NavigateToNote("mc2"), effectReceived)
        job.cancel()
    }

    @Test
    fun assignDialogToggleAndAssignWorker() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()

        viewModel.onEvent(MachineEvent.OpenAssignDialog)
        assertTrue(viewModel.state.value.showAssignDialog)

        viewModel.onEvent(MachineEvent.DismissAssignDialog)
        assertFalse(viewModel.state.value.showAssignDialog)

        // Assign worker
        viewModel.onEvent(MachineEvent.AssignWorker("u_rahim"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showAssignDialog)
        assertEquals("Rahim Uddin", state.assignedWorkerName)
    }

    @Test
    fun printQrClickedShowsAndDismissesToast() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()

        viewModel.onEvent(MachineEvent.PrintQrClicked)
        assertNotNull(viewModel.state.value.qrPrintedToastRes)

        viewModel.onEvent(MachineEvent.DismissToast)
        assertNull(viewModel.state.value.qrPrintedToastRes)
    }

    @Test
    fun refreshPreservesExistingDetailsWithoutFullScreenLoading() = runTest {
        viewModel.onEvent(MachineEvent.Init("mc1"))
        advanceUntilIdle()
        assertEquals("mc1", viewModel.state.value.machineId)
        assertEquals("lathe-1", viewModel.state.value.code)
        assertFalse(viewModel.state.value.isLoading)

        viewModel.onEvent(MachineEvent.Refresh)
        // With existing machine loaded, isLoading is NOT flipped to true
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("mc1", viewModel.state.value.machineId)

        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("mc1", viewModel.state.value.machineId)
    }

    @Test
    fun viewModelHasOnlyHiltInjectableConstructor() {
        val constructors = MachineViewModel::class.java.constructors
        assertEquals(1, constructors.size)
        assertTrue(constructors.first().isAnnotationPresent(javax.inject.Inject::class.java))
    }
}
