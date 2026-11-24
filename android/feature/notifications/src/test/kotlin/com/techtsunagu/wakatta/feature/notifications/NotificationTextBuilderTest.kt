package com.techtsunagu.wakatta.feature.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationTextBuilderTest {

    @Test
    fun contactThreadProducesGenericTextInEnglish() {
        val text = NotificationTextBuilder.buildNotificationText(
            threadKind = ThreadKind.CONTACT,
            senderName = "Rahim",
            language = "en",
        )
        assertEquals("New message from Rahim", text)
    }

    @Test
    fun contactThreadProducesGenericTextInJapanese() {
        val text = NotificationTextBuilder.buildNotificationText(
            threadKind = ThreadKind.CONTACT,
            senderName = "Tanaka",
            language = "ja",
        )
        assertEquals("Tanakaさんから新しいメッセージ", text)
    }

    @Test
    fun contactThreadProducesGenericTextInBengali() {
        val text = NotificationTextBuilder.buildNotificationText(
            threadKind = ThreadKind.CONTACT,
            senderName = "Rahim",
            language = "bn",
        )
        assertEquals("Rahim এর কাছ থেকে নতুন বার্তা", text)
    }

    @Test
    fun languageTagsAreNormalisedAndUnknownFallsBackToEnglish() {
        fun text(language: String) =
            NotificationTextBuilder.buildNotificationText(ThreadKind.CONTACT, "Rahim", language)
        assertEquals("Rahimさんから新しいメッセージ", text("JA-JP"))
        assertEquals("New message from Rahim", text("vi"))
        assertEquals("New message from Rahim", text(""))
    }

    @Test
    fun talkThreadProducesNoNotification() {
        val text = NotificationTextBuilder.buildNotificationText(
            threadKind = ThreadKind.TALK,
            senderName = "Tanabe",
            language = "en",
        )
        assertNull(text)
    }

    @Test
    fun otherKindsProduceNoNotification() {
        for (kind in listOf(ThreadKind.WORK, ThreadKind.UNKNOWN)) {
            assertNull(NotificationTextBuilder.buildNotificationText(kind, "Rahim", "en"))
        }
    }

    @Test
    fun notificationTextNeverContainsMessageContentInAnyLanguage() {
        val sensitiveMessage = "Confidential medical record data"
        for (language in listOf("en", "ja", "bn")) {
            val text = NotificationTextBuilder.buildNotificationText(
                threadKind = ThreadKind.CONTACT,
                senderName = "Rahim",
                language = language,
                messageText = sensitiveMessage,
            )
            assertFalse(language, text!!.contains(sensitiveMessage))
        }
    }
}
