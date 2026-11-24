package com.techtsunagu.wakatta.core.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeviceFeaturesTest {
    private class Script(vararg rounds: DictationEvent) : DictationSource {
        private val left = ArrayDeque(rounds.toList())
        override fun isAvailable() = true
        override fun start(languageTag: String): Flow<DictationEvent> =
            flowOf(left.removeFirstOrNull() ?: DictationEvent.Error(DictationErrorKind.NETWORK))
        override fun stop() = Unit
    }

    @Test
    fun dictationGoesOnOverPausesAndKeepsTheTextBefore() = runTest {
        val source = Script(
            DictationEvent.Final("安全"),
            DictationEvent.Error(DictationErrorKind.NO_SPEECH),
            DictationEvent.Final("第一"),
        )
        assertEquals(
            listOf(
                DictationEvent.Final("今日は安全"),
                DictationEvent.Final("今日は安全第一"),
                DictationEvent.Error(DictationErrorKind.NETWORK),
            ),
            source.continuous("ja-JP", "今日は").toList(),
        )
    }

    @Test
    fun dictationEndsAfterALongSilence() = runTest {
        val silence = DictationEvent.Error(DictationErrorKind.NO_SPEECH)
        val source = Script(silence, silence, silence, DictationEvent.Final("late"))
        assertEquals(listOf(silence), source.continuous("ja-JP", "").take(2).toList())
    }

    @Test
    fun dictationErrorsSayWhatWentWrong() {
        assertEquals(R.string.media_dictation_no_speech, dictationMessage(DictationErrorKind.NO_SPEECH))
        assertEquals(R.string.media_mic_denied, dictationMessage(DictationErrorKind.NO_PERMISSION))
        assertEquals(R.string.media_dictation_network, dictationMessage(DictationErrorKind.NETWORK))
        assertEquals(R.string.media_dictation_unavailable, dictationMessage(DictationErrorKind.BUSY))
    }

    @Test
    fun scanGateIgnoresRepeatsAndTwoCodes() {
        val gate = ScanGate()
        assertEquals("a", gate.accept(listOf("a", "a"), 0))
        assertNull(gate.accept(listOf("a"), 1_000))
        assertNull(gate.accept(listOf("a", "b"), 1_100))
        assertEquals("b", gate.accept(listOf("b"), 1_200))
        assertEquals("a", gate.accept(listOf("a"), 1_300))
    }
}
