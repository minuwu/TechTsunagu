package com.techtsunagu.wakatta.feature.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.techtsunagu.wakatta.core.model.LanguageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Turns a push data map into at most one local notification. Never logs payloads or tokens. */
@Singleton
class PushHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languages: LanguageManager,
    private val senders: SenderNames,
    private val registrar: DeviceRegistrar,
    private val repository: NotificationsRepository,
) {
    constructor(
        context: Context,
        languages: LanguageManager,
        senders: SenderNames,
        registrar: DeviceRegistrar,
    ) : this(context, languages, senders, registrar, FakeNotificationsRepository())

    private val seen = RecentEventIds()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun onMessage(data: Map<String, String>) {
        scope.launch { handle(data) }
    }

    fun onNewToken(token: String) {
        scope.launch { registrar.register(NotificationManagerCompat.from(context).areNotificationsEnabled(), token) }
    }

    internal suspend fun handle(data: Map<String, String>) {
        val payload = PushPayloadParser.parse(data) ?: return
        if (!seen.add(payload.eventId)) return
        val name = payload.senderName ?: payload.senderId?.let { senders.nameOf(it) } ?: return
        val text = NotificationTextBuilder.buildNotificationText(
            payload.threadKind,
            name,
            languages.currentLanguage.value,
        ) ?: return
        repository.save(
            NotificationItem(
                id = payload.eventId,
                kind = payload.threadKind,
                senderName = name,
                isUnread = true,
            ),
        )
        post(payload, text)
    }

    // The permission is checked through areNotificationsEnabled(), which is false without POST_NOTIFICATIONS.
    @SuppressLint("MissingPermission")
    private fun post(payload: PushPayload, text: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        NotificationChannels.createAll(context)
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            payload.link?.let { data = Uri.parse(it) }
        }?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, NotificationChannels.channelFor(payload.type))
            .setSmallIcon(R.drawable.notifications_ic_small)
            .setContentTitle(context.applicationInfo.loadLabel(context.packageManager))
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(payload.eventId.hashCode(), notification)
    }
}
