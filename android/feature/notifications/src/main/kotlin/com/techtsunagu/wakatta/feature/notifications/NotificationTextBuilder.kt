package com.techtsunagu.wakatta.feature.notifications

import kotlinx.serialization.Serializable

@Serializable
enum class ThreadKind {
    CONTACT,
    TALK,
    WORK,
    UNKNOWN,
}

object NotificationTextBuilder {
    /**
     * The generic text for a Contact Thread message, in the account language (ja, en, bn; anything
     * else falls back to en). Talk Threads and every other kind give null: no notification.
     * [messageText] is accepted so a caller cannot leak it by mistake; it is never used.
     */
    fun buildNotificationText(
        threadKind: ThreadKind,
        senderName: String,
        language: String,
        @Suppress("UNUSED_PARAMETER") messageText: String? = null,
    ): String? {
        if (threadKind != ThreadKind.CONTACT) {
            return null
        }
        return when (language.lowercase().substringBefore('-')) {
            "ja" -> "${senderName}さんから新しいメッセージ"
            "bn" -> "${senderName} এর কাছ থেকে নতুন বার্তা"
            else -> "New message from $senderName"
        }
    }
}
