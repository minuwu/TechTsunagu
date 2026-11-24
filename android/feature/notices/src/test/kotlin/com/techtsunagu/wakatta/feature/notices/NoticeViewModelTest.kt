package com.techtsunagu.wakatta.feature.notices

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Receipts
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.notices.data.FakeNoticeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class NoticeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noticeRepository: FakeNoticeRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var checkRepository: TestCheckRepository
    private lateinit var receiptRepository: TestReceiptRepository
    private lateinit var speaker: TestSpeaker
    private lateinit var viewModel: NoticeViewModel

    class TestSessionStore(session: Session? = null) : SessionStore {
        private val _session = MutableStateFlow(session)
        override val session: StateFlow<Session?> = _session.asStateFlow()
        override suspend fun save(session: Session) { _session.value = session }
        override suspend fun clear() { _session.value = null }
        override fun getCachedToken(): String? = _session.value?.token
        fun setSession(session: Session?) { _session.value = session }
    }

    class TestCheckRepository : CheckRepository {
        var lastSubmittedTargetKind: CheckTargetKind? = null
        var lastSubmittedTargetId: String? = null
        override suspend fun submitCheck(
            targetKind: CheckTargetKind,
            targetId: String,
            language: String,
            answers: List<Int>,
            idempotencyKey: String,
        ): AppResult<CheckResult> {
            lastSubmittedTargetKind = targetKind
            lastSubmittedTargetId = targetId
            return AppResult.Success(CheckResult(passed = true))
        }
    }

    class TestReceiptRepository : ReceiptRepository {
        var lastOpenedKind: CheckTargetKind? = null
        var lastOpenedId: String? = null
        override suspend fun markOpened(targetKind: CheckTargetKind, targetId: String): AppResult<Unit> {
            lastOpenedKind = targetKind
            lastOpenedId = targetId
            return AppResult.Success(Unit)
        }
        override suspend fun getReceipts(targetKind: CheckTargetKind, targetId: String): AppResult<Receipts> {
            return AppResult.Success(Receipts())
        }
    }

    class TestSpeaker : Speaker {
        var lastSpokenText: String? = null
        var isCurrentlySpeaking = false
        override fun speak(text: String, languageTag: String) {
            lastSpokenText = text
            isCurrentlySpeaking = true
        }
        override fun isAvailable(languageTag: String): Boolean = true
        override fun stop() {
            isCurrentlySpeaking = false
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noticeRepository = FakeNoticeRepository(delayMs = 0L)
        val workerUser = User(
            id = "u_rahim",
            displayName = "Rahim Uddin",
            role = Role.WORKER,
            language = "bn",
        )
        sessionStore = TestSessionStore(Session(token = "tok_1", user = workerUser))
        checkRepository = TestCheckRepository()
        receiptRepository = TestReceiptRepository()
        speaker = TestSpeaker()

        viewModel = NoticeViewModel(
            noticeRepository = noticeRepository,
            sessionStore = sessionStore,
            checkRepository = checkRepository,
            receiptRepository = receiptRepository,
            speaker = speaker,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsWorkerNoticeAndMarksOpened() = runTest(testDispatcher) {
        viewModel.onEvent(NoticeEvent.Init("n_burn"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isForeman)
        assertEquals("n_burn", state.id)
        assertNotNull(state.workerNotice)
        assertEquals(CheckTargetKind.NOTICE, receiptRepository.lastOpenedKind)
        assertEquals("n_burn", receiptRepository.lastOpenedId)
    }

    @Test
    fun initLoadsForemanNoticeForForemanRole() = runTest(testDispatcher) {
        val foremanUser = User(
            id = "u_tanaka",
            displayName = "田中 健一",
            role = Role.FOREMAN,
            language = "ja",
        )
        sessionStore.setSession(Session(token = "tok_f", user = foremanUser))
        viewModel.onEvent(NoticeEvent.Init("n_burn"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isForeman)
        assertNotNull(state.posterNotice)
        assertTrue(state.posterNotice!!.workerStatuses.isNotEmpty())
    }

    @Test
    fun selectAnswerAndSubmitCheckUpdatesResult() = runTest(testDispatcher) {
        viewModel.onEvent(NoticeEvent.Init("n_burn"))
        advanceUntilIdle()

        // Select correct answer (1)
        viewModel.onEvent(NoticeEvent.SelectAnswer(1))
        assertEquals(1, viewModel.state.value.selectedAnswer)

        viewModel.onEvent(NoticeEvent.SubmitCheck)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isSubmittingCheck)
        val result = state.checkResult
        assertNotNull(result)
        assertTrue(result!!.passed)
        assertEquals(WakattaStatus.GREEN, result.status)
        assertEquals(CheckTargetKind.NOTICE, checkRepository.lastSubmittedTargetKind)
        assertEquals("n_burn", checkRepository.lastSubmittedTargetId)
    }

    @Test
    fun toggleReminderUpdatesEvent() = runTest(testDispatcher) {
        viewModel.onEvent(NoticeEvent.Init("n_burn"))
        advanceUntilIdle()

        viewModel.onEvent(NoticeEvent.ToggleReminder("ev_burn_1"))
        advanceUntilIdle()

        val event = viewModel.state.value.workerNotice?.events?.find { it.id == "ev_burn_1" }
        assertNotNull(event)
        assertTrue(event!!.isReminderOn)
    }

    @Test
    fun toggleSpeakStartsAndStopsSpeaker() = runTest(testDispatcher) {
        viewModel.onEvent(NoticeEvent.Init("n_burn"))
        advanceUntilIdle()

        viewModel.onEvent(NoticeEvent.ToggleSpeak)
        assertTrue(viewModel.state.value.isSpeaking)
        assertTrue(speaker.isCurrentlySpeaking)
        assertNotNull(speaker.lastSpokenText)

        viewModel.onEvent(NoticeEvent.ToggleSpeak)
        assertFalse(viewModel.state.value.isSpeaking)
        assertFalse(speaker.isCurrentlySpeaking)
    }

    @Test
    fun errorStateWhenNoticeNotFound() = runTest(testDispatcher) {
        viewModel.onEvent(NoticeEvent.Init("non_existent_id"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals(R.string.notices_error_not_found, state.errorRes)
    }
}
