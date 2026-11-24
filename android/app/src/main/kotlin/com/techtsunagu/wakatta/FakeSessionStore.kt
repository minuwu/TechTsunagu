package com.techtsunagu.wakatta

import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSessionStore @Inject constructor() : SessionStore {
    private val _session = MutableStateFlow<Session?>(null)
    override val session: StateFlow<Session?> = _session.asStateFlow()

    override suspend fun save(session: Session) {
        _session.value = session
    }

    override suspend fun clear() {
        _session.value = null
    }

    override fun getCachedToken(): String? = _session.value?.token

    fun signInAsDemoWorker() {
        _session.value = Session(
            token = "mock-worker-token",
            expiresAt = "2026-12-31T23:59:59Z",
            user = DemoSeed.demoWorker,
            mustChangePin = false,
        )
    }

    fun signInAsDemoForeman() {
        _session.value = Session(
            token = "mock-foreman-token",
            expiresAt = "2026-12-31T23:59:59Z",
            user = DemoSeed.demoForeman,
            mustChangePin = false,
        )
    }
}
