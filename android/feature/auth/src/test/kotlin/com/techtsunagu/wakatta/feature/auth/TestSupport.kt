package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class MemorySessionStore(initial: Session? = null) : SessionStore {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<Session?> = state
    override suspend fun save(session: Session) {
        state.value = session
    }
    override suspend fun clear() {
        state.value = null
    }
    override fun getCachedToken(): String? = state.value?.token
}

/** Answers with whatever the test sets. */
class ScriptedAuthRepository : AuthRepository {
    var rosterResult: AppResult<List<RosterUser>> = AppResult.Success(emptyList())
    var signInResult: AppResult<Session> = AppResult.Failure(AppError.WrongPin)
    var changePinResult: AppResult<Unit> = AppResult.Success(Unit)
    var lastRosterRole: Role? = null

    override suspend fun roster(shopCode: String, role: Role): AppResult<List<RosterUser>> {
        lastRosterRole = role
        return rosterResult
    }
    override suspend fun signIn(userId: String, pin: String): AppResult<Session> = signInResult
    override suspend fun changePin(currentPin: String, newPin: String): AppResult<Unit> = changePinResult
    override suspend fun signOut() = Unit
    var registerResult: AppResult<String> = AppResult.Success("NEWSHOP1")
    override suspend fun register(shopName: String, displayName: String, pin: String, shopCode: String?) = registerResult
    var findResult: AppResult<Session> = AppResult.Failure(AppError.WrongPin)
    override suspend fun find(shopName: String, displayName: String, pin: String) = findResult
}
