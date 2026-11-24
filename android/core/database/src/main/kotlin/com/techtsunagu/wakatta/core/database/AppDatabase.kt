package com.techtsunagu.wakatta.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OutboxEntity::class, CacheEntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outboxDao(): OutboxDao
    abstract fun cacheEntryDao(): CacheEntryDao
}
