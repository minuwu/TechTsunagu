package com.techtsunagu.wakatta.feature.notifications

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.SettingsStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Turns a push data map into at most one local notification. Never logs payloads or tokens. */
@Singleton
class PushHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languages: LanguageManager,
    private val senders: SenderNames,
    private val registrar: DeviceRegistrar,
    private val repository: NotificationsRepository,
    private val settingsStore: dagger.Lazy<SettingsStore>,
) {
    constructor(
        context: Context,
        languages: LanguageManager,
        senders: SenderNames,
        registrar: DeviceRegistrar,
        repository: NotificationsRepository = FakeNotificationsRepository(),
        settingsStore: SettingsStore? = null,
    ) : this(
        context = context,
        languages = languages,
        senders = senders,
        registrar = registrar,
        repository = repository,
        settingsStore = dagger.Lazy {
            settingsStore ?: object : SettingsStore {
                override val isDarkMode: StateFlow<Boolean?> = MutableStateFlow(null)
                override val isLargeText: StateFlow<Boolean> = MutableStateFlow(false)
                override val isOfflineHintEnabled: StateFlow<Boolean> = MutableStateFlow(true)
                override val isNotificationEnabled: StateFlow<Boolean> = MutableStateFlow(true)
                override val isOnboardingDone: StateFlow<Boolean> = MutableStateFlow(true)
                override suspend fun setDarkMode(enabled: Boolean?) {}
                override suspend fun setLargeText(enabled: Boolean) {}
                override suspend fun setOfflineHint(enabled: Boolean) {}
                override suspend fun setNotification(enabled: Boolean) {}
                override suspend fun setOnboardingDone(done: Boolean) {}
            }
        },
    )

    private val seen = RecentEventIds()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** [title] and [body] are the server's visible text; without them the text is built here. */
    fun onMessage(data: Map<String, String>, title: String? = null, body: String? = null) {
        scope.launch { handle(data, title, body) }
    }

    fun onNewToken(token: String) {
        scope.launch { registrar.register(NotificationManagerCompat.from(context).areNotificationsEnabled(), token) }
    }

    internal suspend fun handle(data: Map<String, String>, title: String? = null, body: String? = null) {
        val payload = PushPayloadParser.parse(data) ?: return
        if (!seen.add(payload.eventId)) return
        val language = languages.currentLanguage.value
        val name = payload.senderName
            ?: payload.senderId?.let { senders.nameOf(it) }
            ?: NotificationTextBuilder.genericTitle(language)
        val text = NotificationTextBuilder.buildNotificationText(payload.threadKind, name, language) ?: title ?: return
        repository.save(
            NotificationItem(
                id = payload.eventId,
                kind = payload.threadKind,
                senderName = name,
                isUnread = true,
                title = title,
            ),
        )
        if (title == null) {
            post(payload, context.applicationInfo.loadLabel(context.packageManager), text)
        } else {
            post(payload, title, body.orEmpty())
        }
    }

    // The permission is checked through areNotificationsEnabled(), which is false without POST_NOTIFICATIONS.
    // Also checks SettingsStore.isNotificationEnabled.
    @SuppressLint("MissingPermission")
    private fun post(payload: PushPayload, title: CharSequence, text: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        if (!settingsStore.get().isNotificationEnabled.value) return
        NotificationChannels.createAll(context)
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            payload.link?.let { data = Uri.parse(it) }
            // The same extras Android puts on the launch intent of a push it shows itself.
            payload.threadId?.let { putExtra("threadId", it) }
            payload.machineId?.let { putExtra("machineId", it) }
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }?.let {
            val request = payload.eventId.hashCode() // one PendingIntent per push: extras do not tell them apart
            PendingIntent.getActivity(context, request, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, NotificationChannels.channelFor(payload.type))
            .setSmallIcon(R.drawable.notifications_ic_small)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(payload.eventId.hashCode(), notification)
    }
}
