package com.techtsunagu.wakatta.feature.notices

import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.feature.notices.data.FakeNoticeRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class NoticeNewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noticeRepository: FakeNoticeRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var audioRecorder: TestAudioRecorder
    private lateinit var photoCapture: TestPhotoCapture
    private lateinit var viewModel: NoticeNewViewModel

    class TestSessionStore(session: Session? = null) : SessionStore {
        private val _session = MutableStateFlow(session)
        override val session: StateFlow<Session?> = _session.asStateFlow()
        override suspend fun save(session: Session) { _session.value = session }
        override suspend fun clear() { _session.value = null }
        override fun getCachedToken(): String? = _session.value?.token
        fun setSession(session: Session?) { _session.value = session }
    }

    class TestAudioRecorder : AudioRecorder {
        override fun start() = emptyFlow<Pair<Float, Int>>()
        override suspend fun stop(): File? = null
    }

    class TestPhotoCapture : PhotoCapture {
        override suspend fun captureOrPick(): File? = null
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        noticeRepository = FakeNoticeRepository(delayMs = 0L)
        val foremanUser = User(
            id = "u_tanaka",
            displayName = "田中 健一",
            role = Role.FOREMAN,
            language = "ja",
        )
        sessionStore = TestSessionStore(Session(token = "tok_f", user = foremanUser))
        audioRecorder = TestAudioRecorder()
        photoCapture = TestPhotoCapture()

        viewModel = NoticeNewViewModel(
            noticeRepository = noticeRepository,
            sessionStore = sessionStore,
            audioRecorder = audioRecorder,
            photoCapture = photoCapture,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun isAllowedToPostIsFalseForForemanAndWorker() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isAllowedToPost)
    }

    @Test
    fun selectSourceKindUpdatesKind() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSourceKind(NoticeSourceKind.TEXT))
        assertEquals(NoticeSourceKind.TEXT, viewModel.state.value.selectedSourceKind)
    }

    @Test
    fun selectSampleTransitionsToBusyThenReview() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_fire"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(NoticeNewStage.REVIEW, state.stage)
        assertNotNull(state.draft)
        assertEquals("draft_fire", state.draft?.id)
        assertEquals("夜警・火の用心", state.editableTitle)
        assertTrue(state.events.isNotEmpty())
        assertTrue(state.events.all { it.confirmed })
    }

    @Test
    fun toggleEventConfirmedTogglesConfirmationState() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_fire"))
        advanceUntilIdle()

        val firstEventId = viewModel.state.value.events.first().id
        assertTrue(viewModel.state.value.events.first().confirmed)

        viewModel.onEvent(NoticeNewEvent.ToggleEventConfirmed(firstEventId))
        assertFalse(viewModel.state.value.events.first().confirmed)

        viewModel.onEvent(NoticeNewEvent.ToggleEventConfirmed(firstEventId))
        assertTrue(viewModel.state.value.events.first().confirmed)
    }

    @Test
    fun publishWithUnconfirmedEventFails() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_fire"))
        advanceUntilIdle()

        val firstEventId = viewModel.state.value.events.first().id
        viewModel.onEvent(NoticeNewEvent.ToggleEventConfirmed(firstEventId))
        assertFalse(viewModel.state.value.events.first().confirmed)

        viewModel.onEvent(NoticeNewEvent.Publish)
        advanceUntilIdle()

        assertEquals("EVENTS_NOT_CONFIRMED", viewModel.state.value.publishError)
        assertFalse(viewModel.state.value.publishSuccess)
    }

    @Test
    fun publishWithConfirmedEventsSucceeds() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_fire"))
        advanceUntilIdle()

        viewModel.onEvent(NoticeNewEvent.Publish)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.publishError)
        assertTrue(state.publishSuccess)
        assertNotNull(state.publishedNoticeId)
    }

    @Test
    fun updateTitleChangesEditableTitle() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_clean"))
        advanceUntilIdle()

        viewModel.onEvent(NoticeNewEvent.UpdateTitle("新しいタイトル"))
        assertEquals("新しいタイトル", viewModel.state.value.editableTitle)
    }

    @Test
    fun resetRestoresInitialState() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(NoticeNewEvent.SelectSample("smp_clean"))
        advanceUntilIdle()

        viewModel.onEvent(NoticeNewEvent.Reset)
        val state = viewModel.state.value
        assertEquals(NoticeNewStage.PICK, state.stage)
        assertNull(state.draft)
    }
}
