package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.asks.data.KnowledgeApi
import com.techtsunagu.wakatta.feature.asks.data.RealChatbotRepository
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeReplyKind
import com.techtsunagu.wakatta.feature.asks.model.kind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The chatbot against the server's real answer shape (`POST /api/knowledge/asks`, docs/openapi.yaml). */
class RealChatbotRepositoryTest {
    private class Store : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("tok", user = DemoSeed.demoWorker))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }
    private val api: KnowledgeApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, Store(), debug = false))
        .create(KnowledgeApi::class.java)
    private val bot = RealChatbotRepository { api }

    @After
    fun tearDown() = server.close()

    @Test
    fun sendsTheQuestionAndMachineAndReadsACitedAnswer() = runBlocking {
        server.enqueue(
            MockResponse.Builder().code(200).body(
                """{"success":true,"message":"ok","data":{"question":"Why stop the machine?","covered":true,"emergency":false,
                "answer":{"ja":"止めてください。","en":"Stop it first."},
                "sources":[{"id":"mhlw-safety:p15:1","kind":"safety","title":"For Work Safety and Health","page":15,
                "url":"https://www.mhlw.go.jp/x.pdf","attribution":"出典：厚生労働省"}],
                "check":{"id":"q1","termId":null,"prompt":{"ja":"何をしますか","en":"What do you do?"},
                "options":[{"ja":"止める","en":"Stop"},{"ja":"動かす","en":"Run"},{"ja":"手袋","en":"Gloves"}],"correctIndex":0}}}""",
            ).build(),
        )

        val res = bot.ask("Why stop the machine?", null, "2")

        val answer = (res as AppResult.Success).value
        assertEquals(KnowledgeReplyKind.COVERED, answer.kind)
        assertEquals(15, answer.sources.single().page)
        assertEquals(0, answer.check!!.correctIndex)
        val request = server.takeRequest()
        assertEquals("/api/knowledge/asks", request.url.encodedPath)
        val body = request.body!!.utf8()
        assertTrue(body.contains("name=\"text\"") && body.contains("Why stop the machine?"))
        assertTrue(body.contains("name=\"machineId\"") && body.contains("\r\n2\r\n"))
    }

    @Test
    fun anEmptyQuestionNeverReachesTheServer() = runBlocking {
        assertEquals(AppResult.Failure(AppError.Validation(listOf("text"))), bot.ask("  ", null, null))
        assertEquals(0, server.requestCount)
    }
}
