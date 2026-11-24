package com.techtsunagu.wakatta.core.media

import kotlinx.coroutines.flow.Flow

enum class DictationErrorKind {
    NO_PERMISSION,
    NO_SPEECH,
    NETWORK,
    UNAVAILABLE,
    BUSY,
    OTHER,
}

sealed interface DictationEvent {
    data object Listening : DictationEvent
    data class Partial(val text: String) : DictationEvent
    data class Final(val text: String) : DictationEvent
    data class Error(val kind: DictationErrorKind) : DictationEvent
}

/**
 * Platform speech recognition source.
 * Wraps SpeechRecognizer on Android; testable on JVM via test doubles.
 */
interface DictationSource {
    fun isAvailable(): Boolean
    fun start(languageTag: String): Flow<DictationEvent>
    fun stop()
}
