package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Tag

@Serializable
private data class Payload(val text: String, val dueAt: String? = null)

@Serializable
private data class Clear(val workerId: String?)

private interface TestApi {
    @POST(ApiPaths.CHECKS)
    suspend fun create(@Tag key: IdempotencyKey, @Body body: Payload): Response<ApiEnvelope<Unit>>

    @PUT(ApiPaths.SHIFT_ASSIGNMENTS)
    suspend fun clearSlot(@ExplicitNulls @Body body: Clear): Response<ApiEnvelope<Unit>>

    @POST(ApiPaths.MACHINES)
    suspend fun plainNull(@Body body: Clear): Response<ApiEnvelope<Unit>>

    @GET(ApiPaths.MACHINE_PANEL)
    suspend fun panel(
        @Path("machine_id") id: String,
        @Header("If-None-Match") ifNoneMatch: String?,
    ): Response<ResponseBody>
}

class ClientBehaviourTest {

    private val Harness.testApi: TestApi get() = retrofit.create(TestApi::class.java)

    @Test fun appHeadersAndBearerAreSent() = Harness().use { h ->
        h.enqueueOk("""{"id":"1","displayName":"A","role":"worker","language":"en"}""")
        runBlocking { h.api.me() }
        val request = h.server.takeRequest()
        assertEquals("1.2.3", request.headers["X-App-Version"])
        assertEquals("android", request.headers["X-App-Platform"])
        assertEquals("ja", request.headers["Accept-Language"])
        assertEquals("Bearer tok-1", request.headers["Authorization"])
    }

    @Test fun loginAndRosterSkipBearer() = Harness().use { h ->
        h.enqueueOk("[]")
        h.enqueueError(401, "UNAUTHORIZED")
        runBlocking {
            h.api.roster("SASEBO01", "worker")
            h.api.login(LoginRequest("1", "0000"))
        }
        assertNull(h.server.takeRequest().headers["Authorization"])
        assertNull(h.server.takeRequest().headers["Authorization"])
    }

    @Test fun wrongPinOnLoginDoesNotClearSession() = Harness().use { h ->
        h.enqueueError(401, "UNAUTHORIZED")
        val result = runBlocking { safeApiCall { h.api.login(LoginRequest("1", "0000")) } }
        assertEquals(AppError.WrongPin, (result as AppResult.Failure).error)
        assertNotNull(h.store.session.value)
    }

    @Test fun wrongCurrentPinOnPinChangeDoesNotClearSession() = Harness().use { h ->
        h.enqueueError(401, "UNAUTHORIZED")
        val result = runBlocking { safeApiCallUnit { h.api.changePin(ChangePinRequest("1111", "2222")) } }
        assertEquals(AppError.WrongPin, (result as AppResult.Failure).error)
        assertNotNull(h.store.session.value)
    }

    @Test fun unauthorizedElsewhereClearsSession() = Harness().use { h ->
        h.enqueueError(401, "UNAUTHORIZED")
        runBlocking { safeApiCall { h.api.me() } }
        assertNull(h.store.session.value)
    }

    @Test fun idempotencyKeyIsReusedOnRetry() = Harness().use { h ->
        h.enqueueError(503, "BUSY")
        h.enqueueOk("null")
        val key = IdempotencyKey("11111111-2222-4333-8444-555555555555")
        runBlocking {
            h.testApi.create(key, Payload("a"))
            h.testApi.create(key, Payload("a"))
        }
        val first = h.server.takeRequest().headers["Idempotency-Key"]
        val second = h.server.takeRequest().headers["Idempotency-Key"]
        assertEquals(key.value, first)
        assertEquals(first, second)
    }

    @Test fun noKeyTagMeansNoHeader() = Harness().use { h ->
        h.enqueueOk("""{"id":"1","displayName":"A"}""")
        runBlocking { h.api.me() }
        assertNull(h.server.takeRequest().headers["Idempotency-Key"])
    }

    @Test fun defaultsAndNullsAreNotSentByDefault() = Harness().use { h ->
        h.enqueueOk("null")
        runBlocking { h.testApi.create(IdempotencyKey("k"), Payload("a")) }
        assertEquals("""{"text":"a"}""", h.server.takeRequest().body?.utf8())
    }

    @Test fun explicitNullIsSentWhenMarked() = Harness().use { h ->
        h.enqueueOk("null")
        h.enqueueOk("null")
        runBlocking {
            h.testApi.clearSlot(Clear(null))
            h.testApi.plainNull(Clear(null))
        }
        assertEquals("""{"workerId":null}""", h.server.takeRequest().body?.utf8())
        assertEquals("{}", h.server.takeRequest().body?.utf8())
    }

    @Test fun notModifiedReturnsCachedBody() = Harness().use { h ->
        val cache = TestCacheStore()
        val conditional = ConditionalGet(cache)
        val body = """{"success":true,"data":{"id":"m1"},"message":"ok"}"""
        h.enqueue(200, body, "ETag" to "\"abc\"")
        h.enqueue(304, "", "ETag" to "\"abc\"")
        val serializer = PanelDto.serializer()
        val first = runBlocking { conditional.get("panel:m1", serializer) { h.testApi.panel("m1", it) } }
        val second = runBlocking { conditional.get("panel:m1", serializer) { h.testApi.panel("m1", it) } }
        assertEquals("m1", (first as AppResult.Success).value.id)
        assertEquals(first, second)
        assertNull(h.server.takeRequest().headers["If-None-Match"])
        assertEquals("\"abc\"", h.server.takeRequest().headers["If-None-Match"])
        assertEquals("\"abc\"", cache.entries["panel:m1"]?.etag)
    }

    @Test fun errorOnConditionalGetIsMapped() = Harness().use { h ->
        h.enqueueError(404, "NOT_FOUND")
        val result = runBlocking {
            ConditionalGet(TestCacheStore()).get("k", PanelDto.serializer()) { h.testApi.panel("x", it) }
        }
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }

    @Test fun unknownEnumValueDoesNotCrash() {
        val decoded = WakattaJson.decodeFromString(
            Thing.serializer(),
            """{"kind":"brand_new","extra":1,"count":null}""",
        )
        assertEquals(Kind.UNKNOWN, decoded.kind)
        assertEquals(0, decoded.count)
    }

    @Test fun unknownRoleBecomesUnknownRole() = Harness().use { h ->
        h.enqueueOk(
            """{"token":"t","expiresAt":"2026-12-22T10:00:00Z","mustChangePin":false,""" +
                """"user":{"id":"1","displayName":"A","role":"janitor","language":"xx","shopId":null,"districtId":null}}""",
        )
        val result = runBlocking { safeApiCall { h.api.login(LoginRequest("1", "0000")) } }
        val session = (result as AppResult.Success).value.toSession()
        assertEquals(com.techtsunagu.wakatta.core.model.Role.UNKNOWN, session.user.role)
    }

    @Test fun baseUrlOverrideWinsOnlyWhenNotEmpty() {
        assertEquals(ApiConfig.BASE_URL, ApiConfig.baseUrl(""))
        assertEquals(ApiConfig.BASE_URL, ApiConfig.baseUrl("  "))
        assertEquals("http://10.0.2.2:8000", ApiConfig.baseUrl("http://10.0.2.2:8000/"))
        assertTrue(!ApiConfig.BASE_URL.endsWith("/"))
    }
}

@Serializable
private data class PanelDto(val id: String)

@Serializable
private enum class Kind { A, UNKNOWN }

@Serializable
private data class Thing(val kind: Kind = Kind.UNKNOWN, val count: Int = 0)
