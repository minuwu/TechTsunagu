package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.serialization.Serializable

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
    /** The title the server wrote (in the account language); null when the text is built on the device. */
    val title: String? = null,
)

interface NotificationsRepository {
    suspend fun load(): AppResult<List<NotificationItem>>
    suspend fun save(item: NotificationItem) {}
    suspend fun markAsRead(id: String) {}
    suspend fun clear() {}
}
