package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.LanguageManager
import javax.inject.Inject
import javax.inject.Singleton

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
class FakeSenderNames @Inject constructor(
    private val languages: LanguageManager,
) : SenderNames {
    private val names = mutableMapOf<String, String>()

    fun setSenderName(senderId: String, name: String) {
        names[senderId] = name
    }

    override suspend fun nameOf(senderId: String): String? {
        return names[senderId] ?: NotificationTextBuilder.genericTitle(languages.currentLanguage.value)
    }
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
