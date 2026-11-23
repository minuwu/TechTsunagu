package com.techtsunagu.wakatta.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.techtsunagu.wakatta.core.database.OutboxStore

class OutboxWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val outboxStore: OutboxStore? = null,
    private val outboxSender: OutboxSender? = null
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val store = outboxStore ?: return Result.success()
        val sender = outboxSender ?: return Result.success()

        val now = System.currentTimeMillis()
        val pending = store.getPending(now = now, limit = 10)
        if (pending.isEmpty()) {
            return Result.success()
        }

        var shouldRetry = false

        for (item in pending) {
            val sendResult = sender.send(
                kind = item.kind,
                idempotencyKey = item.key,
                payloadJson = item.payloadJson
            )

            when (val decision = decide(sendResult.statusCode, sendResult.retryAfterSec)) {
                is OutboxDecision.Done -> {
                    store.remove(item.key)
                }
                is OutboxDecision.Drop -> {
                    store.remove(item.key)
                }
                is OutboxDecision.PauseSession -> {
                    return Result.retry()
                }
                is OutboxDecision.RetryLater -> {
                    val delayMs = (decision.retryAfterSec ?: DEFAULT_RETRY_AFTER_SEC) * 1000L
                    store.recordAttempt(item.key, nextAttemptAt = System.currentTimeMillis() + delayMs)
                    shouldRetry = true
                }
                is OutboxDecision.Backoff -> {
                    val backoffFactor = (1L shl item.attempts.coerceAtMost(6))
                    val delayMs = backoffFactor * BACKOFF_BASE_MS
                    store.recordAttempt(item.key, nextAttemptAt = System.currentTimeMillis() + delayMs)
                    shouldRetry = true
                }
            }
        }

        return if (shouldRetry) Result.retry() else Result.success()
    }

    companion object {
        private const val DEFAULT_RETRY_AFTER_SEC = 30L
        private const val BACKOFF_BASE_MS = 2000L
    }
}
