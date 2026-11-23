package com.techtsunagu.wakatta.core.database

import kotlinx.coroutines.flow.Flow

class RoomOutboxStore(
    private val outboxDao: OutboxDao
) : OutboxStore {

    override suspend fun enqueue(
        kind: String,
        idempotencyKey: String,
        payloadJson: String,
        createdAt: Long
    ) {
        val entity = OutboxEntity(
            key = idempotencyKey,
            kind = kind,
            payloadJson = payloadJson,
            createdAt = createdAt,
            attempts = 0,
            nextAttemptAt = createdAt,
            state = OutboxEntity.STATE_PENDING
        )
        outboxDao.insert(entity)
    }

    override suspend fun get(key: String): OutboxEntity? {
        return outboxDao.get(key)
    }

    override suspend fun getPending(now: Long, limit: Int): List<OutboxEntity> {
        return outboxDao.getPending(now = now, limit = limit)
    }

    override suspend fun recordAttempt(key: String, nextAttemptAt: Long, state: String) {
        val existing = outboxDao.get(key) ?: return
        val updated = existing.copy(
            attempts = existing.attempts + 1,
            nextAttemptAt = nextAttemptAt,
            state = state
        )
        outboxDao.update(updated)
    }

    override suspend fun remove(key: String) {
        outboxDao.delete(key)
    }

    override fun pendingCount(kind: String): Flow<Int> {
        return outboxDao.observePendingCount(kind)
    }
}
