package com.techtsunagu.wakatta.feature.nearmiss

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.nearmiss.data.NearMissApi
import com.techtsunagu.wakatta.feature.nearmiss.data.RealNearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RealNearMissRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class Store(user: User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("tok", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }

    private fun repo(user: User): RealNearMissRepository {
        val store = Store(user)
        val api = ApiClientFactory
            .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
            .create(NearMissApi::class.java)
        return RealNearMissRepository { api }
    }

    @After
    fun tearDown() = server.close()

    private fun ok(data: String) = server.enqueue(
        MockResponse.Builder().code(200).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private fun created(data: String) = server.enqueue(
        MockResponse.Builder().code(201).body("""{"success":true,"data":$data,"message":"created"}""").build(),
    )

    private fun reportJson(id: String, ackAt: String? = null) =
        """{"id":"$id","workerId":"2","machineId":"12","text":{"en":"Slipped on oil"},"workerName":"Rahim","severity":"high","photoMark":{"x":0.5,"y":0.25},"taskId":null,"photo":"blob-$id","createdAt":"2026-11-24T00:00:00Z","acknowledgedAt":${if (ackAt != null) "\"$ackAt\"" else "null"}}"""

    @Test
    fun createNearMissSendsMultipartPartsAndReusesIdempotencyKeyOnRetry() = runBlocking {
        repeat(2) { created(reportJson("1")) }
        val worker = repo(DemoSeed.demoWorker)
        val dummyPhoto = tempFolder.newFile("photo.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val dummyAudio = tempFolder.newFile("audio.m4a").apply { writeBytes(byteArrayOf(4, 5, 6)) }

        val request = NearMissCreateRequest(
            machineId = "12",
            text = "Slipped near machine 12",
            severity = NearMissSeverity.HIGH,
            photoFile = dummyPhoto,
            photoMark = PhotoMark(50f, 25f),
            audioFile = dummyAudio,
            idempotencyKey = "nm-key-1",
        )

        repeat(2) {
            val result = worker.createNearMiss(request)
            assertTrue(result is AppResult.Success)
            val dto = (result as AppResult.Success).value
            assertEquals("1", dto.id)
            assertEquals(NearMissSeverity.HIGH, dto.severity)
            assertEquals(NearMissStatus.OPEN, dto.status)
        }

        val first = server.takeRequest()
        val retry = server.takeRequest()

        val firstBody = first.body!!.utf8()
        assertTrue(firstBody.contains("name=\"machineId\""))
        assertTrue(firstBody.contains("12"))
        assertTrue(firstBody.contains("name=\"text\""))
        assertTrue(firstBody.contains("Slipped near machine 12"))
        assertTrue(firstBody.contains("name=\"audio\""))
        assertTrue(firstBody.contains("name=\"photo\""))
        assertTrue(Regex("name=\"severity\"[\\s\\S]{1,80}high").containsMatchIn(firstBody))
        assertTrue(Regex("name=\"photoMarkX\"[\\s\\S]{1,80}0\\.5").containsMatchIn(firstBody))
        assertTrue(Regex("name=\"photoMarkY\"[\\s\\S]{1,80}0\\.25").containsMatchIn(firstBody))

        assertEquals("nm-key-1", first.headers["Idempotency-Key"])
        assertEquals("nm-key-1", retry.headers["Idempotency-Key"])
    }

    @Test
    fun getNearMissesForemanGetsReportsFromServerAndReplacesPhotoUrl() = runBlocking {
        ok("""{"items":[${reportJson("10")},${reportJson("11", "2026-11-24T01:00:00Z")}],"nextCursor":null}""")
        val foreman = repo(DemoSeed.demoForeman)
        val result = foreman.getNearMisses()
        assertTrue(result is AppResult.Success)

        val list = (result as AppResult.Success).value
        assertEquals(2, list.size)
        assertEquals("10", list[0].id)
        assertEquals(NearMissStatus.OPEN, list[0].status)
        assertEquals("/api/near-misses/10/photo", list[0].photoUrl)

        assertEquals("11", list[1].id)
        assertEquals(NearMissStatus.ACK, list[1].status)
        assertEquals("2026-11-24T01:00:00Z", list[1].acknowledgedAt)
    }

    @Test
    fun aWorkerReadsTheirReportsFromTheServer() = runBlocking {
        ok("""{"items":[${reportJson("99")}],"nextCursor":null}""")
        val mine = (repo(DemoSeed.demoWorker).getNearMisses() as AppResult.Success).value.single()
        assertEquals("Rahim", mine.userName)
        assertEquals(NearMissSeverity.HIGH, mine.severity)
        assertEquals(PhotoMark(50f, 25f), mine.photoMark)
    }

    @Test
    fun linkTaskPutsTheTaskId() = runBlocking {
        ok(reportJson("42"))
        assertTrue(repo(DemoSeed.demoForeman).linkTask("42", "7") is AppResult.Success)
        val sent = server.takeRequest()
        assertEquals("PUT /api/near-misses/42/task", "${sent.method} ${sent.url.encodedPath}")
        assertEquals("""{"taskId":"7"}""", sent.body!!.utf8())
    }

    @Test
    fun acknowledgeCallsAckEndpointAndReturnsAckResult() = runBlocking {
        ok(reportJson("42", "2026-11-24T02:00:00Z"))
        val foreman = repo(DemoSeed.demoForeman)
        val result = foreman.acknowledge("42", "u_tanaka")
        assertTrue(result is AppResult.Success)

        val ack = (result as AppResult.Success).value
        assertEquals("42", ack.id)
        assertEquals(NearMissStatus.ACK, ack.status)
        assertEquals("2026-11-24T02:00:00Z", ack.acknowledgedAt)
        assertEquals("u_tanaka", ack.acknowledgedBy)

        val sent = server.takeRequest()
        assertEquals("/api/near-misses/42/ack", sent.url.encodedPath)
        assertEquals("POST", sent.method)
    }

    @Test
    fun getNearMissReturnsNotFoundWhenIdMissing() = runBlocking {
        ok(reportJson("1"))
        val foreman = repo(DemoSeed.demoForeman)
        assertEquals("1", (foreman.getNearMiss("1") as AppResult.Success).value.id)
        assertEquals("/api/near-misses/1", server.takeRequest().url.encodedPath)

        server.enqueue(MockResponse.Builder().code(404).body("""{"success":false,"error":{"code":"NOT_FOUND","message":"Not found"}}""").build())
        assertEquals(AppResult.Failure(AppError.NotFound), foreman.getNearMiss("999"))
    }
}
