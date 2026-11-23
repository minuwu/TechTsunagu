package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealChatRepositoryTest {
    private class TestSession(user: User) : SessionStore {
        private val flow = MutableStateFlow<Session?>(Session("tok", user = user))
        override val session: StateFlow<Session?> = flow
        override suspend fun save(session: Session) { flow.value = session }
        override suspend fun clear() { flow.value = null }
        override fun getCachedToken(): String? = flow.value?.token
    }

    private val server = MockWebServer().also { it.start() }
    private val foreman = User("1", "Yamaguchi", Role.FOREMAN, "ja")
    private val worker = User("12", "Rahim", Role.WORKER, "en")

    private fun repo(user: User, pollMs: Long = 5L): RealChatRepository {
        val session = TestSession(user)
        val api = ApiClientFactory
            .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, session, debug = false))
            .create(ChatApi::class.java)
        val workers = object : WorkerRepository {
            override suspend fun getWorkers() = AppResult.Success(listOf(WorkerInfo("12", "Rahim", "bn")))
        }
        val glossary = object : GlossaryRepository {
            override suspend fun getTerms() = AppResult.Success(emptyList<GlossaryTerm>())
            override suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String) =
                AppResult.Success(GlossaryTerm(termJa, readingJa, termBn, termEn))
        }
        return RealChatRepository({ api }, session, workers, glossary, FakeChatRepository(), pollMs)
    }

    @After
    fun tearDown() = server.close()

    private fun ok(data: String, etag: String? = null, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).apply {
            if (code != 304) body("""{"success":true,"data":$data,"message":"ok"}""")
            etag?.let { addHeader("ETag", it) }
        }.build(),
    )

    private val question = """"id":"q1","termId":null,"prompt":{"ja":"Q"},"options":[{"ja":"a"},{"ja":"b"},{"ja":"c"}]"""

    private fun draft(sentAt: String?) =
        """{"id":"91","threadId":"7","fromUserId":"1","kind":"text","language":"ja","stage":3,""" +
            """"needsEdit":false,"transcriptJa":null,"text":{"ja":"hello","en":"hi"},"toUserId":null,"refMessageId":null,""" +
            """"createdAt":"2026-11-23T08:00:00Z","sentAt":${sentAt?.let { "\"$it\"" }},"questions":[{$question,"correctIndex":0}],""" +
            """"taskDrafts":[{"title":{"ja":"t"},"assigneeIds":[],"dueAt":null,"machineId":null}]}"""

    @Test
    fun foremanSendMakesTheDraftWithTheKeyThenSendsItWithTheAnswerKey() = runBlocking {
        ok(draft(null))
        ok(draft("2026-11-23T08:00:05Z"))
        val result = repo(foreman).sendMessage("7", "hello", false, "1", "key-1") as AppResult.Success
        assertEquals("91", result.value.id)
        val post = server.takeRequest()
        assertEquals("key-1", post.headers["Idempotency-Key"])
        assertTrue(post.headers["Content-Type"]!!.startsWith("multipart/form-data"))
        val send = server.takeRequest().body!!.utf8()
        // Every CheckQuestion field is required: the null termId and correctIndex 0 must be on the wire.
        assertTrue(send.contains(""""termId":null"""))
        assertTrue(send.contains(""""correctIndex":0"""))
    }

    @Test
    fun aRepeatedSendThatAnswersAlreadySentIsSuccess() = runBlocking {
        ok(draft(null))
        server.enqueue(
            MockResponse.Builder().code(409)
                .body("""{"success":false,"error":{"code":"ALREADY_SENT","message":"sent"}}""").build(),
        )
        assertTrue(repo(foreman).sendMessage("7", "hello", false, "1", "key-1").isSuccess)
    }

    @Test
    fun workerPollMapsStatusCaseAndSkipsA304() = runBlocking {
        val msg = """{"id":"91","threadId":"7","fromUserId":"1","kind":"text","text":{"ja":"j","en":"e"},""" +
            """"createdAt":"2026-11-23T08:00:00Z","sentAt":"2026-11-23T08:00:05Z","questions":[{$question}],""" +
            """"myStatus":"green","nudged":true}"""
        ok("""{"items":[$msg]}""", etag = "\"a\"")
        ok("null", etag = "\"a\"", code = 304)
        ok("""{"items":[$msg,$msg]}""", etag = "\"b\"")
        val lists = repo(worker).getMessages("7").take(2).toList()
        assertEquals(1, lists[0].size)
        assertEquals(2, lists[1].size)
        val first = lists[0][0]
        assertEquals(WakattaStatus.GREEN, first.workerAnswerStatus)
        assertTrue(first.isNudged)
        assertEquals("e", first.translatedText)
        server.takeRequest()
        assertEquals("\"a\"", server.takeRequest().headers["If-None-Match"])
    }

    @Test
    fun answersSendOnlyTheAnswersAndMapTheStatus() = runBlocking {
        ok("""{"status":"orange","followUpIds":["4"]}""")
        val r = repo(worker).submitAnswers("91", "12", listOf(1, -1), "k") as AppResult.Success
        assertEquals(WakattaStatus.ORANGE, r.value.status)
        assertEquals(listOf("4"), r.value.followUpIds)
        assertEquals("""{"answers":[1,-1]}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun nudgeAndReexplainSendOnlyTheFieldsTheSchemaAllows() = runBlocking {
        ok("null")
        ok(draft("2026-11-23T08:00:05Z"))
        val repo = repo(foreman)
        assertTrue(repo.nudge("91", "12").isSuccess)
        assertTrue(repo.reexplain("91", ReexplainRequest("12", "term", "easy", emptyMap(), emptyMap(), emptyList())).isSuccess)
        assertEquals("""{"userId":"12"}""", server.takeRequest().body?.utf8())
        assertEquals("""{"userId":"12","termJa":"term"}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun aRateLimitedNudgeIsReportedAndAWorkerNeverCallsFollowUps() = runBlocking {
        server.enqueue(
            MockResponse.Builder().code(429).addHeader("Retry-After", "600")
                .body("""{"success":false,"error":{"code":"RATE_LIMITED","message":"slow"}}""").build(),
        )
        assertEquals(AppResult.Failure(AppError.RateLimited(600)), repo(foreman).nudge("91", "12"))
        assertTrue((repo(worker).getFollowUps() as AppResult.Success).value.isEmpty())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun threadListMapsAndAFailureShowsAnEmptyList() = runBlocking {
        ok("""[{"id":"7","titleJa":"Line 2","kind":"group","memberIds":["12"],"unread":2,"lastMessageAt":null}]""")
        val rows = repo(worker).getThreadSummaries().first()
        assertEquals("7", rows[0].id)
        assertEquals(2, rows[0].unreadCount)
        server.enqueue(MockResponse.Builder().code(500).build())
        assertFalse(repo(worker).getThreadSummaries().first().isNotEmpty())
    }
}
