package com.techtsunagu.wakatta.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * The five channels. Ids and importance are permanent once created on a device, so change them
 * only with a new id. Calling [createAll] again is a no-op.
 */
object NotificationChannels {
    const val MESSAGES = "messages"
    const val CONTACTS = "contacts"
    const val SAFETY = "safety"
    const val LIFE = "life"
    const val UPDATES = "updates"

    fun channelFor(type: String): String = when (type) {
        "chat_message" -> MESSAGES
        "contact_message" -> CONTACTS
        "check_due", "nudge", "briefing_published" -> SAFETY
        "notice_new", "event_reminder" -> LIFE
        else -> UPDATES
    }

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannels(
            listOf(
                channel(context, MESSAGES, R.string.notifications_channel_messages, NotificationManager.IMPORTANCE_HIGH),
                channel(context, CONTACTS, R.string.notifications_channel_contacts, NotificationManager.IMPORTANCE_HIGH),
                channel(context, SAFETY, R.string.notifications_channel_safety, NotificationManager.IMPORTANCE_HIGH),
                channel(context, LIFE, R.string.notifications_channel_life, NotificationManager.IMPORTANCE_DEFAULT),
                channel(context, UPDATES, R.string.notifications_channel_updates, NotificationManager.IMPORTANCE_LOW),
            ),
        )
    }

    private fun channel(context: Context, id: String, nameRes: Int, importance: Int) =
        NotificationChannel(id, context.getString(nameRes), importance)
}
