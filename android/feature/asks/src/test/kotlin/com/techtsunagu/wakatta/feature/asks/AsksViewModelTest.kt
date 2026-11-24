package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import com.techtsunagu.wakatta.feature.asks.data.AskRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeAskRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AsksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class TestSessionStore(session: Session? = null) : SessionStore {
        private val _session = MutableStateFlow(session)
        override val session: StateFlow<Session?> = _session.asStateFlow()
        override suspend fun save(session: Session) { _session.value = session }
        override suspend fun clear() { _session.value = null }
        override fun getCachedToken(): String? = _session.value?.token
    }

    private class TestOutboxWriter : OutboxWriter {
        val enqueued = mutableListOf<String>()
        override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
            enqueued.add(kind)
        }
        override fun pendingCount(kind: String): Flow<Int> = flowOf(enqueued.size)
    }

    private class TestSpeaker : Speaker {
        var spokenText: String? = null
        var stopped = false
        override fun speak(text: String, languageTag: String) {
            spokenText = text
            stopped = false
        }
        override fun isAvailable(languageTag: String): Boolean = true
        override fun stop() { stopped = true }
    }

    @Test
    fun initialStateLoadsWorkerAsks() = runTest {
        val session = Session(token = "tok", user = DemoSeed.demoWorker)
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(session),
        )
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isForeman)
        assertEquals("u_rahim", state.workerId)
        assertTrue(state.asks.isNotEmpty())
    }

    @Test
    fun queryChangedUpdatesState() = runTest {
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(),
        )
        viewModel.onEvent(AskEvent.QueryChanged("test query"))
        assertEquals("test query", viewModel.state.value.currentInput)
    }

    @Test
    fun sendClickClearsInputAndSends() = runTest {
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(),
        )
        advanceUntilIdle()

        viewModel.onEvent(AskEvent.QueryChanged("When is garbage day?"))
        viewModel.onEvent(AskEvent.SendClick)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("", state.currentInput)
        assertTrue(state.asks.any { it.text == "When is garbage day?" })
    }

    @Test
    fun suggestionClickSendsAsk() = runTest {
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(),
        )
        advanceUntilIdle()

        viewModel.onEvent(AskEvent.SuggestionClick("Where is the drill?"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.asks.any { it.text == "Where is the drill?" })
    }

    @Test
    fun toggleOriginalExpandsAndCollapses() = runTest {
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(),
        )
        advanceUntilIdle()

        viewModel.onEvent(AskEvent.ToggleOriginalClick("ak2"))
        assertTrue(viewModel.state.value.expandedOriginalIds.contains("ak2"))

        viewModel.onEvent(AskEvent.ToggleOriginalClick("ak2"))
        assertFalse(viewModel.state.value.expandedOriginalIds.contains("ak2"))
    }

    @Test
    fun speakClickTogglesSpeaker() = runTest {
        val speaker = TestSpeaker()
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            outboxWriter = TestOutboxWriter(),
            speaker = speaker,
            sessionStore = TestSessionStore(),
        )
        advanceUntilIdle()

        viewModel.onEvent(AskEvent.SpeakClick("ak2", "Hello", "en-US"))
        assertEquals("ak2", viewModel.state.value.currentlySpeakingAskId)
        assertEquals("Hello", speaker.spokenText)

        viewModel.onEvent(AskEvent.SpeakClick("ak2", "Hello", "en-US"))
        assertFalse(viewModel.state.value.currentlySpeakingAskId == "ak2")
        assertTrue(speaker.stopped)
    }

    @Test
    fun aFailedQuestionGoesBackInTheBoxAndIsSentAgainWithItsKey() = runTest {
        val real = FakeAskRepository(0L)
        val keys = mutableListOf<String>()
        val flaky = object : AskRepository by real {
            override suspend fun sendAsk(workerId: String, request: AskCreateRequest): AppResult<AskDto> {
                keys += request.idempotencyKey
                return if (keys.size == 1) AppResult.Failure(AppError.Offline) else real.sendAsk(workerId, request)
            }
        }
        val viewModel = AskViewModel(
            askRepository = flaky,
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(Session(token = "tok", user = DemoSeed.demoWorker)),
        )
        advanceUntilIdle()
        viewModel.onEvent(AskEvent.QueryChanged("Where is the bus?"))
        viewModel.onEvent(AskEvent.SendClick)
        advanceUntilIdle()
        assertEquals("Where is the bus?", viewModel.state.value.currentInput)
        assertEquals(null, viewModel.state.value.errorRes)

        viewModel.onEvent(AskEvent.SendClick)
        advanceUntilIdle()
        assertEquals("", viewModel.state.value.currentInput)
        assertEquals(2, keys.size)
        assertEquals(keys[0], keys[1])
    }
}
