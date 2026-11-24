package com.techtsunagu.wakatta.core.database

import android.content.Context
import androidx.room.Room
import com.techtsunagu.wakatta.core.model.CacheStore

/** The Room file of the app, without Room in its signature. One per process. */
class WakattaStores(context: Context) {
    private val database = Room.databaseBuilder(context, AppDatabase::class.java, "wakatta.db").build()
    val outbox: OutboxStore = RoomOutboxStore(database.outboxDao())
    val cache: CacheStore = RoomCacheStore(database.cacheEntryDao())
}
