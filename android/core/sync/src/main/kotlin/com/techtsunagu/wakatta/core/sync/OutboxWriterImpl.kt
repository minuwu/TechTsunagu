package com.techtsunagu.wakatta.core.sync

import com.techtsunagu.wakatta.core.database.OutboxStore
import com.techtsunagu.wakatta.core.model.OutboxWriter
import kotlinx.coroutines.flow.Flow

class OutboxWriterImpl(
    private val outboxStore: OutboxStore
) : OutboxWriter {

    override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
        outboxStore.enqueue(
            kind = kind,
            idempotencyKey = idempotencyKey,
            payloadJson = payloadJson
        )
    }

    override fun pendingCount(kind: String): Flow<Int> {
        return outboxStore.pendingCount(kind)
    }
}
