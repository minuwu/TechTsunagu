package com.techtsunagu.wakatta.feature.notifications

/** A push data map reduced to ids. The text of the notification is built on the device. */
data class PushPayload(
    val type: String,
    val eventId: String,
    val threadKind: ThreadKind,
    val senderId: String?,
    val senderName: String? = null,
    val link: String? = null,
)

object PushPayloadParser {
    // Substrings that mark answers, checks, care content or message text (platform contract 1.7).
    private val forbiddenParts = listOf(
        "answer", "correct", "care", "health", "pain", "appetite", "transcript", "body", "text", "content", "check", "confusion",
    )

    /** Returns null for a payload the app must ignore: unknown version, missing ids, or a forbidden field. */
    fun parse(data: Map<String, String>): PushPayload? {
        if (data["v"] != "1") return null
        val forbidden = data.keys.any { key ->
            key.equals("message", ignoreCase = true) || forbiddenParts.any { key.contains(it, ignoreCase = true) }
        }
        if (forbidden) return null
        val type = data["type"]?.takeIf { it.isNotBlank() } ?: return null
        val eventId = data["eventId"]?.takeIf { it.isNotBlank() } ?: return null
        return PushPayload(
            type = type,
            eventId = eventId,
            threadKind = threadKindOf(type),
            senderId = data["senderId"]?.takeIf { it.isNotBlank() },
            senderName = data["senderName"]?.takeIf { it.isNotBlank() },
            link = data["link"]?.takeIf { it.isNotBlank() },
        )
    }

    private fun threadKindOf(type: String) = when (type) {
        "contact_message" -> ThreadKind.CONTACT
        "talk_message" -> ThreadKind.TALK
        "chat_message" -> ThreadKind.WORK
        else -> ThreadKind.UNKNOWN
    }
}

/** The last [max] event ids; a repeated push is ignored. */
class RecentEventIds(private val max: Int = 100) {
    private val ids = LinkedHashSet<String>()

    /** True when [id] is new. */
    @Synchronized
    fun add(id: String): Boolean {
        if (!ids.add(id)) return false
        if (ids.size > max) ids.remove(ids.first())
        return true
    }
}
