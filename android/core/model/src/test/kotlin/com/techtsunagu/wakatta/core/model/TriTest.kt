package com.techtsunagu.wakatta.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TriTest {

    @Test
    fun pick_returnsTargetLanguageIfAvailable() {
        val tri: Tri = mapOf(
            "ja" to "こんにちは",
            "en" to "Hello",
            "bn" to "হ্যালো"
        )
        assertEquals("হ্যালো", pick(tri, "bn"))
        assertEquals("Hello", pick(tri, "en"))
        assertEquals("こんにちは", pick(tri, "ja"))
    }

    @Test
    fun pick_fallsBackToEnglishWhenTargetMissing() {
        val tri: Tri = mapOf(
            "ja" to "こんにちは",
            "en" to "Hello"
        )
        assertEquals("Hello", pick(tri, "bn"))
        assertEquals("Hello", pick(tri, "vi"))
    }

    @Test
    fun pick_fallsBackToJapaneseWhenEnglishMissing() {
        val tri: Tri = mapOf(
            "ja" to "こんにちは"
        )
        assertEquals("こんにちは", pick(tri, "bn"))
    }

    @Test
    fun pick_handlesNullAndEmpty() {
        assertEquals("", pick(null, "en"))
        assertEquals("", pick(emptyMap(), "en"))
    }

    @Test
    fun stripFurigana_removesFuriganaBraces() {
        val text = "今日は3号機の{あぶら}油の点検をしてください。"
        assertEquals("今日は3号機の油の点検をしてください。", stripFurigana(text))
    }
}
