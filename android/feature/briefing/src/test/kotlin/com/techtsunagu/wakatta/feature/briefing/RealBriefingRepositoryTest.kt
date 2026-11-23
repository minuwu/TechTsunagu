package com.techtsunagu.wakatta.feature.briefing

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.briefing.data.BriefingApi
import com.techtsunagu.wakatta.feature.briefing.data.RealBriefingRepository
import com.techtsunagu.wakatta.feature.briefing.data.splitFocusChange
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import com.techtsunagu.wakatta.feature.briefing.model.CheckQuestionWithAnswer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RealBriefingRepositoryTest {
    private class Store(user: User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("tok", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }

    private fun repo(user: User): RealBriefingRepository {
        val store = Store(user)
        val api = ApiClientFactory
            .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
            .create(BriefingApi::class.java)
        return RealBriefingRepository({ api }, store)
    }

    @After
    fun tearDown() = server.close()

    private fun ok(data: String) = server.enqueue(
        MockResponse.Builder().code(200).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    private val q = """{"id":"q1","termId":null,"prompt":{"en":"When?"},"options":[{"en":"A"},{"en":"B"},{"en":"C"}],"correctIndex":2}"""

    private fun briefing(id: String, status: String = "draft") = """{"id":"$id","text":{"en":"Check the cover. Die change is at noon."},
        "status":"$status","createdAt":"2026-11-24T00:00:00Z","publishedAt":null,"transcriptJa":"t","question":$q,"needsEdit":true}"""

    @Test
    fun draftSendsTextWithTheSameKeyOnRetry() = runBlocking {
        repeat(2) { ok(briefing("12")) }
        val foreman = repo(DemoSeed.demoForeman)
        repeat(2) { assertTrue(foreman.createDraft(" hello ", null, "key-1") is AppResult.Success) }
        val first = server.takeRequest()
        val retry = server.takeRequest()
        assertTrue(first.body!!.utf8().contains("name=\"text\""))
        assertEquals("key-1", first.headers["Idempotency-Key"])
        assertEquals("key-1", retry.headers["Idempotency-Key"])
    }

    @Test
    fun publishSendsOnlySchemaFieldsWithAnExplicitNullTermId() = runBlocking {
        ok(briefing("12", "published"))
        val question = CheckQuestionWithAnswer("q1", mapOf("en" to "When?"), listOf(mapOf("en" to "A")), 2)
        val out = repo(DemoSeed.demoForeman)
            .publishBriefing(BriefingPublishRequest("12", mapOf("en" to "x"), emptyMap(), emptyMap(), question))
        assertTrue(out is AppResult.Success)
        val sent = server.takeRequest()
        val body = sent.body!!.utf8()
        assertTrue(body.contains("\"termId\":null"))
        assertFalse(body.contains("focusText") || body.contains("changeText"))
        assertEquals("publish-12", sent.headers["Idempotency-Key"])
    }

    @Test
    fun workerBriefingHasNoCorrectIndexAndMapsPassed() = runBlocking {
        ok(
            """{"items":[{"id":"12","text":{"en":"Hi"},"status":"published","createdAt":"2026-11-24T00:00:00Z",
            "publishedAt":null,"question":{"id":"q1","termId":null,"prompt":{"en":"?"},"options":[{"en":"A"},{"en":"B"},{"en":"C"}]},
            "opened":true,"passed":false}],"nextCursor":null}""",
        )
        val b = (repo(DemoSeed.demoWorker).getBriefingForWorker("12") as AppResult.Success).value
        assertTrue(b.opened)
        assertEquals(false, b.passed)
        assertEquals(3, b.question.options.size)
    }

    @Test
    fun receiptsBecomeThreeStatusGroups() = runBlocking {
        ok("""{"understood":[{"id":"1","name":"A"}],"openedOnly":[{"id":"2","name":"B"}],"notOpened":[{"id":"3","name":"C"}]}""")
        val list = (repo(DemoSeed.demoForeman).getWorkerStatuses("12") as AppResult.Success).value
        assertEquals(listOf(true, true, false), list.map { it.hasAttended })
    }

    @Test
    fun focusIsTheFirstSentenceAndChangeTheRest() {
        val (focus, change) = splitFocusChange(mapOf("en" to "One. Two. Three.", "ja" to "ひとつだけ"))
        assertEquals("One.", focus["en"])
        assertEquals("Two. Three.", change["en"])
        assertEquals("ひとつだけ", change["ja"])
    }
}
