package com.techtsunagu.wakatta.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntryEntity)

    @Query("SELECT * FROM cache_entry WHERE `key` = :key")
    suspend fun get(key: String): CacheEntryEntity?

    @Query("DELETE FROM cache_entry WHERE `key` LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)

    @Query("DELETE FROM cache_entry WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM cache_entry")
    suspend fun clearAll()
}
