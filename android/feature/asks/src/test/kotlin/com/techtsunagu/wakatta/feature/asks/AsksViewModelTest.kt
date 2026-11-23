package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.testing.MainDispatcherRule
import com.techtsunagu.wakatta.feature.asks.data.FakeAskRepository
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
import org.junit.Assert.assertNotNull
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

    private class TestAudioRecorder : AudioRecorder {
        var startCount = 0
        var stopCount = 0
        override fun start(): Flow<Pair<Float, Int>> {
            startCount++
            return flowOf(Pair(0.8f, 60))
        }
        override suspend fun stop(): File? {
            stopCount++
            return null
        }
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
            audioRecorder = TestAudioRecorder(),
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
            audioRecorder = TestAudioRecorder(),
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
            audioRecorder = TestAudioRecorder(),
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
            audioRecorder = TestAudioRecorder(),
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
    fun micClickTogglesRecordingAndSendsVoiceAsk() = runTest {
        val recorder = TestAudioRecorder()
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            audioRecorder = recorder,
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(),
        )
        advanceUntilIdle()

        // Start recording
        viewModel.onEvent(AskEvent.MicClick)
        assertTrue(viewModel.state.value.isRecording)
        assertEquals(1, recorder.startCount)

        // Stop recording
        viewModel.onEvent(AskEvent.MicClick)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isRecording)
        assertEquals(1, recorder.stopCount)
        assertTrue(viewModel.state.value.asks.any { it.kind.name == "VOICE" })
    }

    @Test
    fun toggleOriginalExpandsAndCollapses() = runTest {
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            audioRecorder = TestAudioRecorder(),
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
            audioRecorder = TestAudioRecorder(),
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
    fun foremanModeAllowsCannedReplies() = runTest {
        val foreman = User(id = "u_tanaka", displayName = "Tanaka", role = Role.FOREMAN, language = "ja")
        val session = Session(token = "tok", user = foreman)
        val viewModel = AskViewModel(
            askRepository = FakeAskRepository(0L),
            audioRecorder = TestAudioRecorder(),
            outboxWriter = TestOutboxWriter(),
            speaker = TestSpeaker(),
            sessionStore = TestSessionStore(session),
        )
        advanceUntilIdle()

        assertTrue(viewModel.state.value.isForeman)

        viewModel.onEvent(AskEvent.CannedReplyClick("ak3", 0))
        advanceUntilIdle()

        val updated = viewModel.state.value.asks.find { it.id == "ak3" }
        assertNotNull(updated)
        assertEquals("REPLIED", updated?.status?.name)
    }
}
