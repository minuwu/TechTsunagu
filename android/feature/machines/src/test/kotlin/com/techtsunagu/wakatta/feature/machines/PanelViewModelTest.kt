package com.techtsunagu.wakatta.feature.machines

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.feature.machines.data.FakePanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateResult
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PanelViewModelTest {

    /** The Fake repository with switches for the network: fail every poll, or hang the answer. */
    private class FlakyPanelRepository(val inner: FakePanelRepository) : PanelRepository by inner {
        var failWith: AppError? = null
        var hang: CompletableDeferred<Unit>? = null
        var failOverrideWith: AppError? = null
        val overrideKeys = mutableListOf<String>()

        override suspend fun getPanelState(machineId: String, ifNoneMatch: String?): AppResult<PanelStateResult> {
            hang?.await()
            failWith?.let { return AppResult.Failure(it) }
            return inner.getPanelState(machineId, ifNoneMatch)
        }

        override suspend fun requestOverride(
            machineId: String,
            workerId: String?,
            minutes: Int,
            reason: String,
            idempotencyKey: String,
        ): AppResult<PanelOverrideDto> {
            overrideKeys += idempotencyKey
            failOverrideWith?.let { return AppResult.Failure(it) }
            return inner.requestOverride(machineId, workerId, minutes, reason, idempotencyKey)
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fake: FakePanelRepository
    private lateinit var panelRepository: FlakyPanelRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var viewModel: PanelViewModel
    private var nowMs = 1_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fake = FakePanelRepository(delayMs = 0L)
        panelRepository = FlakyPanelRepository(fake)
        sessionStore = TestSessionStore()
        viewModel = PanelViewModel(panelRepository, sessionStore) { nowMs }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun tick() {
        viewModel.onEvent(PanelEvent.PollTick)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    private fun loadReadyMachine() {
        viewModel.onEvent(PanelEvent.Init("mc1"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(PanelState.READY, viewModel.state.value.state)
    }

    @Test
    fun initLoadsPanelStateReadyForMc1() = runTest {
        viewModel.onEvent(PanelEvent.Init("mc1"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("mc1", state.machineId)
        assertEquals(PanelState.READY, state.state)
        assertTrue(state.isUserSignedOff)
        assertNotNull(state.lastEtag)
    }

    @Test
    fun initLoadsPanelStateLockedForMc2() = runTest {
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("mc2", state.machineId)
        assertEquals(PanelState.LOCKED, state.state)
        assertFalse(state.isUserSignedOff)
    }

    @Test
    fun pollTickHandles304UnmodifiedEtag() = runTest {
        loadReadyMachine()
        val initialEtag = viewModel.state.value.lastEtag
        assertNotNull(initialEtag)

        tick()

        assertEquals(initialEtag, viewModel.state.value.lastEtag)
        assertEquals(PanelState.READY, viewModel.state.value.state)
    }

    // ---- Finding 1: the Panel must never show a stale Ready ----

    @Test
    fun twoFailedPollsAfterSuccessGiveUnknown() = runTest {
        loadReadyMachine()
        panelRepository.failWith = AppError.Server

        tick()
        assertEquals("one failure alone keeps the last state", PanelState.READY, viewModel.state.value.state)

        tick()
        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)
    }

    @Test
    fun offlineOrTimeoutGivesUnknownAtOnce() = runTest {
        loadReadyMachine()
        panelRepository.failWith = AppError.Offline
        tick()
        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)

        panelRepository.failWith = null
        tick()
        assertEquals(PanelState.READY, viewModel.state.value.state)

        panelRepository.failWith = AppError.Timeout
        tick()
        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)
    }

    @Test
    fun aSuccessBetweenFailuresResetsTheFailureCount() = runTest {
        loadReadyMachine()
        panelRepository.failWith = AppError.Server
        tick()
        panelRepository.failWith = null
        tick()
        panelRepository.failWith = AppError.Server
        tick()

        assertEquals(PanelState.READY, viewModel.state.value.state)
    }

    @Test
    fun laterSuccessShowsTheRealState() = runTest {
        loadReadyMachine()
        panelRepository.failWith = AppError.Offline
        tick()
        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)

        // While we were offline the server state changed to an Override.
        fake.requestOverride("mc1", null, 60, "Urgent production quota", "server-side")
        panelRepository.failWith = null
        tick()

        assertEquals(PanelState.OVERRIDE, viewModel.state.value.state)
        assertNotNull(viewModel.state.value.activeOverride)
    }

    @Test
    fun notModifiedAfterFailureRecovers() = runTest {
        loadReadyMachine()
        panelRepository.failWith = AppError.Server
        tick()
        tick()
        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)

        panelRepository.failWith = null
        tick() // the Fake answers 304 here: the ETag still matches

        assertEquals(PanelState.READY, viewModel.state.value.state)
    }

    @Test
    fun dataOlderThanTenSecondsIsUnknownEvenWhileThePollIsStillWaiting() = runTest {
        loadReadyMachine()
        panelRepository.hang = CompletableDeferred()
        nowMs += 11_000L

        tick()

        assertEquals(PanelState.UNKNOWN, viewModel.state.value.state)
    }

    @Test
    fun dataYoungerThanTenSecondsStaysReadyWhileThePollIsWaiting() = runTest {
        loadReadyMachine()
        panelRepository.hang = CompletableDeferred()
        nowMs += 9_000L

        tick()

        assertEquals(PanelState.READY, viewModel.state.value.state)
    }

    @Test
    fun noPollingBeforeTheFirstLoadSucceeded() = runTest {
        viewModel.onEvent(PanelEvent.PollTick)
        advanceUntilIdle()

        assertEquals(PanelState.LOCKED, viewModel.state.value.state)
        assertTrue(viewModel.state.value.machineId.isEmpty())
    }

    // ---- Other behaviour ----

    @Test
    fun navigationEffectsEmitted() = runTest {
        loadReadyMachine()

        var effectReceived: PanelEffect? = null
        val job = launch {
            viewModel.effects.collect { effectReceived = it }
        }

        viewModel.onEvent(PanelEvent.OpenNote)
        advanceUntilIdle()
        assertEquals(PanelEffect.NavigateToNote("mc1"), effectReceived)

        viewModel.onEvent(PanelEvent.OpenNearMiss)
        advanceUntilIdle()
        assertEquals(PanelEffect.NavigateToNearMiss("mc1"), effectReceived)

        job.cancel()
    }

    @Test
    fun remindWorkerUsesTheNameFromThePanelAndAStringResource() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()

        viewModel.onEvent(PanelEvent.RemindWorker("u_rahim"))

        assertEquals(PanelMessage(R.string.machines_panel_reminded_toast, "Rahim Uddin"), viewModel.state.value.toastMessage)
        viewModel.onEvent(PanelEvent.DismissToast)
        assertNull(viewModel.state.value.toastMessage)
    }

    @Test
    fun shortOverrideReasonGivesAStringResourceError() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()

        viewModel.onEvent(PanelEvent.OverrideReasonChanged("no"))
        viewModel.onEvent(PanelEvent.RequestOverrideConfirm)

        assertEquals(R.string.machines_panel_override_reason_error, viewModel.state.value.overrideReasonErrorRes)
        assertFalse(viewModel.state.value.showOverrideConfirmDialog)
    }

    @Test
    fun foremanOverrideFlowAndRevoke() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()

        assertEquals(PanelState.LOCKED, viewModel.state.value.state)

        viewModel.onEvent(PanelEvent.OpenOverrideSheet)
        assertTrue(viewModel.state.value.showOverrideSheet)

        viewModel.onEvent(PanelEvent.OverrideWorkerSelected("u_rahim"))
        viewModel.onEvent(PanelEvent.OverrideMinutesChanged(120))
        viewModel.onEvent(PanelEvent.OverrideReasonChanged("Urgent production quota"))

        viewModel.onEvent(PanelEvent.RequestOverrideConfirm)
        assertTrue(viewModel.state.value.showOverrideConfirmDialog)

        viewModel.onEvent(PanelEvent.ConfirmOverride)
        advanceUntilIdle()

        val stateAfterOverride = viewModel.state.value
        assertFalse(stateAfterOverride.showOverrideConfirmDialog)
        assertFalse(stateAfterOverride.showOverrideSheet)
        assertEquals(PanelState.OVERRIDE, stateAfterOverride.state)
        assertNotNull(stateAfterOverride.activeOverride)
        assertEquals(PanelMessage(R.string.machines_panel_override_authorized), stateAfterOverride.toastMessage)

        viewModel.onEvent(PanelEvent.EndOverride)
        advanceUntilIdle()

        val stateAfterRevoke = viewModel.state.value
        assertEquals(PanelState.LOCKED, stateAfterRevoke.state)
        assertNull(stateAfterRevoke.activeOverride)
    }

    @Test
    fun failedOverrideKeepsTheSheetAndRetryReusesTheKey() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()
        viewModel.onEvent(PanelEvent.OpenOverrideSheet)
        viewModel.onEvent(PanelEvent.OverrideReasonChanged("Urgent production quota"))

        panelRepository.failOverrideWith = AppError.Offline
        viewModel.onEvent(PanelEvent.ConfirmOverride)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.showOverrideSheet)
        assertNull(viewModel.state.value.error)
        assertEquals(PanelMessage(R.string.machines_error_offline), viewModel.state.value.toastMessage)

        panelRepository.failOverrideWith = null
        viewModel.onEvent(PanelEvent.ConfirmOverride)
        advanceUntilIdle()

        assertEquals(2, panelRepository.overrideKeys.size)
        assertEquals(panelRepository.overrideKeys[0], panelRepository.overrideKeys[1])
        assertEquals(PanelState.OVERRIDE, viewModel.state.value.state)
    }

    @Test
    fun changedOverrideInputGetsANewKey() = runTest {
        sessionStore.setForemanSession()
        viewModel.onEvent(PanelEvent.Init("mc2"))
        advanceUntilIdle()
        viewModel.onEvent(PanelEvent.OverrideReasonChanged("Urgent production quota"))
        panelRepository.failOverrideWith = AppError.Timeout

        viewModel.onEvent(PanelEvent.ConfirmOverride)
        advanceUntilIdle()
        viewModel.onEvent(PanelEvent.OverrideMinutesChanged(30))
        viewModel.onEvent(PanelEvent.ConfirmOverride)
        advanceUntilIdle()

        assertNotEquals(panelRepository.overrideKeys[0], panelRepository.overrideKeys[1])
    }

    @Test
    fun loadFailureKeepsTheTypedError() = runTest {
        panelRepository.failWith = AppError.Offline
        viewModel.onEvent(PanelEvent.Init("mc1"))
        advanceUntilIdle()

        assertEquals(AppError.Offline, viewModel.state.value.error)
    }

    @Test
    fun viewModelHasOneInjectConstructorAndNoFakes() {
        val injectConstructors = PanelViewModel::class.java.constructors.filter {
            it.isAnnotationPresent(javax.inject.Inject::class.java)
        }
        assertEquals(1, injectConstructors.size)
    }
}
