package com.techtsunagu.wakatta.core.sync

sealed interface OutboxDecision {
    data object Done : OutboxDecision
    data class Drop(val code: Int) : OutboxDecision
    data object PauseSession : OutboxDecision
    data class RetryLater(val retryAfterSec: Long?) : OutboxDecision
    data object Backoff : OutboxDecision
}

fun decide(code: Int, retryAfterSec: Long? = null): OutboxDecision {
    return when {
        code in 200..299 -> OutboxDecision.Done
        code == 401 -> OutboxDecision.PauseSession
        code == 404 || code == 409 -> OutboxDecision.Drop(code)
        code == 429 || code == 503 -> OutboxDecision.RetryLater(retryAfterSec)
        code in 500..599 -> OutboxDecision.Backoff
        else -> OutboxDecision.Drop(code)
    }
}
