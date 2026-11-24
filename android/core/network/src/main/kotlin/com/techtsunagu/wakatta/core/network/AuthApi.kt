package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.appLanguage
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/** Auth routes (docs/openapi.yaml tag `auth`). Field names are the backend's; never send a worker id. */
interface AuthApi {
    @GET(ApiPaths.ROSTER)
    suspend fun roster(
        @Query("code") code: String,
        @Query("role") role: String,
    ): Response<ApiEnvelope<List<RosterEntryDto>>>

    @POST(ApiPaths.LOGIN)
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginResponse>>

    @GET(ApiPaths.ME)
    suspend fun me(): Response<ApiEnvelope<UserDto>>

    @PUT(ApiPaths.ME_PIN)
    suspend fun changePin(@Body body: ChangePinRequest): Response<ApiEnvelope<Unit>>

    @POST(ApiPaths.LOGOUT)
    suspend fun logout(): Response<ApiEnvelope<Unit>>
}

@Serializable
data class RosterEntryDto(val id: String, val displayName: String)

@Serializable
data class LoginRequest(val userId: String, val pin: String)

@Serializable
data class ChangePinRequest(val currentPin: String, val newPin: String)

/** `role` stays a string on the wire; a value this app does not know maps to [Role.UNKNOWN]. */
@Serializable
data class UserDto(
    val id: String,
    val displayName: String,
    val role: String = "",
    val language: String = "ja",
    val shopId: String? = null,
    val districtId: String? = null,
)

@Serializable
data class LoginResponse(
    val token: String,
    val expiresAt: String? = null,
    val mustChangePin: Boolean = false,
    val user: UserDto,
)

fun UserDto.toUser(): User = User(
    id = id,
    displayName = displayName,
    role = Role.entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: Role.UNKNOWN,
    language = appLanguage(language),
    shopId = shopId,
    districtId = districtId,
)

fun LoginResponse.toSession(): Session =
    Session(token = token, expiresAt = expiresAt, user = user.toUser(), mustChangePin = mustChangePin)
