package com.techtsunagu.wakatta.core.sync

interface OutboxSender {
    suspend fun send(kind: String, idempotencyKey: String, payloadJson: String): OutboxSendResult
}

data class OutboxSendResult(
    val statusCode: Int,
    val retryAfterSec: Long? = null
)
