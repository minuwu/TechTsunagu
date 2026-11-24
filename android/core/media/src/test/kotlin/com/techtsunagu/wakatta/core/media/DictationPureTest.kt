package com.techtsunagu.wakatta.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class DictationPureTest {

    @Test
    fun mapSpeechRecognizerError_mapsAllKnownCodes() {
        assertEquals(DictationErrorKind.NO_PERMISSION, mapSpeechRecognizerError(9))
        assertEquals(DictationErrorKind.NO_SPEECH, mapSpeechRecognizerError(6))
        assertEquals(DictationErrorKind.NO_SPEECH, mapSpeechRecognizerError(7))
        assertEquals(DictationErrorKind.NETWORK, mapSpeechRecognizerError(1))
        assertEquals(DictationErrorKind.NETWORK, mapSpeechRecognizerError(2))
        assertEquals(DictationErrorKind.NETWORK, mapSpeechRecognizerError(4))
        assertEquals(DictationErrorKind.NETWORK, mapSpeechRecognizerError(11))
        assertEquals(DictationErrorKind.UNAVAILABLE, mapSpeechRecognizerError(12))
        assertEquals(DictationErrorKind.UNAVAILABLE, mapSpeechRecognizerError(13))
        assertEquals(DictationErrorKind.UNAVAILABLE, mapSpeechRecognizerError(14))
        assertEquals(DictationErrorKind.BUSY, mapSpeechRecognizerError(8))
        assertEquals(DictationErrorKind.BUSY, mapSpeechRecognizerError(10))
        assertEquals(DictationErrorKind.OTHER, mapSpeechRecognizerError(3))
        assertEquals(DictationErrorKind.OTHER, mapSpeechRecognizerError(5))
        assertEquals(DictationErrorKind.OTHER, mapSpeechRecognizerError(15))
        assertEquals(DictationErrorKind.OTHER, mapSpeechRecognizerError(999))
    }

    @Test
    fun dictationAccumulator_emptyStart_collectsPartialAndFinal() {
        val acc = DictationAccumulator()
        assertEquals("", acc.current())

        assertEquals("Hello", acc.onPartial("Hello"))
        assertEquals("Hello world", acc.onPartial("Hello world"))
        assertEquals("Hello world", acc.onFinal("Hello world"))
        assertEquals("Hello world", acc.current())

        // Next utterance joins with space for English
        assertEquals("Hello world Next step", acc.onPartial("Next step"))
        assertEquals("Hello world Next step done", acc.onFinal("Next step done"))
        assertEquals("Hello world Next step done", acc.current())
    }

    @Test
    fun dictationAccumulator_withInitialText_appendsCorrectly() {
        val acc = DictationAccumulator("Pre-existing note")
        assertEquals("Pre-existing note", acc.current())

        assertEquals("Pre-existing note and more", acc.onPartial("and more"))
        assertEquals("Pre-existing note and final", acc.onFinal("and final"))
    }

    @Test
    fun dictationAccumulator_japaneseJoining_noSpaceBetweenKanjiKana() {
        val acc = DictationAccumulator("2号プレスの点検")
        assertEquals("2号プレスの点検", acc.current())

        // When both parts are Japanese, join directly without extra space
        val partial = acc.onPartial("完了しました")
        assertEquals("2号プレスの点検完了しました", partial)

        val finalized = acc.onFinal("完了しました")
        assertEquals("2号プレスの点検完了しました", finalized)
    }

    @Test
    fun dictationAccumulator_resetAndClear() {
        val acc = DictationAccumulator("Initial")
        acc.onPartial("draft")
        acc.reset("Brand new")
        assertEquals("Brand new", acc.current())

        acc.clear()
        assertEquals("", acc.current())
    }

    @Test
    fun dictationTag_mapsLanguagesAccurately() {
        assertEquals("ja-JP", dictationTag("ja"))
        assertEquals("ja-JP", dictationTag("ja-JP"))
        assertEquals("ja-JP", dictationTag("japanese"))
        assertEquals("bn-BD", dictationTag("bn"))
        assertEquals("bn-BD", dictationTag("bn-bd"))
        assertEquals("bn-BD", dictationTag("bengali"))
        assertEquals("vi-VN", dictationTag("vi"))
        assertEquals("vi-VN", dictationTag("vi-vn"))
        assertEquals("vi-VN", dictationTag("vietnamese"))
        assertEquals("id-ID", dictationTag("id"))
        assertEquals("id-ID", dictationTag("id-id"))
        assertEquals("id-ID", dictationTag("indonesian"))
        assertEquals("en-US", dictationTag("en"))
        assertEquals("en-US", dictationTag("en-us"))
        assertEquals("en-US", dictationTag("english"))
        assertEquals("tl-PH", dictationTag("tl"))
        assertEquals("tl-PH", dictationTag("tl-ph"))
        assertEquals("tl-PH", dictationTag("fil"))
        assertEquals("tl-PH", dictationTag("tagalog"))
        // Defaults to ja-JP
        assertEquals("ja-JP", dictationTag(null))
        assertEquals("ja-JP", dictationTag(""))
        assertEquals("ja-JP", dictationTag("unknown"))
    }
}
