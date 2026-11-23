package com.techtsunagu.wakatta.feature.notices

import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
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
class LifeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var noticeRepository: FakeNoticeRepository
    private lateinit var sessionStore: TestSessionStore
    private lateinit var viewModel: LifeViewModel

    class TestSessionStore(session: Session? = null) : SessionStore {
        private val _session = MutableStateFlow(session)
        override val session: StateFlow<Session?> = _session.asStateFlow()
        override suspend fun save(session: Session) { _session.value = session }
        override suspend fun clear() { _session.value = null }
        override fun getCachedToken(): String? = _session.value?.token
        fun setSession(session: Session?) { _session.value = session }
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
        viewModel = LifeViewModel(noticeRepository, sessionStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadForWorkerPopulatesState() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isForeman)
        assertEquals("bn", state.userLanguage)
        assertTrue(state.upcomingEvents.isNotEmpty())
        assertEquals(4, state.notices.size)
        // Two notices in seed are in WAIT status for Rahim (n_burn, n_drill)
        assertEquals(2, state.pendingNoticeCount)
        assertNull(state.error)
    }

    @Test
    fun loadForForemanPopulatesForemanSpecifics() = runTest(testDispatcher) {
        val foremanUser = User(
            id = "u_tanaka",
            displayName = "田中 健一",
            role = Role.FOREMAN,
            language = "ja",
        )
        sessionStore.setSession(Session(token = "tok_f", user = foremanUser))
        viewModel.onEvent(LifeEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isForeman)
        assertEquals("ja", state.userLanguage)
        assertTrue(state.posterNotices.isNotEmpty())
        assertNotNull(state.todayWord)
        assertEquals("ধন্যবাদ", state.todayWord?.term)
    }

    @Test
    fun toggleReminderUpdatesUpcomingEvents() = runTest(testDispatcher) {
        advanceUntilIdle()
        val initialEvents = viewModel.state.value.upcomingEvents
        assertTrue(initialEvents.isNotEmpty())
        val firstEventId = initialEvents[0].id

        viewModel.onEvent(LifeEvent.ToggleReminder(firstEventId))
        advanceUntilIdle()

        val updatedEvent = viewModel.state.value.upcomingEvents.find { it.id == firstEventId }
        assertNotNull(updatedEvent)
        assertTrue(updatedEvent!!.isReminderOn)
    }

    @Test
    fun refreshEventReloadsData() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(LifeEvent.Refresh)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertTrue(state.notices.isNotEmpty())
    }

    @Test
    fun retryEventReloadsData() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.onEvent(LifeEvent.Retry)
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertTrue(viewModel.state.value.notices.isNotEmpty())
    }
}
