package com.techtsunagu.wakatta.feature.briefing

import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Receipts
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.briefing.data.FakeBriefingRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
class BriefingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var briefingRepository: FakeBriefingRepository
    private lateinit var testAudioRecorder: TestAudioRecorder
    private lateinit var testSpeaker: TestSpeaker
    private lateinit var testReceiptRepository: TestReceiptRepository
    private lateinit var testCheckRepository: TestCheckRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        briefingRepository = FakeBriefingRepository(delayMs = 0L)
        testAudioRecorder = TestAudioRecorder()
        testSpeaker = TestSpeaker()
        testReceiptRepository = TestReceiptRepository()
        testCheckRepository = TestCheckRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- BriefingNewViewModel Tests ---

    @Test
    fun briefingNew_initialState_loadsPastBriefings() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(BriefingNewPhase.IDLE, state.phase)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.pastBriefings.isNotEmpty())
    }

    @Test
    fun briefingNew_startAndStopRecording_createsDraft() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.StartRecording)
        assertEquals(BriefingNewPhase.RECORDING, vm.state.value.phase)

        vm.onEvent(BriefingNewEvent.StopRecording)
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(BriefingNewPhase.READY, state.phase)
        assertNotNull(state.draft)
    }

    @Test
    fun briefingNew_dictateInstead_processesDirectText() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.DictateInstead("入力した朝礼テキスト"))
        advanceUntilIdle()

        val state = vm.state.value
        assertEquals(BriefingNewPhase.READY, state.phase)
        assertEquals("入力した朝礼テキスト", state.draft?.transcriptJa)
    }

    @Test
    fun briefingNew_redoDraft_resetsToIdle() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.DictateInstead("テスト"))
        advanceUntilIdle()
        assertEquals(BriefingNewPhase.READY, vm.state.value.phase)

        vm.onEvent(BriefingNewEvent.RedoDraft)
        assertEquals(BriefingNewPhase.IDLE, vm.state.value.phase)
        assertNull(vm.state.value.draft)
    }

    @Test
    fun briefingNew_publishFlow_showsDialog_and_publishes() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.DictateInstead("テスト"))
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.RequestPublish)
        assertTrue(vm.state.value.showPublishConfirm)

        vm.onEvent(BriefingNewEvent.DismissPublishConfirm)
        assertFalse(vm.state.value.showPublishConfirm)

        vm.onEvent(BriefingNewEvent.RequestPublish)
        vm.onEvent(BriefingNewEvent.ConfirmPublish)
        advanceUntilIdle()

        assertFalse(vm.state.value.showPublishConfirm)
        assertFalse(vm.state.value.isPublishing)
        assertEquals(BriefingNewPhase.IDLE, vm.state.value.phase)
    }

    @Test
    fun briefingNew_retry_clearsErrorAndReloads() = runTest {
        val vm = BriefingNewViewModel(briefingRepository, testAudioRecorder)
        advanceUntilIdle()

        vm.onEvent(BriefingNewEvent.Retry)
        advanceUntilIdle()
        assertNull(vm.state.value.error)
    }

    // --- BriefingBoardViewModel Tests ---

    @Test
    fun briefingBoard_init_loadsBoardData() = runTest {
        val vm = BriefingBoardViewModel(briefingRepository)
        vm.onEvent(BriefingBoardEvent.Init("b_seed"))
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals("b_seed", state.id)
        assertNotNull(state.briefing)
        assertNotNull(state.receipts)
        assertTrue(state.workerStatuses.isNotEmpty())
    }

    @Test
    fun briefingBoard_sendReexplanation_triggersNotification() = runTest {
        val vm = BriefingBoardViewModel(briefingRepository)
        vm.onEvent(BriefingBoardEvent.Init("b_seed"))
        advanceUntilIdle()

        vm.onEvent(BriefingBoardEvent.SendReexplanation("u_nguyen", "Nguyen Van An"))
        advanceUntilIdle()

        assertEquals("Nguyen Van An", vm.state.value.snackbarMessage)

        vm.onEvent(BriefingBoardEvent.DismissSnackbar)
        assertNull(vm.state.value.snackbarMessage)
    }

    @Test
    fun briefingBoard_refresh_and_retry() = runTest {
        val vm = BriefingBoardViewModel(briefingRepository)
        vm.onEvent(BriefingBoardEvent.Init("b_today"))
        advanceUntilIdle()

        vm.onEvent(BriefingBoardEvent.Refresh)
        advanceUntilIdle()
        assertEquals("b_today", vm.state.value.id)

        vm.onEvent(BriefingBoardEvent.Retry)
        advanceUntilIdle()
        assertNull(vm.state.value.error)
    }

    // --- BriefingCardViewModel Tests ---

    @Test
    fun briefingCard_init_loadsWorkerBriefingAndMarksOpened() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Init("b_today"))
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.briefing)
        assertEquals("b_today", state.briefing?.id)
        assertTrue(testReceiptRepository.markedOpened.contains("b_today"))
    }

    @Test
    fun briefingCard_selectAnswer_correct_setsGreen() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Init("b_today"))
        advanceUntilIdle()

        vm.onEvent(BriefingCardEvent.SelectAnswer(0))
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.isAnswered)
        assertEquals(0, state.selectedOptionIndex)
        assertEquals(WakattaStatus.GREEN, state.status)
        assertEquals(true, state.isPassed)
    }

    @Test
    fun briefingCard_selectAnswer_notSure_setsOrange() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Init("b_today"))
        advanceUntilIdle()

        vm.onEvent(BriefingCardEvent.SelectAnswer(-1))
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.isAnswered)
        assertEquals(-1, state.selectedOptionIndex)
        assertEquals(WakattaStatus.ORANGE, state.status)
        assertEquals(false, state.isPassed)
    }

    @Test
    fun briefingCard_selectAnswer_wrong_setsRed() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Init("b_today"))
        advanceUntilIdle()

        vm.onEvent(BriefingCardEvent.SelectAnswer(1))
        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state.isAnswered)
        assertEquals(1, state.selectedOptionIndex)
        assertEquals(WakattaStatus.RED, state.status)
        assertEquals(false, state.isPassed)
    }

    @Test
    fun briefingCard_readAloud_and_stopSpeech() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Init("b_today"))
        advanceUntilIdle()

        vm.onEvent(BriefingCardEvent.ToggleReadAloud)
        assertTrue(vm.state.value.isSpeaking)
        assertNotNull(testSpeaker.spokenText)

        vm.onEvent(BriefingCardEvent.ToggleReadAloud)
        assertFalse(vm.state.value.isSpeaking)
        assertTrue(testSpeaker.stopped)
    }

    @Test
    fun briefingCard_done_emitsCloseEffect() = runTest {
        val vm = BriefingCardViewModel(
            briefingRepository = briefingRepository,
            receiptRepository = testReceiptRepository,
            checkRepository = testCheckRepository,
            speaker = testSpeaker,
        )
        vm.onEvent(BriefingCardEvent.Done)
        assertTrue(testSpeaker.stopped)
    }

    // --- Helpers / Fakes for testing ---

    private class TestAudioRecorder : AudioRecorder {
        override fun start(): Flow<Pair<Float, Int>> = flowOf(Pair(0.5f, 1))
        override suspend fun stop(): File? = null
    }

    private class TestSpeaker : Speaker {
        var spokenText: String? = null
        var stopped = false
        override fun speak(text: String, languageTag: String) {
            spokenText = text
        }
        override fun isAvailable(languageTag: String) = true
        override fun stop() {
            stopped = true
        }
    }

    private class TestReceiptRepository : ReceiptRepository {
        val markedOpened = mutableSetOf<String>()
        override suspend fun markOpened(targetKind: CheckTargetKind, targetId: String): AppResult<Unit> {
            markedOpened.add(targetId)
            return AppResult.Success(Unit)
        }
        override suspend fun getReceipts(targetKind: CheckTargetKind, targetId: String): AppResult<Receipts> {
            return AppResult.Success(Receipts())
        }
    }

    private class TestCheckRepository : CheckRepository {
        override suspend fun submitCheck(
            targetKind: CheckTargetKind,
            targetId: String,
            language: String,
            answers: List<Int>,
            idempotencyKey: String,
        ): AppResult<CheckResult> {
            val passed = answers.isNotEmpty() && answers[0] == 0
            return AppResult.Success(CheckResult(passed = passed))
        }
    }
}
