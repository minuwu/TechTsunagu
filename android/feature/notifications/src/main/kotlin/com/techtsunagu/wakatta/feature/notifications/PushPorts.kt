package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tells the backend about this install. The Real implementation calls the device
 * endpoint with `installId`, `platform` = android, `fcmToken`, `appVersion`, `osApiLevel`, `locale`
 * and `notificationsEnabled`, and the sign-out call before logout. It owns the install id.
 */
interface DeviceRegistrar {
    /** [fcmToken] null means "the current token". */
    suspend fun register(notificationsEnabled: Boolean, fcmToken: String? = null)

    suspend fun unregister()
}

/** The display name of a sender, looked up on the device. Payloads carry only the id. */
interface SenderNames {
    suspend fun nameOf(senderId: String): String?
}

@Serializable
data class NotificationItem(
    val id: String,
    val kind: ThreadKind,
    val senderName: String,
    val isUnread: Boolean,
)

interface NotificationsRepository {
    suspend fun load(): AppResult<List<NotificationItem>>
    suspend fun save(item: NotificationItem) {}
    suspend fun markAsRead(id: String) {}
    suspend fun clear() {}
}

@Singleton
class FakeDeviceRegistrar @Inject constructor() : DeviceRegistrar {
    var lastEnabled: Boolean? = null
        private set
    var lastToken: String? = null
        private set
    var unregisterCalled: Boolean = false
        private set

    override suspend fun register(notificationsEnabled: Boolean, fcmToken: String?) {
        lastEnabled = notificationsEnabled
        lastToken = fcmToken
    }

    override suspend fun unregister() {
        lastEnabled = null
        unregisterCalled = true
    }
}

@Singleton
class FakeSenderNames @Inject constructor() : SenderNames {
    override suspend fun nameOf(senderId: String): String? = "Tanaka"
}

@Singleton
class FakeNotificationsRepository @Inject constructor() : NotificationsRepository {
    private val items = mutableListOf(
        NotificationItem("n1", ThreadKind.CONTACT, "Tanaka", isUnread = true),
        NotificationItem("n2", ThreadKind.CONTACT, "Rahim", isUnread = false),
    )

    override suspend fun load(): AppResult<List<NotificationItem>> = synchronized(items) {
        AppResult.Success(items.toList())
    }

    override suspend fun save(item: NotificationItem): Unit = synchronized(items) {
        items.removeAll { it.id == item.id }
        items.add(0, item)
    }

    override suspend fun markAsRead(id: String): Unit = synchronized(items) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) {
            items[index] = items[index].copy(isUnread = false)
        }
    }

    override suspend fun clear(): Unit = synchronized(items) {
        items.clear()
    }
}
