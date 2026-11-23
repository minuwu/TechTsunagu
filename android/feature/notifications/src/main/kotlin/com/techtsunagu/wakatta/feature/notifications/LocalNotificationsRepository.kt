package com.techtsunagu.wakatta.feature.notifications

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationsRepository {
    private val prefs by lazy {
        context.getSharedPreferences("wakatta_notifications_local", Context.MODE_PRIVATE)
    }
    private val json = Json { ignoreUnknownKeys = true }
    private val memoryList = mutableListOf<NotificationItem>()
    private var isLoaded = false

    @Synchronized
    private fun ensureLoaded() {
        if (!isLoaded) {
            val raw = prefs.getString("items_json", null)
            if (!raw.isNullOrBlank()) {
                runCatching {
                    val parsed = json.decodeFromString<List<NotificationItem>>(raw)
                    memoryList.clear()
                    memoryList.addAll(parsed)
                }
            }
            isLoaded = true
        }
    }

    @Synchronized
    private fun persist() {
        runCatching {
            val encoded = json.encodeToString(memoryList.take(100))
            prefs.edit().putString("items_json", encoded).apply()
        }
    }

    override suspend fun load(): AppResult<List<NotificationItem>> = synchronized(this) {
        ensureLoaded()
        AppResult.Success(memoryList.toList())
    }

    override suspend fun save(item: NotificationItem): Unit = synchronized(this) {
        ensureLoaded()
        memoryList.removeAll { it.id == item.id }
        memoryList.add(0, item)
        persist()
    }

    override suspend fun markAsRead(id: String): Unit = synchronized(this) {
        ensureLoaded()
        val index = memoryList.indexOfFirst { it.id == id }
        if (index >= 0) {
            memoryList[index] = memoryList[index].copy(isUnread = false)
            persist()
        }
    }

    override suspend fun clear(): Unit = synchronized(this) {
        ensureLoaded()
        memoryList.clear()
        persist()
    }
}
