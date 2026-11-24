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
import java.io.File
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
        return RealChatRepository({ api }, session, workers, glossary, MemoryCacheStore(), pollMs)
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

    private fun draft(sentAt: String?, needsEdit: Boolean = false, textJa: String = "hello") =
        """{"id":"91","threadId":"7","fromUserId":"1","kind":"text","language":"ja","stage":3,""" +
            """"needsEdit":$needsEdit,"transcriptJa":null,"text":{"ja":"$textJa","en":"hi"},"toUserId":null,"refMessageId":null,""" +
            """"createdAt":"2026-11-23T08:00:00Z","sentAt":${sentAt?.let { "\"$it\"" }},"questions":[{$question,"correctIndex":0}],""" +
            """"taskDrafts":[{"title":{"ja":"t"},"assigneeIds":[],"dueAt":null,"machineId":null}]}"""

    @Test
    fun foremanSendMakesTheDraftWithTheKeyThenSendsItWithTheAnswerKey() = runBlocking {
        ok(draft(null))
        ok(draft("2026-11-23T08:00:05Z"))
        val repo = repo(foreman)
        val result = repo.sendMessage("7", "hello", false, "1", "key-1") as AppResult.Success
        assertEquals("91", result.value.id)
        assertTrue(result.value.isDraft)
        assertEquals(1, server.requestCount)
        assertFalse((repo.sendDraft("91") as AppResult.Success).value.isDraft)
        val post = server.takeRequest()
        assertEquals("key-1", post.headers["Idempotency-Key"])
        assertTrue(post.headers["Content-Type"]!!.startsWith("multipart/form-data"))
        val send = server.takeRequest().body!!.utf8()
        // Every CheckQuestion field is required: the null termId and correctIndex 0 must be on the wire.
        assertTrue(send.contains(""""termId":null"""))
        assertTrue(send.contains(""""correctIndex":0"""))
    }

    @Test
    fun voiceMessageSendsOnlyAudioPartWithIdempotencyKey() = runBlocking {
        ok(draft(null))
        val audioFile = File.createTempFile("test_voice_", ".m4a").apply {
            writeBytes("test audio bytes".toByteArray())
        }
        try {
            val result = repo(foreman).sendMessage(
                threadId = "7",
                text = null,
                audio = audioFile,
                isVoice = true,
                fromUserId = "1",
                idempotencyKey = "key-audio-1",
            ) as AppResult.Success
            assertEquals("91", result.value.id)
            val post = server.takeRequest()
            assertEquals("key-audio-1", post.headers["Idempotency-Key"])
            assertTrue(post.headers["Content-Type"]!!.startsWith("multipart/form-data"))
            val bodyString = post.body!!.utf8()
            assertTrue(bodyString.contains("""name="audio""""))
            assertTrue(bodyString.contains("test audio bytes"))
            assertFalse(bodyString.contains("""name="text""""))
        } finally {
            audioFile.delete()
        }
    }

    @Test
    fun workerVoiceMessageSendsImmediatelyWithoutSendCall() = runBlocking {
        ok(draft("2026-11-23T08:00:05Z"))
        val audioFile = File.createTempFile("test_worker_voice_", ".m4a").apply {
            writeBytes("worker voice data".toByteArray())
        }
        try {
            val result = repo(worker).sendMessage(
                threadId = "7",
                text = null,
                audio = audioFile,
                isVoice = true,
                fromUserId = "12",
                idempotencyKey = "key-worker-1",
            ) as AppResult.Success
            assertEquals("91", result.value.id)
            val post = server.takeRequest()
            assertEquals("key-worker-1", post.headers["Idempotency-Key"])
            val bodyString = post.body!!.utf8()
            assertTrue(bodyString.contains("""name="audio""""))
            assertFalse(bodyString.contains("""name="text""""))
            // Worker gets sentAt directly, so no /send request is made
            assertEquals(1, server.requestCount)
        } finally {
            audioFile.delete()
        }
    }

    @Test
    fun sendingBothOrNeitherTextAndAudioFailsValidation() = runBlocking {
        val audioFile = File.createTempFile("test_both_", ".m4a")
        try {
            val both = repo(foreman).sendMessage(
                threadId = "7",
                text = "hello",
                audio = audioFile,
                isVoice = true,
                fromUserId = "1",
                idempotencyKey = "key-fail-1",
            )
            assertTrue(both is AppResult.Failure && (both as AppResult.Failure).error is AppError.Validation)

            val neither = repo(foreman).sendMessage(
                threadId = "7",
                text = null,
                audio = null,
                isVoice = false,
                fromUserId = "1",
                idempotencyKey = "key-fail-2",
            )
            assertTrue(neither is AppResult.Failure && (neither as AppResult.Failure).error is AppError.Validation)
        } finally {
            audioFile.delete()
        }
    }

    @Test
    fun aRepeatedSendThatAnswersAlreadySentIsSuccess() = runBlocking {
        ok(draft(null))
        server.enqueue(
            MockResponse.Builder().code(409)
                .body("""{"success":false,"error":{"code":"ALREADY_SENT","message":"sent"}}""").build(),
        )
        val repo = repo(foreman)
        repo.sendMessage("7", "hello", false, "1", "key-1")
        assertFalse((repo.sendDraft("91") as AppResult.Success).value.isDraft)
    }

    @Test
    fun aDraftLeftFromAnEarlierTimeoutIsSentFromThePoll() = runBlocking {
        ok("""{"items":[${draft(null, needsEdit = true)}]}""", etag = "e1")
        ok(draft("2026-11-23T08:00:05Z"))
        val repo = repo(foreman, pollMs = 60_000L)
        val shown = repo.getMessages("7").first().single()
        assertTrue(shown.isDraft)
        assertTrue(shown.needsEdit)
        assertTrue(repo.sendDraft("91").isSuccess)
        server.takeRequest()
        val send = server.takeRequest()
        assertEquals("/api/messages/91/send", send.url.encodedPath)
        val body = send.body!!.utf8()
        assertTrue(body.contains(""""text":{"ja":"hello","en":"hi"}"""))
        assertTrue(body.contains(""""tasks":[{"title":{"ja":"t"}"""))
    }

    @Test
    fun anEditedDraftIsSentWithItsNewText() = runBlocking {
        ok(draft(null))
        ok(draft(null, textJa = "edited"))
        ok(draft("2026-11-23T08:00:05Z", textJa = "edited"))
        val repo = repo(foreman)
        repo.sendMessage("7", "hello", false, "1", "key-1")
        repo.editDraft("91", "edited")
        repo.sendDraft("91")
        repeat(2) { server.takeRequest() }
        assertTrue(server.takeRequest().body!!.utf8().contains(""""ja":"edited""""))
    }

    @Test
    fun aDraftWithoutWordsOrAnUnknownOneIsNotSent() = runBlocking {
        ok(draft(null, needsEdit = true, textJa = ""))
        val repo = repo(foreman)
        repo.sendMessage("7", "hello", false, "1", "key-1")
        assertEquals(AppError.Validation(listOf("text")), (repo.sendDraft("91") as AppResult.Failure).error)
        assertEquals(AppError.NotFound, (repo.sendDraft("92") as AppResult.Failure).error)
        assertEquals(1, server.requestCount)
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
        assertTrue(repo.reexplain("91", ReexplainRequest("12", "term", "", emptyMap(), emptyMap(), emptyList())).isSuccess)
        assertEquals("""{"userId":"12"}""", server.takeRequest().body?.utf8())
        assertEquals("""{"userId":"12","termJa":"term"}""", server.takeRequest().body?.utf8())
    }

    @Test
    fun editDraftSendsDraftEditBodyShape() = runBlocking {
        ok(draft(null, textJa = "edited ja"))
        val result = repo(foreman).editDraft("91", "edited ja") as AppResult.Success
        assertEquals("91", result.value.id)
        val req = server.takeRequest()
        assertEquals("PATCH", req.method)
        assertEquals("/api/messages/91", req.url.encodedPath)
        assertEquals("""{"textJa":"edited ja"}""", req.body?.utf8())
    }

    @Test
    fun editDraft409AlreadySentShowsTypedError() = runBlocking {
        server.enqueue(
            MockResponse.Builder().code(409)
                .body("""{"success":false,"error":{"code":"ALREADY_SENT","message":"already sent"}}""").build(),
        )
        val result = repo(foreman).editDraft("91", "edited text")
        assertTrue(result is AppResult.Failure)
        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Conflict)
        assertEquals("ALREADY_SENT", (error as AppError.Conflict).code)
    }

    @Test
    fun editDraftWithNeedsEditTruePreservesNeedsEditState() = runBlocking {
        ok(draft(null, needsEdit = true, textJa = "keep my text"))
        val result = repo(foreman).editDraft("91", "keep my text") as AppResult.Success
        assertTrue(result.value.needsEdit)
        assertEquals("keep my text", result.value.originalText)
    }

    @Test
    fun reexplainDraftReturnsDraftWithEasyJaAndExplanation() = runBlocking {
        val fu = """[{"id":"fu1","messageId":"91","userId":"12","displayName":"Rahim","termJa":"金型","status":"open","note":"","createdAt":"2026-11-23T08:00:00Z","resolvedAt":null}]"""
        ok("""{"items":$fu,"nextCursor":null}""")
        val draftResponse = """{"text":{"ja":"やさしい日本語","en":"easy english"},"question":{"id":"q1","termId":"金型","prompt":{"ja":"質問","en":"question"},"options":[{"ja":"A","en":"A"},{"ja":"B","en":"B"},{"ja":"C","en":"C"}],"correctIndex":1}}"""
        ok(draftResponse)
        val result = repo(foreman).getReexplainDraft("91", "12") as AppResult.Success
        assertEquals("やさしい日本語", result.value.easyJa)
        assertEquals("easy english", result.value.explanation["en"])
        assertEquals(1, result.value.newQuestionCorrectIndex)
        assertEquals("金型", result.value.termJa)
        val listReq = server.takeRequest()
        val draftReq = server.takeRequest()
        assertEquals("POST", draftReq.method)
        assertEquals("/api/messages/91/reexplain/draft", draftReq.url.encodedPath)
        assertEquals("""{"userId":"12","termJa":"金型"}""", draftReq.body?.utf8())
    }

    @Test
    fun reexplainSendsBothOrNeither() = runBlocking {
        val repo = repo(foreman)
        // 1. Both provided: sends both text and question with correctIndex
        ok(draft("2026-11-23T08:00:05Z"))
        val both = repo.reexplain(
            "91",
            ReexplainRequest(
                userId = "12",
                termJa = "金型",
                easyJa = "簡単な日本語",
                explanation = mapOf("en" to "simple"),
                newQuestionPrompt = mapOf("ja" to "Q"),
                newQuestionOptions = listOf(mapOf("ja" to "1"), mapOf("ja" to "2"), mapOf("ja" to "3")),
                newQuestionCorrectIndex = 2,
            ),
        )
        assertTrue(both.isSuccess)
        val bothReq = server.takeRequest()
        val bothBody = bothReq.body?.utf8().orEmpty()
        assertTrue(bothBody.contains(""""userId":"12""""))
        assertTrue(bothBody.contains(""""termJa":"金型""""))
        assertTrue(bothBody.contains(""""ja":"簡単な日本語""""))
        assertTrue(bothBody.contains(""""correctIndex":2"""))

        // 2. Neither provided: sends only userId and termJa
        ok(draft("2026-11-23T08:00:05Z"))
        val neither = repo.reexplain(
            "91",
            ReexplainRequest(
                userId = "12",
                termJa = "金型",
                easyJa = "",
                explanation = emptyMap(),
                newQuestionPrompt = emptyMap(),
                newQuestionOptions = emptyList(),
            ),
        )
        assertTrue(neither.isSuccess)
        val neitherReq = server.takeRequest()
        assertEquals("""{"userId":"12","termJa":"金型"}""", neitherReq.body?.utf8())

        // 3. Text provided without question -> fails validation
        val onlyText = repo.reexplain(
            "91",
            ReexplainRequest(
                userId = "12",
                termJa = "金型",
                easyJa = "簡単な日本語",
                explanation = emptyMap(),
                newQuestionPrompt = emptyMap(),
                newQuestionOptions = emptyList(),
            ),
        )
        assertTrue(onlyText is AppResult.Failure && (onlyText as AppResult.Failure).error is AppError.Validation)

        // 4. Question provided without text -> fails validation
        val onlyQuestion = repo.reexplain(
            "91",
            ReexplainRequest(
                userId = "12",
                termJa = "金型",
                easyJa = "",
                explanation = emptyMap(),
                newQuestionPrompt = mapOf("ja" to "Q"),
                newQuestionOptions = listOf(mapOf("ja" to "1"), mapOf("ja" to "2"), mapOf("ja" to "3")),
            ),
        )
        assertTrue(onlyQuestion is AppResult.Failure && (onlyQuestion as AppResult.Failure).error is AppError.Validation)
    }

    @Test
    fun askForemanSendsAskForemanInBodyAndSecondTapWhileInFlightDoesNothing() = runBlocking {
        val askMsg = """{"id":"99","threadId":"7","fromUserId":"12","kind":"ask","language":"ja","stage":5,""" +
            """"needsEdit":false,"transcriptJa":null,"text":{"ja":"金型交換"},"toUserId":null,"refMessageId":"91",""" +
            """"createdAt":"2026-11-23T08:00:00Z","sentAt":"2026-11-23T08:00:01Z","questions":[]}"""
        ok(askMsg, code = 201)
        val repo = repo(worker)
        val result = repo.askExplainAgain("91", "12", "金型交換")
        assertTrue(result.isSuccess)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/api/messages/91/ask", req.url.encodedPath)
        assertEquals("""{"termJa":"金型交換"}""", req.body?.utf8())
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
    fun aDirectThreadIsCalledByTheNameTheServerGives() = runBlocking {
        val rows = """[{"id":"7","titleJa":"Rahim","displayTitle":"Yamaguchi","kind":"direct"},{"id":"8","titleJa":"Line 2","displayTitle":"Line 2"}]"""
        ok(rows)
        ok(rows)
        assertEquals(listOf("Yamaguchi", "Line 2"), repo(worker).getThreads().first().map { it.titleJa })
        assertEquals(listOf("Yamaguchi", "Line 2"), repo(worker).getThreadSummaries().first().map { it.title["ja"] })
    }

    @Test
    fun aWorkersMessageWithoutJapaneseShowsTheWorkersOwnWordsAndName() = runBlocking {
        ok(
            """{"items":[{"id":"92","threadId":"7","fromUserId":"13","fromDisplayName":"Siti","fromRole":"worker","kind":"text",""" +
                """"language":"en","text":{"en":"It is hot"},"createdAt":"2026-11-23T08:00:00Z","sentAt":"2026-11-23T08:00:00Z"}]}""",
            etag = "e1",
        )
        val message = repo(foreman).getMessages("7").first().single()
        assertEquals("It is hot", message.originalText)
        assertEquals("Siti", message.fromUserName)
        assertFalse(message.isDraft)
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
