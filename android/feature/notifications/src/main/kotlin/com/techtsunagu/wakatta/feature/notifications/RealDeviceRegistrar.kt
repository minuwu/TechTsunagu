package com.techtsunagu.wakatta.feature.notifications

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class RealDeviceRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: dagger.Lazy<DeviceApi>,
    private val config: AppConfig,
    private val sessionStore: dagger.Lazy<SessionStore>,
    private val notificationsRepository: dagger.Lazy<NotificationsRepository>,
) : DeviceRegistrar {

    constructor(
        context: Context,
        api: () -> DeviceApi,
        config: AppConfig,
        sessionStore: SessionStore,
        notificationsRepository: NotificationsRepository = FakeNotificationsRepository(),
    ) : this(
        context = context,
        api = dagger.Lazy { api() },
        config = config,
        sessionStore = dagger.Lazy { sessionStore },
        notificationsRepository = dagger.Lazy { notificationsRepository },
    )

    private val prefs by lazy {
        context.getSharedPreferences("wakatta_notifications_device_prefs", Context.MODE_PRIVATE)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            var previousSession: Session? = null
            sessionStore.get().session.collect { session ->
                val wasLoggedIn = previousSession != null
                previousSession = session
                if (!wasLoggedIn && session != null) {
                    val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                    register(notificationsEnabled = enabled)
                } else if (wasLoggedIn && session == null) {
                    notificationsRepository.get().clear()
                }
            }
        }
    }

    internal fun getOrCreateInstallId(): String {
        val existing = prefs.getString("install_id", null)
        if (!existing.isNullOrBlank()) return existing
        val newId = UUID.randomUUID().toString()
        prefs.edit().putString("install_id", newId).apply()
        return newId
    }

    internal fun getCachedToken(): String? = prefs.getString("fcm_token", null)

    internal fun saveToken(token: String) {
        prefs.edit().putString("fcm_token", token).apply()
    }

    override suspend fun register(notificationsEnabled: Boolean, fcmToken: String?) {
        val installId = getOrCreateInstallId()
        val token = fcmToken ?: getCachedToken() ?: resolveFirebaseToken()
        if (token.isNullOrBlank()) {
            return
        }
        saveToken(token)

        val body = DeviceInDto(
            installId = installId,
            platform = "android",
            fcmToken = token,
            appVersion = config.versionName,
            osApiLevel = Build.VERSION.SDK_INT,
            locale = Locale.getDefault().toLanguageTag(),
            notificationsEnabled = notificationsEnabled,
        )
        safeApiCall { api.get().registerDevice(body) }
    }

    override suspend fun unregister() {
        val installId = getOrCreateInstallId()
        safeApiCallUnit { api.get().unregisterDevice(installId) }
    }

    private suspend fun resolveFirebaseToken(): String? = runCatching {
        FirebaseMessaging.getInstance().token.awaitTask()
    }.getOrNull()
}

internal suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { exception -> cont.resumeWithException(exception) }
        addOnCanceledListener { cont.cancel() }
    }
