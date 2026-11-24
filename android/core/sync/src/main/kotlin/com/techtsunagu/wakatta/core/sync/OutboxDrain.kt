package com.techtsunagu.wakatta.core.sync

import com.techtsunagu.wakatta.core.database.OutboxEntity
import com.techtsunagu.wakatta.core.database.OutboxStore
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.OutboxSender
import com.techtsunagu.wakatta.core.model.SessionStore
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

internal enum class OutboxStep { REMOVE, RETRY, WAIT, FAIL }

internal fun outboxStep(result: AppResult<Unit>): OutboxStep = when (result) {
    is AppResult.Success -> OutboxStep.REMOVE
    is AppResult.Failure -> when (result.error) {
        AppError.Offline, AppError.Timeout, AppError.Busy, AppError.Server, is AppError.RateLimited -> OutboxStep.RETRY
        // Sent again after the next sign-in, or after the update.
        AppError.SessionExpired, AppError.PinChangeRequired, AppError.UpgradeRequired -> OutboxStep.WAIT
        else -> OutboxStep.FAIL
    }
}

/** Sends what the signed-in person queued, with the same Idempotency-Key each time. */
class OutboxDrain @Inject constructor(
    private val store: OutboxStore,
    private val senders: Set<@JvmSuppressWildcards OutboxSender>,
    private val session: SessionStore,
) {
    /** True when something must be tried again soon. */
    suspend fun run(): Boolean {
        val me = signedInUserId() ?: return false
        var again = false
        for (item in store.getPending(limit = Int.MAX_VALUE)) {
            val (name, userId) = item.kind.split(':', limit = 2).let { it[0] to it.getOrNull(1) }
            val sender = senders.find { it.name == name }
            if (userId != me || sender == null) continue
            when (outboxStep(sender.send(me, item.key, item.payloadJson))) {
                OutboxStep.REMOVE -> store.remove(item.key)
                OutboxStep.RETRY -> again = true
                OutboxStep.WAIT -> Unit
                OutboxStep.FAIL -> store.recordAttempt(item.key, nextAttemptAt = 0, state = OutboxEntity.STATE_FAILED)
            }
        }
        return again
    }

    /**
     * A cold start from WorkManager runs before the encrypted session has loaded, and `null` then means "not loaded
     * yet", not "signed out". Wait a little for it; a signed-out phone gets nothing after the wait.
     */
    private suspend fun signedInUserId(): String? =
        session.session.value?.user?.id
            ?: withTimeoutOrNull(SESSION_LOAD_WAIT_MS) { session.session.filterNotNull().first() }?.user?.id

    private companion object {
        const val SESSION_LOAD_WAIT_MS = 5_000L
    }
}
