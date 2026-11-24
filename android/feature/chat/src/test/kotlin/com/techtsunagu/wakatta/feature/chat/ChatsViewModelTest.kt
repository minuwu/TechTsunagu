package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.feature.chat.data.FakeChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.techtsunagu.wakatta.feature.chat.data.WorkThreadSource
import com.techtsunagu.wakatta.feature.chat.model.ThreadFilter
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModelTest {

    private class TestSessionStore(user: User? = null) : SessionStore {
        private val flow = MutableStateFlow<Session?>(user?.let { Session("tok", user = it) })
        override val session: StateFlow<Session?> = flow
        override suspend fun save(session: Session) { flow.value = session }
        override suspend fun clear() { flow.value = null }
        override fun getCachedToken(): String? = flow.value?.token
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeChatRepository
    private lateinit var viewModel: ChatsViewModel
    private val sessionStore = TestSessionStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeChatRepository().apply { setLatency(0L) }
        val threadSource = WorkThreadSource(repository)
        viewModel = ChatsViewModel(setOf(threadSource), sessionStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadsThreads() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertNull(state.error)
        assertEquals(3, state.threads.size)
    }

    @Test
    fun filterUnreadOnlyShowsThreadsWithUnread() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ChatsEvent.SetFilter(ThreadFilter.UNREAD))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(ThreadFilter.UNREAD, state.filter)
        assertTrue(state.threads.all { it.unreadCount > 0 })
        assertEquals(1, state.threads.size)
        assertEquals("t_line2", state.threads.first().id)
    }

    @Test
    fun filterAllShowsAllThreads() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ChatsEvent.SetFilter(ThreadFilter.UNREAD))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.state.value.threads.size)

        viewModel.onEvent(ChatsEvent.SetFilter(ThreadFilter.ALL))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.state.value.threads.size)
    }

    @Test
    fun retryAndRefreshKeepOneCollectorOnTheSource() = runTest {
        val upstream = MutableStateFlow(emptyList<ThreadSummary>())
        val source = object : ThreadSource {
            override fun getThreads() = upstream
        }
        val vm = ChatsViewModel(setOf(source), sessionStore)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(ChatsEvent.Retry)
        vm.onEvent(ChatsEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, upstream.subscriptionCount.value)
    }

    @Test
    fun retryRefreshesThreads() = runTest {
        viewModel.onEvent(ChatsEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(3, viewModel.state.value.threads.size)
    }

    @Test
    fun workerSeesForemanTitleInsteadOfOwnNameOnThread() = runTest {
        val workerUser = User("12", "Rahim Uddin", Role.WORKER, "bn")
        val fakeSessionStore = TestSessionStore(workerUser)
        val source = object : ThreadSource {
            override fun getThreads() = MutableStateFlow(
                listOf(
                    ThreadSummary("t_rahim", mapOf("ja" to "Rahim Uddin"), "", 0, ""),
                ),
            )
        }
        val vm = ChatsViewModel(setOf(source), fakeSessionStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val thread = vm.state.value.threads.first()
        assertEquals("職長", thread.title["ja"])
        assertEquals("Foreman", thread.title["en"])
        assertEquals("ফোরম্যান", thread.title["bn"])
    }
}
