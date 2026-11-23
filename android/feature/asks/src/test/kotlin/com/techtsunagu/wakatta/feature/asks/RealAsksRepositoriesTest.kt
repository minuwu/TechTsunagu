package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.asks.data.AsksApi
import com.techtsunagu.wakatta.feature.asks.data.FakeAskRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeMeetupRepository
import com.techtsunagu.wakatta.feature.asks.data.FakeSenpaiRepository
import com.techtsunagu.wakatta.feature.asks.data.RealAskRepository
import com.techtsunagu.wakatta.feature.asks.data.RealMeetupRepository
import com.techtsunagu.wakatta.feature.asks.data.RealSenpaiRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealAsksRepositoriesTest {
    private class Store(user: User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("tok", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }
    private val store = Store(DemoSeed.demoWorker)
    private val api: AsksApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
        .create(AsksApi::class.java)
    private val asks = RealAskRepository({ api }, store, FakeAskRepository(0L))
    private val senpai = RealSenpaiRepository({ api }, store, FakeSenpaiRepository())
    private val meetups = RealMeetupRepository({ api }, store, FakeMeetupRepository(0L))

    @After
    fun tearDown() = server.close()

    private fun ok(data: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private fun ask(id: String, covered: Boolean, forwarded: Boolean = false, reply: String = "null") =
        """{"id":"$id","workerId":"5","text":"q$id","covered":$covered,
        "answer":${if (covered) """{"ja":"あ","en":"a"}""" else "null"},"sourceNoticeIds":${if (covered) """["7"]""" else "[]"},
        "forwarded":$forwarded,"senpaiReply":$reply,"createdAt":"2026-11-24T00:30:00Z"}"""

    private val pair = """{"id":"3","workerId":"5","senpaiName":"Sato","kind":"coworker",
        "createdAt":"2026-11-01T00:00:00Z","expiresAt":"2026-12-01T00:00:00Z"}"""

    @Test
    fun listIsOldestFirstAndMapsCoveredForwardedAndReplied() = runBlocking {
        ok(pair)
        ok("""{"items":[${ask("3", false, true, """{"ja":"はい","en":"Yes"}""")},${ask("2", false, true)},${ask("1", true)}],"nextCursor":null}""")
        val list = asks.getAsks("5").first()
        assertEquals(listOf("1", "2", "3"), list.map { it.id })
        assertEquals(AskStatus.ANSWERED, list[0].status)
        assertEquals("7", list[0].matchNotice?.id)
        assertEquals(AskStatus.FORWARDED, list[1].status)
        assertEquals(AskStatus.REPLIED, list[2].status)
        assertEquals("Yes", list[2].replyEn)
        assertEquals("Sato", list[2].senpaiName)
        assertEquals("11/24 09:30", list[0].createdAt)
    }

    @Test
    fun textAskSendsOnlyTextWithTheSameKeyOnRetryAndForwardsWhenNotCovered() = runBlocking {
        val request = AskCreateRequest(text = " where is the bus? ")
        repeat(2) {
            ok(ask("9", covered = false))
            ok(ask("9", covered = false, forwarded = true))
            ok(pair)
            assertTrue(asks.sendAsk("5", request) is AppResult.Success)
        }
        val first = server.takeRequest()
        server.takeRequest() // forward
        server.takeRequest() // pair
        val retry = server.takeRequest()
        val body = first.body!!.utf8()
        assertTrue(body.contains("name=\"text\"") && body.contains("where is the bus?"))
        assertFalse(body.contains("workerId"))
        assertEquals(request.idempotencyKey, first.headers["Idempotency-Key"])
        assertEquals(request.idempotencyKey, retry.headers["Idempotency-Key"])
    }

    @Test
    fun voiceAskSendsTheRecordingAsAnAudioPart() = runBlocking {
        val file = File.createTempFile("ask", ".m4a").apply { writeBytes(byteArrayOf(1, 2, 3)); deleteOnExit() }
        ok(ask("4", covered = true))
        ok(pair)
        val sent = asks.sendAsk("5", AskCreateRequest(text = "mock", isVoice = true, audioFile = file)) as AppResult.Success
        assertEquals(AskKind.VOICE, sent.value.kind)
        val body = server.takeRequest().body!!.utf8()
        assertTrue(body.contains("name=\"audio\""))
        assertFalse(body.contains("name=\"text\""))
    }

    @Test
    fun noPairIsNullAndMeetupKindsMapBetweenFestivalAndEvent() = runBlocking {
        ok("null")
        assertNull(senpai.getSenpaiPairForWorker("5").first())

        ok(pair)
        ok("""{"id":"8","pairId":"3","kind":"event","date":"2026-11-20","note":"n","loggedBy":"worker"}""")
        val logged = meetups.logMeetup("5", MeetupCreateRequest(kind = "festival", date = "2026-11-20", note = "n")) as AppResult.Success
        assertEquals("festival", logged.value.kind)
        server.takeRequest()
        server.takeRequest()
        server.takeRequest().let {
            val body = it.body!!.utf8()
            assertTrue(body.contains("\"kind\":\"event\""))
            assertFalse(body.contains("workerId"))
        }
    }

    @Test
    fun foremanKeepsTheFakeAndMakesNoCall() = runBlocking {
        val foreman = RealAskRepository({ api }, Store(DemoSeed.demoForeman), FakeAskRepository(0L))
        assertTrue(foreman.getAsks(null).first().isNotEmpty())
        assertEquals(0, server.requestCount)
    }
}
