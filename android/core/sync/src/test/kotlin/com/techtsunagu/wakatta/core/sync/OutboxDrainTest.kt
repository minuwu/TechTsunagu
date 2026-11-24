package com.techtsunagu.wakatta.core.sync

import com.techtsunagu.wakatta.core.database.OutboxEntity
import com.techtsunagu.wakatta.core.database.OutboxStore
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.OutboxSender
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboxDrainTest {
    private class Store : OutboxStore {
        val rows = linkedMapOf<String, OutboxEntity>()
        override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String, createdAt: Long) {
            rows[idempotencyKey] = OutboxEntity(idempotencyKey, kind, payloadJson, createdAt)
        }
        override suspend fun get(key: String) = rows[key]
        override suspend fun getPending(now: Long, limit: Int) = rows.values.filter { it.state == OutboxEntity.STATE_PENDING }
        override suspend fun recordAttempt(key: String, nextAttemptAt: Long, state: String) {
            rows[key] = rows.getValue(key).copy(state = state)
        }
        override suspend fun remove(key: String) {
            rows.remove(key)
        }
        override suspend fun oldestFailed(kind: String) = rows.values.find { it.kind == kind && it.state == OutboxEntity.STATE_FAILED }
        override fun pendingCount(kind: String): Flow<Int> = flowOf(rows.size)
    }

    private class Sender(var result: AppResult<Unit>) : OutboxSender {
        val keys = mutableListOf<String>()
        override val name = "nearmiss"
        override suspend fun send(userId: String, idempotencyKey: String, payloadJson: String): AppResult<Unit> {
            keys += idempotencyKey
            return result
        }
    }

    private class Sessions(initial: Session? = SIGNED_IN) : SessionStore {
        override val session = MutableStateFlow(initial)
        override suspend fun save(session: Session) = Unit
        override suspend fun clear() = Unit
        override fun getCachedToken(): String? = null
    }

    private val store = Store()

    private fun drain(sender: Sender) = OutboxDrain(store, setOf(sender), Sessions())

    @Test
    fun aSessionThatLoadsAfterTheStartIsWaitedFor() = runTest {
        // A cold start from WorkManager: the encrypted session is still loading when the drain runs.
        val sessions = Sessions(initial = null)
        val sender = Sender(AppResult.Success(Unit))
        store.enqueue("nearmiss:12", "k1", "{}")
        launch {
            delay(1_000)
            sessions.session.value = SIGNED_IN
        }
        assertFalse(OutboxDrain(store, setOf(sender), sessions).run())
        assertEquals(listOf("k1"), sender.keys)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun aSignedOutPhoneSendsNothingAndDoesNotHang() = runTest {
        val sender = Sender(AppResult.Success(Unit))
        store.enqueue("nearmiss:12", "k1", "{}")
        assertFalse(OutboxDrain(store, setOf(sender), Sessions(initial = null)).run())
        assertTrue(sender.keys.isEmpty())
        assertEquals(setOf("k1"), store.rows.keys)
    }

    @Test
    fun aNetworkErrorKeepsTheRowAndTheKey() = runTest {
        val sender = Sender(AppResult.Failure(AppError.Offline))
        store.enqueue("nearmiss:12", "k1", "{}")
        assertTrue(drain(sender).run())
        sender.result = AppResult.Success(Unit)
        assertFalse(drain(sender).run())
        assertEquals(listOf("k1", "k1"), sender.keys)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun aRefusedWriteIsNotTriedAgain() = runTest {
        val sender = Sender(AppResult.Failure(AppError.Validation()))
        store.enqueue("nearmiss:12", "k1", "{}")
        assertFalse(drain(sender).run())
        assertFalse(drain(sender).run())
        assertEquals(listOf("k1"), sender.keys)
        assertEquals("k1", store.oldestFailed("nearmiss:12")?.key)
    }

    @Test
    fun anotherPersonsWriteAndAnExpiredSessionWait() = runTest {
        val sender = Sender(AppResult.Failure(AppError.SessionExpired))
        store.enqueue("nearmiss:13", "other", "{}")
        store.enqueue("nearmiss:12", "mine", "{}")
        assertFalse(drain(sender).run())
        assertEquals(listOf("mine"), sender.keys)
        assertEquals(setOf("other", "mine"), store.rows.keys)
        assertTrue(store.rows.values.all { it.state == OutboxEntity.STATE_PENDING })
    }

    private companion object {
        val SIGNED_IN = Session("t", user = User(id = "12", displayName = "R", role = Role.WORKER, language = "bn"))
    }
}
