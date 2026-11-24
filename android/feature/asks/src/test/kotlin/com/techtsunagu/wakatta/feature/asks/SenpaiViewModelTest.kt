package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import com.techtsunagu.wakatta.feature.asks.data.FakeMeetupRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeSenpaiRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeTodayWordSource
import com.techtsunagu.wakatta.feature.asks.data.SenpaiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SenpaiViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class TestSessionStore(session: Session? = null) : SessionStore {
        private val _session = MutableStateFlow(session)
        override val session: StateFlow<Session?> = _session.asStateFlow()
        override suspend fun save(session: Session) { _session.value = session }
        override suspend fun clear() { _session.value = null }
        override fun getCachedToken(): String? = _session.value?.token
    }

    @Test
    fun initialWorkerStateLoadsPairAndMeetups() = runTest {
        val session = Session(token = "tok", user = DemoSeed.demoWorker)
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(session),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isForeman)
        assertEquals("u_rahim", state.workerId)
        assertNotNull(state.pair)
        assertEquals("佐藤 美咲", state.pair?.senpaiName)
        assertTrue(state.meetups.isNotEmpty())
    }

    @Test
    fun openAndCloseMeetupSheet() = runTest {
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()

        viewModel.onEvent(SenpaiEvent.OpenMeetupSheet("drill"))
        assertTrue(viewModel.state.value.isMeetupSheetOpen)
        assertEquals("drill", viewModel.state.value.meetupDraftKind)

        viewModel.onEvent(SenpaiEvent.CloseMeetupSheet)
        assertFalse(viewModel.state.value.isMeetupSheetOpen)
    }

    @Test
    fun meetupDraftChanges() = runTest {
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(),
            workerRepository = TestWorkers(),
        )
        viewModel.onEvent(SenpaiEvent.MeetupKindChanged("festival"))
        assertEquals("festival", viewModel.state.value.meetupDraftKind)

        viewModel.onEvent(SenpaiEvent.MeetupDateChanged("2026-11-20"))
        assertEquals("2026-11-20", viewModel.state.value.meetupDraftDate)

        viewModel.onEvent(SenpaiEvent.MeetupNoteChanged("Chatted about culture"))
        assertEquals("Chatted about culture", viewModel.state.value.meetupDraftNote)
    }

    @Test
    fun saveMeetupLogsNewMeetup() = runTest {
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()

        viewModel.onEvent(SenpaiEvent.OpenMeetupSheet("coffee"))
        viewModel.onEvent(SenpaiEvent.MeetupNoteChanged("Discussed job training"))
        viewModel.onEvent(SenpaiEvent.SaveMeetupClick)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isMeetupSheetOpen)
        assertTrue(viewModel.state.value.meetups.any { it.note == "Discussed job training" })
    }

    @Test
    fun foremanPairsAWorkerWithANamedSenpai() = runTest {
        val foreman = User(id = "u_tanaka", displayName = "Tanaka", role = Role.FOREMAN, language = "ja")
        val pairs = FakeSenpaiRepository(0L)
        val viewModel = SenpaiViewModel(
            senpaiRepository = pairs,
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(Session(token = "tok", user = foreman)),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()

        viewModel.onEvent(SenpaiEvent.OpenPairSheet)
        viewModel.onEvent(SenpaiEvent.CreatePairLinkClick) // no name: nothing is sent
        advanceUntilIdle()
        assertNull(viewModel.state.value.createdPairToken)

        viewModel.onEvent(SenpaiEvent.PairDraftChanged("6", " Sato ", "jichikai"))
        viewModel.onEvent(SenpaiEvent.CreatePairLinkClick)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.createdPairToken)
        val made = viewModel.state.value.pairs.first { it.workerId == "6" }
        assertEquals("Sato", made.senpaiName)
        assertEquals("jichikai", made.kind)

        viewModel.onEvent(SenpaiEvent.DismissTokenDialog)
        assertNull(viewModel.state.value.createdPairToken)
    }

    @Test
    fun aFutureMeetupIsRefusedBeforeTheServerIsAsked() = runTest {
        val meetups = FakeMeetupRepository(0L)
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = meetups,
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(),
            workerRepository = TestWorkers(),
        )
        viewModel.onEvent(SenpaiEvent.OpenMeetupSheet())
        viewModel.onEvent(SenpaiEvent.MeetupDateChanged("2999-01-01"))
        viewModel.onEvent(SenpaiEvent.SaveMeetupClick)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isMeetupSheetOpen)
        assertEquals(R.string.asks_error_future_date, (viewModel.effects.first() as SenpaiEffect.ShowToast).messageRes)
    }

    @Test
    fun aListThatDoesNotLoadShowsTheErrorAndRetryLoadsItAgain() = runTest {
        var up = false
        val real = FakeSenpaiRepository(0L)
        val flaky = object : SenpaiRepository by real {
            override fun getSenpaiPairForWorker(workerId: String) = flow {
                if (!up) error("offline")
                emit(real.getSenpaiPairForWorker(workerId).first())
            }
        }
        val viewModel = SenpaiViewModel(
            senpaiRepository = flaky,
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(Session(token = "tok", user = DemoSeed.demoWorker)),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()
        assertEquals(R.string.asks_error_load, viewModel.state.value.errorRes)

        up = true
        viewModel.onEvent(SenpaiEvent.Retry)
        advanceUntilIdle()
        assertNull(viewModel.state.value.errorRes)
        assertNotNull(viewModel.state.value.pair)
    }

    @Test
    fun foremanStateLoadsAllPairsAndTodayWord() = runTest {
        val foreman = User(id = "u_tanaka", displayName = "Tanaka", role = Role.FOREMAN, language = "ja")
        val session = Session(token = "tok", user = foreman)
        val viewModel = SenpaiViewModel(
            senpaiRepository = FakeSenpaiRepository(0L),
            meetupRepository = FakeMeetupRepository(0L),
            todayWordSource = FakeTodayWordSource(),
            sessionStore = TestSessionStore(session),
            workerRepository = TestWorkers(),
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isForeman)
        assertTrue(state.pairs.isNotEmpty())
        assertNotNull(state.todayWord)
        assertEquals("ধন্যবাদ", state.todayWord?.word)
    }
}

internal class TestWorkers : WorkerRepository {
    override suspend fun getWorkers(): AppResult<List<WorkerInfo>> =
        AppResult.Success(listOf(WorkerInfo("5", "Rahim Uddin", "bn"), WorkerInfo("6", "Siti Rahma", "id")))
}
