package com.techtsunagu.wakatta.core.database

import kotlinx.coroutines.flow.Flow

interface OutboxStore {
    suspend fun enqueue(
        kind: String,
        idempotencyKey: String,
        payloadJson: String,
        createdAt: Long = System.currentTimeMillis()
    )
    suspend fun get(key: String): OutboxEntity?
    suspend fun getPending(now: Long = System.currentTimeMillis(), limit: Int = 10): List<OutboxEntity>
    suspend fun recordAttempt(key: String, nextAttemptAt: Long, state: String = OutboxEntity.STATE_PENDING)
    suspend fun remove(key: String)
    suspend fun oldestFailed(kind: String): OutboxEntity?
    fun pendingCount(kind: String): Flow<Int>
}
