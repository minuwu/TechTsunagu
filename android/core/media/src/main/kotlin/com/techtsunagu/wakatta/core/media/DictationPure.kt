package com.techtsunagu.wakatta.core.media

// Standard SpeechRecognizer error codes from android.speech.SpeechRecognizer
const val SPEECH_ERROR_NETWORK_TIMEOUT = 1
const val SPEECH_ERROR_NETWORK = 2
const val SPEECH_ERROR_AUDIO = 3
const val SPEECH_ERROR_SERVER = 4
const val SPEECH_ERROR_CLIENT = 5
const val SPEECH_ERROR_SPEECH_TIMEOUT = 6
const val SPEECH_ERROR_NO_MATCH = 7
const val SPEECH_ERROR_RECOGNIZER_BUSY = 8
const val SPEECH_ERROR_INSUFFICIENT_PERMISSIONS = 9
const val SPEECH_ERROR_TOO_MANY_REQUESTS = 10
const val SPEECH_ERROR_SERVER_DISCONNECTED = 11
const val SPEECH_ERROR_LANGUAGE_NOT_SUPPORTED = 12
const val SPEECH_ERROR_LANGUAGE_UNAVAILABLE = 13
const val SPEECH_ERROR_CANNOT_CHECK_SUPPORT = 14
const val SPEECH_ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS = 15

/**
 * Maps Android SpeechRecognizer error integer codes to [DictationErrorKind].
 * Pure Kotlin without android.jar dependencies so it runs in JVM unit tests.
 */
fun mapSpeechRecognizerError(errorCode: Int): DictationErrorKind = when (errorCode) {
    SPEECH_ERROR_INSUFFICIENT_PERMISSIONS -> DictationErrorKind.NO_PERMISSION
    SPEECH_ERROR_SPEECH_TIMEOUT, SPEECH_ERROR_NO_MATCH -> DictationErrorKind.NO_SPEECH
    SPEECH_ERROR_NETWORK_TIMEOUT,
    SPEECH_ERROR_NETWORK,
    SPEECH_ERROR_SERVER,
    SPEECH_ERROR_SERVER_DISCONNECTED,
    -> DictationErrorKind.NETWORK
    SPEECH_ERROR_LANGUAGE_NOT_SUPPORTED,
    SPEECH_ERROR_LANGUAGE_UNAVAILABLE,
    SPEECH_ERROR_CANNOT_CHECK_SUPPORT,
    -> DictationErrorKind.UNAVAILABLE
    SPEECH_ERROR_RECOGNIZER_BUSY,
    SPEECH_ERROR_TOO_MANY_REQUESTS,
    -> DictationErrorKind.BUSY
    else -> DictationErrorKind.OTHER
}

/**
 * Pure Kotlin helper to join partial and finalized speech segments.
 * Retains existing prefix, handles spacing between words, and respects
 * CJK (Japanese) punctuation and character boundaries.
 */
class DictationAccumulator(initialText: String = "") {
    private var committed: String = initialText.trim()
    private var partial: String = ""

    fun onPartial(text: String): String {
        partial = text.trim()
        return current()
    }

    fun onFinal(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            committed = if (committed.isEmpty()) {
                trimmed
            } else {
                join(committed, trimmed)
            }
        }
        partial = ""
        return committed
    }

    fun current(): String {
        if (partial.isEmpty()) return committed
        if (committed.isEmpty()) return partial
        return join(committed, partial)
    }

    fun reset(text: String = "") {
        committed = text.trim()
        partial = ""
    }

    fun clear() {
        committed = ""
        partial = ""
    }

    private fun join(first: String, second: String): String {
        if (first.isEmpty()) return second
        if (second.isEmpty()) return first
        if (first.endsWith(" ") || first.endsWith("\n") || second.startsWith(" ")) {
            return "$first$second"
        }
        val lastChar = first.last()
        val firstChar = second.first()
        val isCjk = isCjkOrPunctuation(lastChar) && isCjkOrPunctuation(firstChar)
        return if (isCjk) "$first$second" else "$first $second"
    }

    private fun isCjkOrPunctuation(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
            ub == Character.UnicodeBlock.HIRAGANA ||
            ub == Character.UnicodeBlock.KATAKANA ||
            ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
            ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
    }
}

/**
 * Maps user language preference to the IETF BCP 47 language tag for SpeechRecognizer.
 * Supported languages: ja-JP (default), bn-BD, vi-VN, id-ID, en-US, tl-PH.
 */
fun dictationTag(lang: String?): String = when (lang?.lowercase()?.trim()) {
    "bn", "bn-bd", "bengali" -> "bn-BD"
    "vi", "vi-vn", "vietnamese" -> "vi-VN"
    "id", "id-id", "indonesian" -> "id-ID"
    "en", "en-us", "english" -> "en-US"
    "tl", "fil", "tl-ph", "tagalog", "filipino" -> "tl-PH"
    "ja", "ja-jp", "japanese" -> "ja-JP"
    else -> "ja-JP"
}
