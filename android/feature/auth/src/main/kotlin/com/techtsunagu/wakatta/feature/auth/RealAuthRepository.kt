package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.AuthApi
import com.techtsunagu.wakatta.core.network.ChangePinRequest
import com.techtsunagu.wakatta.core.network.LoginRequest
import com.techtsunagu.wakatta.core.network.RegisterRequest
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit
import com.techtsunagu.wakatta.core.network.toSession
import com.techtsunagu.wakatta.core.network.toUser

/** [api] is a provider so mock builds never construct the HTTP stack. */
class RealAuthRepository(
    private val api: () -> AuthApi,
    private val store: SessionStore,
) : AuthRepository {

    override suspend fun roster(shopCode: String, role: Role): AppResult<List<RosterUser>> {
        val wireRole = role.name.lowercase()
        return when (val result = safeApiCall { api().roster(shopCode, wireRole) }) {
            is AppResult.Success -> AppResult.Success(result.value.map { RosterUser(it.id, it.displayName, wireRole) })
            is AppResult.Failure -> result
        }
    }

    override suspend fun signIn(userId: String, pin: String): AppResult<Session> =
        when (val result = safeApiCall { api().login(LoginRequest(userId, pin)) }) {
            is AppResult.Success -> {
                val session = result.value.toSession()
                store.save(session)
                AppResult.Success(session)
            }
            is AppResult.Failure -> result
        }

    override suspend fun changePin(currentPin: String, newPin: String): AppResult<Unit> {
        val result = safeApiCallUnit { api().changePin(ChangePinRequest(currentPin, newPin)) }
        val current = store.session.value
        if (result.isSuccess && current != null && current.mustChangePin) {
            store.save(current.copy(mustChangePin = false))
        }
        return result
    }

    override suspend fun register(shopName: String, displayName: String, pin: String): AppResult<String> =
        when (val result = safeApiCall { api().register(RegisterRequest(shopName, displayName, pin)) }) {
            is AppResult.Success -> {
                val r = result.value
                store.save(Session(token = r.token, expiresAt = r.expiresAt, user = r.user.toUser()))
                AppResult.Success(r.shopCode)
            }
            is AppResult.Failure -> result
        }

    /** The local session ends even when the server cannot be reached. */
    override suspend fun signOut() {
        safeApiCallUnit { api().logout() }
        store.clear()
    }
}
