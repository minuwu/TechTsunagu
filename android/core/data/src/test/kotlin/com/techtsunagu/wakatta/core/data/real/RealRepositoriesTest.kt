package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

private class MemoryStore(role: Role) : SessionStore {
    private val state = MutableStateFlow<Session?>(
        Session("tok", user = User("12", "Rahim", role, "bn")),
    )
    override val session: StateFlow<Session?> = state
    override suspend fun save(session: Session) { state.value = session }
    override suspend fun clear() { state.value = null }
    override fun getCachedToken(): String? = state.value?.token
}

class RealRepositoriesTest {
    private val server = MockWebServer().also { it.start() }
    private val store = MemoryStore(Role.WORKER)
    private val api: SharedDataApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
        .create(SharedDataApi::class.java)

    @After
    fun tearDown() = server.close()

    private fun ok(data: String) = server.enqueue(
        MockResponse.Builder().code(200).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private fun machine(id: String) =
        """{"id":"$id","code":"press-$id","name":{"ja":"M$id"},"currentVersionId":"4$id","activeWorkerId":null}"""

    private val passport = """{"workerId":"12","displayName":"Rahim","machines":[{"id":"1","code":"press-1","name":{"ja":"M1"}},""" +
        """{"id":"2","code":"press-2","name":{"ja":"M2"}}],"cells":[""" +
        """{"machineId":"1","status":"expiring","passedAt":"2026-11-01T00:00:00Z"},{"machineId":"2","status":"none","passedAt":null}]}"""

    @Test
    fun workerReadyComesFromOwnPassportAndPagesAreFollowed() = runBlocking {
        ok("""{"items":[${machine("1")}],"nextCursor":"c2"}""")
        ok("""{"items":[${machine("2")}],"nextCursor":null}""")
        ok(passport)
        val result = RealMachineRepository({ api }, store).getMachines()
        val machines = (result as AppResult.Success).value
        assertEquals(listOf(true, false), machines.map { it.isReady })
        server.takeRequest()
        assertEquals("200", server.takeRequest().url.queryParameter("limit"))
    }

    @Test
    fun passportKeepsOnlyMachinesWithASignOff() = runBlocking {
        ok(passport)
        val signOffs = (RealSignOffRepository { api }.getSignOffs("12") as AppResult.Success).value
        assertEquals(1, signOffs.size)
        assertEquals(SkillStatus.EXPIRING, signOffs[0].status)
        assertEquals("M1", signOffs[0].machineName["ja"])
    }

    @Test
    fun checkSendsServerShapeAndTheSameKeyOnRetry() = runBlocking {
        val repo = RealCheckRepository { api }
        val reply = """{"passed":false,"wrong":[{"id":"q2","termId":null,"prompt":{"ja":"x"}}],"signOff":null}"""
        ok(reply)
        ok(reply)
        val first = repo.submitCheck(CheckTargetKind.NOTE, "41", "bn", listOf(1, 0), "key-1")
        repo.submitCheck(CheckTargetKind.NOTE, "41", "bn", listOf(1, 0), "key-1")
        val result = (first as AppResult.Success).value
        assertFalse(result.passed)
        assertEquals(listOf("q2"), result.wrongQuestions)
        assertFalse(result.signOffGranted)
        val request = server.takeRequest()
        assertEquals("""{"target":{"kind":"note","id":"41"},"language":"bn","answers":[1,0]}""", request.body?.utf8())
        assertEquals("key-1", request.headers["Idempotency-Key"])
        assertEquals("key-1", server.takeRequest().headers["Idempotency-Key"])
    }

    @Test
    fun receiptForANoteIsRefusedWithoutACall() = runBlocking {
        val result = RealReceiptRepository { api }.markOpened(CheckTargetKind.NOTE, "41")
        assertEquals(AppError.Validation(listOf("kind")), (result as AppResult.Failure).error)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun serverErrorIsMapped() = runBlocking {
        server.enqueue(
            MockResponse.Builder().code(403)
                .body("""{"success":false,"error":{"code":"FORBIDDEN","message":"m","details":{}}}""").build(),
        )
        val result = RealSignOffRepository { api }.getSignOffs("99")
        assertEquals(AppError.Forbidden, (result as AppResult.Failure).error)
    }
}
