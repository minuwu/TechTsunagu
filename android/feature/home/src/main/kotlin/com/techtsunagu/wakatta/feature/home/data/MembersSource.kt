package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.safeApiCall
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** A person of the shop as a Foreman manages them (`/api/workers`, `/api/foremen`). */
data class Member(
    val id: String,
    val name: String,
    val role: Role,
    val language: String,
    val active: Boolean,
    /** Still on the first PIN the Foreman shared: they have not signed in yet. */
    val mustChangePin: Boolean,
)

interface MembersSource {
    suspend fun list(): AppResult<List<Member>>
    suspend fun add(role: Role, name: String, language: String, pin: String): AppResult<Member>
    suspend fun update(member: Member, name: String? = null, pin: String? = null, active: Boolean? = null): AppResult<Member>
}

/** A first PIN the server accepts: 4 digits for a Worker, 6 for a Foreman, not a run or a repeat (domain/pins.py). */
fun firstPin(role: Role, random: SecureRandom = SecureRandom()): String {
    val length = if (role == Role.FOREMAN) 6 else 4
    while (true) {
        val pin = (1..length).map { random.nextInt(10) }.joinToString("")
        val steps = pin.zipWithNext { a, b -> b - a }.toSet()
        if (steps != setOf(0) && steps != setOf(1) && steps != setOf(-1)) return pin
    }
}

internal interface MembersApi {
    @GET(ApiPaths.WORKERS)
    suspend fun workers(@Query("limit") limit: Int): Response<ApiEnvelope<WireItems<WireAccount>>>

    @GET(ApiPaths.FOREMEN)
    suspend fun foremen(@Query("limit") limit: Int): Response<ApiEnvelope<WireItems<WireAccount>>>

    @POST(ApiPaths.WORKERS)
    suspend fun createWorker(@Body body: AccountIn): Response<ApiEnvelope<WireAccount>>

    @POST(ApiPaths.FOREMEN)
    suspend fun createForeman(@Body body: AccountIn): Response<ApiEnvelope<WireAccount>>

    @PATCH(ApiPaths.WORKER)
    suspend fun updateWorker(@Path("user_id") id: String, @Body body: AccountUpdate): Response<ApiEnvelope<WireAccount>>

    @PATCH(ApiPaths.FOREMAN)
    suspend fun updateForeman(@Path("user_id") id: String, @Body body: AccountUpdate): Response<ApiEnvelope<WireAccount>>
}

@Serializable
internal data class WireAccount(
    val id: String,
    val displayName: String,
    val role: String,
    val language: String = "en",
    val active: Boolean = true,
    val mustChangePin: Boolean = false,
)

@Serializable
internal data class AccountIn(val displayName: String, val language: String, val pin: String)

@Serializable
internal data class AccountUpdate(val displayName: String? = null, val pin: String? = null, val active: Boolean? = null)

private fun WireAccount.toMember() = Member(
    id = id,
    name = displayName,
    role = if (role == "foreman") Role.FOREMAN else Role.WORKER,
    language = language,
    active = active,
    mustChangePin = mustChangePin,
)

// shortcut: one page of 100 per role; a shop past 100 people needs the cursor.
private const val PAGE = 100

internal class RealMembersSource(private val api: () -> MembersApi) : MembersSource {
    override suspend fun list(): AppResult<List<Member>> {
        val workers = safeApiCall { api().workers(PAGE) }
        if (workers is AppResult.Failure) return workers
        val foremen = safeApiCall { api().foremen(PAGE) }
        if (foremen is AppResult.Failure) return foremen
        val all = (workers as AppResult.Success).value.items + (foremen as AppResult.Success).value.items
        return AppResult.Success(all.map { it.toMember() })
    }

    override suspend fun add(role: Role, name: String, language: String, pin: String): AppResult<Member> =
        when (val r = safeApiCall {
            val body = AccountIn(name, language, pin)
            if (role == Role.FOREMAN) api().createForeman(body) else api().createWorker(body)
        }) {
            is AppResult.Success -> AppResult.Success(r.value.toMember())
            is AppResult.Failure -> r
        }

    override suspend fun update(member: Member, name: String?, pin: String?, active: Boolean?): AppResult<Member> =
        when (val r = safeApiCall {
            val body = AccountUpdate(name, pin, active)
            if (member.role == Role.FOREMAN) api().updateForeman(member.id, body) else api().updateWorker(member.id, body)
        }) {
            is AppResult.Success -> AppResult.Success(r.value.toMember())
            is AppResult.Failure -> r
        }
}

@Singleton
class FakeMembersSource @Inject constructor() : MembersSource {
    private val people = mutableListOf(
        Member("u_rahim", "Rahim Uddin", Role.WORKER, "en", active = true, mustChangePin = false),
        Member("u_sadia", "Sadia Akter", Role.WORKER, "en", active = true, mustChangePin = true),
        Member("u_tanaka", "田中 健", Role.FOREMAN, "ja", active = true, mustChangePin = false),
    )

    override suspend fun list(): AppResult<List<Member>> = AppResult.Success(people.toList())

    override suspend fun add(role: Role, name: String, language: String, pin: String): AppResult<Member> {
        val m = Member("u_${people.size + 1}", name, role, language, active = true, mustChangePin = true)
        people += m
        return AppResult.Success(m)
    }

    override suspend fun update(member: Member, name: String?, pin: String?, active: Boolean?): AppResult<Member> {
        val i = people.indexOfFirst { it.id == member.id }
        val m = member.copy(
            name = name ?: member.name,
            active = active ?: member.active,
            mustChangePin = if (pin != null) true else member.mustChangePin,
        )
        if (i >= 0) people[i] = m
        return AppResult.Success(m)
    }
}
