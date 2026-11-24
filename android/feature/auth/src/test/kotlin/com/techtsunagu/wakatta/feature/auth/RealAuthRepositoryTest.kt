package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.core.network.AuthApi
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealAuthRepositoryTest {

    private val server = MockWebServer().also { it.start() }
    private val store = MemorySessionStore()
    private val api: AuthApi = ApiClientFactory
        .retrofit(
            server.url("/").toString(),
            ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false),
        )
        .create(AuthApi::class.java)
    private val repository = RealAuthRepository({ api }, store)

    private val existing = Session(
        token = "old",
        user = User(id = "1", displayName = "Old", role = Role.WORKER, language = "en"),
    )

    @After
    fun tearDown() = server.close()

    private fun reply(code: Int, body: String) {
        server.enqueue(MockResponse.Builder().code(code).body(body).build())
    }

    private fun ok(data: String) = reply(200, """{"success":true,"data":$data,"message":"ok"}""")

    private fun error(code: Int, errorCode: String, details: String = "{}") =
        reply(code, """{"success":false,"error":{"code":"$errorCode","message":"m","details":$details}}""")

    private fun loginBody(mustChangePin: Boolean) =
        """{"token":"tok-9","expiresAt":"2026-12-22T10:00:00Z","mustChangePin":$mustChangePin,""" +
            """"user":{"id":"12","displayName":"Rahim Uddin","role":"worker","language":"bn","shopId":"3","districtId":null}}"""

    @Test
    fun registeringKeepsTheSessionUntilTheOwnerHasSeenTheCode() = runBlocking { // M11
        ok(
            """{"token":"tok-new","expiresAt":null,"shopCode":"MINATO7",""" +
                """"user":{"id":"40","displayName":"Mori","role":"foreman","language":"ja","shopId":"9","districtId":null}}""",
        )
        val result = repository.register("Minato Works", "Mori", "739215")
        assertEquals(AppResult.Success("MINATO7"), result)
        assertNull(store.session.value) // a session would switch the app to Home and hide the code

        repository.finishRegister()
        assertEquals("tok-new", store.session.value?.token)
    }

    @Test
    fun loginSuccessSavesTheSession() = runBlocking {
        ok(loginBody(mustChangePin = true))
        val result = repository.signIn("12", "9137")
        assertTrue(result.isSuccess)
        val saved = store.session.value
        assertEquals("tok-9", saved?.token)
        assertEquals(Role.WORKER, saved?.user?.role)
        assertEquals("en", saved?.user?.language) // the app ships ja and en only (ADR 0016)
        assertEquals("3", saved?.user?.shopId)
        assertTrue(saved?.mustChangePin == true)
    }

    @Test
    fun loginSendsOnlyUserIdAndPin() = runBlocking {
        ok(loginBody(mustChangePin = false))
        repository.signIn("12", "9137")
        val request = server.takeRequest()
        assertEquals("""{"userId":"12","pin":"9137"}""", request.body?.utf8())
        assertNull(request.headers["Authorization"])
    }

    @Test
    fun wrongPinKeepsTheCurrentSession() = runBlocking {
        store.save(existing)
        error(401, "UNAUTHORIZED")
        val result = repository.signIn("12", "0000")
        assertEquals(AppError.WrongPin, (result as AppResult.Failure).error)
        assertNotNull(store.session.value)
        assertEquals("old", store.session.value?.token)
    }

    @Test
    fun pinLockCarriesTheUnlockTime() = runBlocking {
        error(423, "PIN_LOCKED", """{"until":"2026-12-22T10:15:00Z"}""")
        val result = repository.signIn("12", "0000")
        assertEquals(AppError.PinLocked("2026-12-22T10:15:00Z"), (result as AppResult.Failure).error)
        assertNull(store.session.value)
    }

    @Test
    fun rosterUsesCodeAndRoleQueryAndMapsNames() = runBlocking {
        ok("""[{"id":"12","displayName":"Rahim Uddin"},{"id":"13","displayName":"Sadia Akter"}]""")
        val result = repository.roster("SASEBO01", Role.WORKER)
        val names = (result as AppResult.Success).value.map { it.displayName }
        assertEquals(listOf("Rahim Uddin", "Sadia Akter"), names)
        val url = server.takeRequest().url
        assertEquals("SASEBO01", url.queryParameter("code"))
        assertEquals("worker", url.queryParameter("role"))
    }

    @Test
    fun unknownShopCodeIsNotFound() = runBlocking {
        error(404, "NOT_FOUND")
        val result = repository.roster("NOSUCH01", Role.FOREMAN)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }

    @Test
    fun changePinClearsTheMustChangeFlag() = runBlocking {
        store.save(existing.copy(mustChangePin = true))
        ok("null")
        val result = repository.changePin("1111", "9137")
        assertTrue(result.isSuccess)
        assertFalse(store.session.value!!.mustChangePin)
        assertEquals("""{"currentPin":"1111","newPin":"9137"}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun wrongCurrentPinKeepsTheSession() = runBlocking {
        store.save(existing.copy(mustChangePin = true))
        error(401, "UNAUTHORIZED")
        val result = repository.changePin("0000", "9137")
        assertEquals(AppError.WrongPin, (result as AppResult.Failure).error)
        assertTrue(store.session.value!!.mustChangePin)
    }

    @Test
    fun signOutClearsEvenWhenTheServerIsDown() = runBlocking {
        store.save(existing)
        server.close()
        repository.signOut()
        assertNull(store.session.value)
    }

    @Test
    fun signOutSendsTheBearerToken() = runBlocking {
        store.save(existing)
        ok("null")
        repository.signOut()
        assertEquals("Bearer old", server.takeRequest().headers["Authorization"])
        assertNull(store.session.value)
    }
}
