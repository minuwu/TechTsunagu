package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User

data class RosterUser(
    val id: String,
    val displayName: String,
    val role: String,
)

/** Sign-in and PIN. A successful [signIn] has already saved the session in the SessionStore. */
interface AuthRepository {
    suspend fun roster(shopCode: String, role: Role): AppResult<List<RosterUser>>
    suspend fun signIn(userId: String, pin: String): AppResult<Session>
    suspend fun changePin(currentPin: String, newPin: String): AppResult<Unit>
    suspend fun signOut()

    /**
     * Opens a new shop with this person as its first Foreman and returns the new shop code. The session starts only
     * with [finishRegister], after the owner has seen the code: a saved session switches the app to Home at once (M11).
     */
    suspend fun register(shopName: String, displayName: String, pin: String, shopCode: String? = null): AppResult<String>

    /** Starts the session [register] kept back. */
    suspend fun finishRegister()

    /** Forgot the shop code: signs in by shop name, own name and PIN; the session carries the code. */
    suspend fun find(shopName: String, displayName: String, pin: String): AppResult<Session>
}

internal object AuthRules {
    private val shopCodePattern = Regex("^[A-Z0-9]{6,12}$")
    private val pinPattern = Regex("^\\d{4,6}$")

    fun validateShopCode(shopCode: String): Boolean = shopCodePattern.matches(shopCode)

    fun validatePin(pin: String): Boolean = pinPattern.matches(pin)
}

/** Seeded demo data for previews, tests and builds without a live API. */
class FakeAuthRepository(private val store: SessionStore? = null) : AuthRepository {

    private val users = listOf(
        RosterUser("u_tanaka", "田中 健 (Tanaka Ken)", "foreman"),
        RosterUser("u_rahim", "Rahim Uddin", "worker"),
        RosterUser("u_nguyen", "Nguyen Van Nam", "worker"),
        RosterUser("u_siti", "Siti Rahma", "worker"),
    )

    override suspend fun roster(shopCode: String, role: Role): AppResult<List<RosterUser>> {
        if (!AuthRules.validateShopCode(shopCode)) return AppResult.Success(emptyList())
        return AppResult.Success(users.filter { it.role == role.name.lowercase() })
    }

    override suspend fun signIn(userId: String, pin: String): AppResult<Session> {
        val user = users.firstOrNull { it.id == userId } ?: return AppResult.Failure(AppError.WrongPin)
        val role = if (user.role == "foreman") Role.FOREMAN else Role.WORKER
        val session = Session(
            token = "fake-token",
            user = User(id = user.id, displayName = user.displayName, role = role, language = "en"),
        )
        store?.save(session)
        return AppResult.Success(session)
    }

    override suspend fun changePin(currentPin: String, newPin: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun find(shopName: String, displayName: String, pin: String): AppResult<Session> =
        users.firstOrNull { it.displayName.equals(displayName.trim(), ignoreCase = true) }?.let { signIn(it.id, pin) }
            ?: AppResult.Failure(AppError.WrongPin)

    private var pending: Session? = null

    override suspend fun register(shopName: String, displayName: String, pin: String, shopCode: String?): AppResult<String> {
        pending = Session(token = "fake-token", user = User(id = "u_new", displayName = displayName, role = Role.FOREMAN, language = "ja"))
        return AppResult.Success(shopCode ?: "NEWSHOP1")
    }

    override suspend fun finishRegister() {
        pending?.let { store?.save(it) }
        pending = null
    }

    override suspend fun signOut() {
        store?.clear()
    }
}
