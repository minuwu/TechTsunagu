package com.techtsunagu.wakatta.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE `key` = :key")
    suspend fun get(key: String): OutboxEntity?

    @Query("SELECT * FROM outbox WHERE state = 'PENDING' AND nextAttemptAt <= :now ORDER BY nextAttemptAt ASC LIMIT :limit")
    suspend fun getPending(now: Long, limit: Int = 10): List<OutboxEntity>

    @Query("SELECT * FROM outbox ORDER BY nextAttemptAt ASC")
    suspend fun getAllOrderedByNextAttempt(): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox WHERE kind = :kind AND state = 'PENDING'")
    fun observePendingCount(kind: String): Flow<Int>

    @Update
    suspend fun update(item: OutboxEntity)

    @Query("DELETE FROM outbox WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()
}
