package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppError
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
class ScanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var machineRepository: TestMachineRepository
    private lateinit var qrScanner: TestQrScanner
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        machineRepository = TestMachineRepository()
        qrScanner = TestQrScanner()
        viewModel = ScanViewModel(machineRepository, qrScanner)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadsAvailableMachines() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isTorchOn)
        assertEquals(3, state.availableMachines.size)
    }

    @Test
    fun toggleTorchTogglesState() = runTest {
        assertFalse(viewModel.state.value.isTorchOn)

        viewModel.onEvent(ScanEvent.ToggleTorch)
        assertTrue(viewModel.state.value.isTorchOn)

        viewModel.onEvent(ScanEvent.ToggleTorch)
        assertFalse(viewModel.state.value.isTorchOn)
    }

    @Test
    fun manualCodeChangedUpdatesStateAndClearsError() = runTest {
        viewModel.onEvent(ScanEvent.ManualCodeChanged("lathe-1"))
        assertEquals("lathe-1", viewModel.state.value.manualCodeInput)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun submitValidManualCodeEmitsNavigateToPanelEffect() = runTest {
        advanceUntilIdle()
        var effectReceived: ScanEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(ScanEvent.ManualCodeChanged("lathe-1"))
        viewModel.onEvent(ScanEvent.SubmitManualCode)
        advanceUntilIdle()

        assertEquals(ScanEffect.NavigateToPanel("mc1"), effectReceived)
        job.cancel()
    }

    @Test
    fun submitInvalidManualCodeSetsError() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(ScanEvent.ManualCodeChanged("nonexistent-machine"))
        viewModel.onEvent(ScanEvent.SubmitManualCode)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(AppError.NotFound, state.error)

        viewModel.onEvent(ScanEvent.ClearError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun simulateScanEmitsNavigateToPanelEffect() = runTest {
        var effectReceived: ScanEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(ScanEvent.SimulateScan("mc2"))
        advanceUntilIdle()

        assertEquals(ScanEffect.NavigateToPanel("mc2"), effectReceived)
        job.cancel()
    }

    @Test
    fun qrCodeDetectedFromScannerEmitsNavigateToPanelEffect() = runTest {
        advanceUntilIdle()
        var effectReceived: ScanEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        qrScanner.resultsFlow.tryEmit("https://wakatta.app/m/mc3?s=shop1")
        advanceUntilIdle()

        assertEquals(ScanEffect.NavigateToPanel("mc3"), effectReceived)
        job.cancel()
    }

    @Test
    fun unknownQrPayloadSetsTypedValidationError() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(ScanEvent.QrCodeDetected("garbage-payload"))
        advanceUntilIdle()

        assertEquals(AppError.Validation(), viewModel.state.value.error)
    }

    @Test
    fun machineListFailureShowsErrorInsteadOfDemoMachines() = runTest {
        machineRepository.shouldFail = true
        viewModel = ScanViewModel(machineRepository, qrScanner)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.availableMachines.isEmpty())
        assertEquals(AppError.Server, viewModel.state.value.error)
    }

    @Test
    fun viewModelHasOnlyHiltInjectableConstructor() {
        val constructors = ScanViewModel::class.java.constructors
        assertEquals(1, constructors.size)
        assertTrue(constructors.first().isAnnotationPresent(javax.inject.Inject::class.java))
    }
}
