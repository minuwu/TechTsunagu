package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SkillStatus
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
class MachinesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var machineRepository: TestMachineRepository
    private lateinit var signOffRepository: TestSignOffRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var viewModel: MachinesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        machineRepository = TestMachineRepository()
        signOffRepository = TestSignOffRepository()
        sessionStore = TestSessionStore()
        viewModel = MachinesViewModel(machineRepository, signOffRepository, TestWorkerRepository(), sessionStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateLoadsMachinesForWorker() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertNull(state.error)
        assertFalse(state.isForeman)
        assertEquals(3, state.machines.size)

        // mc1 has signoff in TestSignOffRepository -> should be ready
        val mc1 = state.machines.find { it.id == "mc1" }
        assertNotNull(mc1)
        assertTrue(mc1!!.isReady)
        assertEquals(SkillStatus.VALID, mc1.status)

        // mc2 has no signoff -> locked for worker
        val mc2 = state.machines.find { it.id == "mc2" }
        assertNotNull(mc2)
        assertFalse(mc2!!.isReady)
        assertTrue("a Worker list has no Worker chips", mc1.workerStatuses.isEmpty())
    }

    @Test
    fun foremanSessionLoadsMachinesWithLockedWorkerCounts() = runTest {
        sessionStore.setForemanSession()
        viewModel = MachinesViewModel(machineRepository, signOffRepository, TestWorkerRepository(), sessionStore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isForeman)
        assertEquals(3, state.machines.size)

        // Foreman sees machine readiness directly
        val mc1 = state.machines.find { it.id == "mc1" }
        assertTrue(mc1!!.isReady)

        // Verify locked workers count
        val mc2 = state.machines.find { it.id == "mc2" }
        assertNotNull(mc2)
        assertEquals(listOf("Rahim Uddin", "Nguyen Van An", "Siti Rahma"), mc2!!.workerStatuses.map { it.displayName })
        assertEquals(3, mc2.lockedWorkerCount)
        assertEquals(0, mc1.lockedWorkerCount)
    }

    @Test
    fun emptyMachinesSetsEmptyState() = runTest {
        machineRepository.machinesList = emptyList()
        viewModel = MachinesViewModel(machineRepository, signOffRepository, TestWorkerRepository(), sessionStore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.machines.isEmpty())
    }

    @Test
    fun failureSetsErrorState() = runTest {
        machineRepository.shouldFail = true
        viewModel = MachinesViewModel(machineRepository, signOffRepository, TestWorkerRepository(), sessionStore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(AppError.Server, state.error)
    }

    @Test
    fun retryReloadsMachines() = runTest {
        machineRepository.shouldFail = true
        viewModel = MachinesViewModel(machineRepository, signOffRepository, TestWorkerRepository(), sessionStore)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.error != null)

        machineRepository.shouldFail = false
        viewModel.onEvent(MachinesEvent.Retry)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.error)
        assertFalse(state.isLoading)
        assertEquals(3, state.machines.size)
    }

    @Test
    fun machineClickedEmitsNavigateToMachineEffect() = runTest {
        var effectReceived: MachinesEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(MachinesEvent.MachineClicked("mc2"))
        advanceUntilIdle()

        assertEquals(MachinesEffect.NavigateToMachine("mc2"), effectReceived)
        job.cancel()
    }

    @Test
    fun takeCheckClickedEmitsNavigateToCheckEffect() = runTest {
        var effectReceived: MachinesEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(MachinesEvent.TakeCheckClicked("mc2"))
        advanceUntilIdle()

        assertEquals(MachinesEffect.NavigateToCheck("mc2"), effectReceived)
        job.cancel()
    }

    @Test
    fun refreshPreservesExistingMachinesWithoutFullScreenLoading() = runTest {
        advanceUntilIdle()
        val initialMachines = viewModel.state.value.machines
        assertEquals(3, initialMachines.size)
        assertFalse(viewModel.state.value.isLoading)

        viewModel.onEvent(MachinesEvent.Refresh)
        // With existing machines, isLoading is NOT flipped to true
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(3, viewModel.state.value.machines.size)

        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(3, viewModel.state.value.machines.size)
    }

    @Test
    fun viewModelHasOnlyHiltInjectableConstructor() {
        val constructors = MachinesViewModel::class.java.constructors
        assertEquals(1, constructors.size)
        assertTrue(constructors.first().isAnnotationPresent(javax.inject.Inject::class.java))
    }
}
