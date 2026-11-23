package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.navigation.AnalyticsKey
import com.techtsunagu.wakatta.core.navigation.AskKey
import com.techtsunagu.wakatta.core.navigation.BoardKey
import com.techtsunagu.wakatta.core.navigation.BriefingNewKey
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.CourseKey
import com.techtsunagu.wakatta.core.navigation.FollowUpsKey
import com.techtsunagu.wakatta.core.navigation.LearningKey
import com.techtsunagu.wakatta.core.navigation.LifeKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.MachinesKey
import com.techtsunagu.wakatta.core.navigation.NearMissListKey
import com.techtsunagu.wakatta.core.navigation.NearMissNewKey
import com.techtsunagu.wakatta.core.navigation.NoteKey
import com.techtsunagu.wakatta.core.navigation.NotificationsKey
import com.techtsunagu.wakatta.core.navigation.ScanKey
import com.techtsunagu.wakatta.core.navigation.SkillsKey
import com.techtsunagu.wakatta.core.navigation.SupportLogKey
import com.techtsunagu.wakatta.core.navigation.TaskKey
import com.techtsunagu.wakatta.feature.home.data.FakeEventSource
import com.techtsunagu.wakatta.feature.home.data.FakeTaskRepository
import com.techtsunagu.wakatta.feature.home.data.HomeAttentionSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

internal class TestSessionStore(initialSession: Session? = null) : SessionStore {
    private val _session = MutableStateFlow(initialSession)
    override val session: StateFlow<Session?> = _session.asStateFlow()
    override suspend fun save(session: Session) { _session.value = session }
    override suspend fun clear() { _session.value = null }
    override fun getCachedToken(): String? = _session.value?.token
}

private class TestMachineRepository : MachineRepository {
    override suspend fun getMachines(): AppResult<List<Machine>> = AppResult.Success(DemoSeed.machines)
    override suspend fun getMachine(id: String): AppResult<MachineDetail> = AppResult.Failure(AppError.NotFound)
    override suspend fun scan(machineId: String): AppResult<Unit> = AppResult.Success(Unit)
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var machineRepository: MachineRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var eventSource: FakeEventSource
    private lateinit var sessionStore: TestSessionStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        machineRepository = TestMachineRepository()
        taskRepository = FakeTaskRepository(delayMs = 0L)
        eventSource = FakeEventSource()
        sessionStore = TestSessionStore(
            Session(
                token = "token",
                user = DemoSeed.demoForeman,
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            attentionSources = setOf(HomeAttentionSource()),
            machineRepository = machineRepository,
            taskRepository = taskRepository,
            eventSource = eventSource,
            boardSource = com.techtsunagu.wakatta.feature.home.data.FakeHomeBoardSource(),
            sessionStore = sessionStore,
        )
    }

    @Test
    fun loadDataPopulatesForemanState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.isForeman)
        assertEquals("田中 健一", state.userName)
        assertEquals(5, state.tallyGreen)
        assertEquals(1, state.tallyWait)
        assertTrue(state.attentionCards.isNotEmpty())
        assertTrue(state.lockedMachines.isNotEmpty())
    }

    @Test
    fun loadDataPopulatesWorkerState() = runTest(testDispatcher) {
        sessionStore.save(
            Session(
                token = "worker_token",
                user = DemoSeed.demoWorker,
            )
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isForeman)
        assertEquals("Rahim Uddin", state.userName)
        assertNotNull(state.todayBriefing)
        assertTrue(state.pendingChecks.isNotEmpty())
        assertTrue(state.myMachines.isNotEmpty())
    }

    @Test
    fun bellClickEmitsNotificationsNavigation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.BellClick)
        val effect = viewModel.effects.first()
        assertTrue(effect is HomeEffect.Navigate)
        assertEquals(NotificationsKey, (effect as HomeEffect.Navigate).key)
    }

    @Test
    fun followUpClickEmitsFollowUpsNavigation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.FollowUpClick)
        val effect = viewModel.effects.first()
        assertTrue(effect is HomeEffect.Navigate)
        assertEquals(FollowUpsKey, (effect as HomeEffect.Navigate).key)
    }

    @Test
    fun tileClicksEmitNavigation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.BoardTileClick)
        val effect1 = viewModel.effects.first()
        assertEquals(BoardKey, (effect1 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.MachinesTileClick)
        val effect2 = viewModel.effects.first()
        assertEquals(MachinesKey, (effect2 as HomeEffect.Navigate).key)
    }

    @Test
    fun quickActionClicksEmitNavigation() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.BriefingClick)
        val e1 = viewModel.effects.first()
        assertEquals(BriefingNewKey, (e1 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.NearMissClick)
        val e2 = viewModel.effects.first()
        assertEquals(NearMissListKey, (e2 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.AnalyticsClick)
        val e3 = viewModel.effects.first()
        assertEquals(AnalyticsKey, (e3 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.SkillsClick)
        val e4 = viewModel.effects.first()
        assertEquals(SkillsKey, (e4 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.LifeClick)
        val e5 = viewModel.effects.first()
        assertEquals(LifeKey, (e5 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.SupportLogClick)
        val e6 = viewModel.effects.first()
        assertTrue((e6 as HomeEffect.Navigate).key is SupportLogKey)
    }

    @Test
    fun machineClickNavigatesByReadiness() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.MachineClick("mc1", isReady = true))
        val e1 = viewModel.effects.first()
        assertEquals(MachineKey("mc1"), (e1 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.MachineClick("mc2", isReady = false))
        val e2 = viewModel.effects.first()
        assertEquals(NoteKey("mc2"), (e2 as HomeEffect.Navigate).key)
    }

    @Test
    fun workerSpecificEventsEmitNavigation() = runTest(testDispatcher) {
        sessionStore.save(Session(token = "tok", user = DemoSeed.demoWorker))
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ScanMachineClick)
        val e1 = viewModel.effects.first()
        assertEquals(ScanKey, (e1 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.ReportNearMissClick)
        val e2 = viewModel.effects.first()
        assertTrue((e2 as HomeEffect.Navigate).key is NearMissNewKey)

        viewModel.onEvent(HomeEvent.AskClick)
        val e3 = viewModel.effects.first()
        assertEquals(AskKey, (e3 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.SafetyCourseClick)
        val e4 = viewModel.effects.first()
        assertEquals(CourseKey, (e4 as HomeEffect.Navigate).key)

        viewModel.onEvent(HomeEvent.MyLearningClick)
        val e5 = viewModel.effects.first()
        assertTrue((e5 as HomeEffect.Navigate).key is LearningKey)
    }

    @Test
    fun retryReloadsData() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(HomeEvent.Retry)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}
