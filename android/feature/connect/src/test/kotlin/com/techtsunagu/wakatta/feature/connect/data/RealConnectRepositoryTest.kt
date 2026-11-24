package com.techtsunagu.wakatta.feature.connect.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.connect.MemoryCacheStore
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealConnectRepositoryTest {

    private class TestStore(user: User) : SessionStore {
        private val s = MutableStateFlow<Session?>(Session("test_token", user = user))
        override val session: StateFlow<Session?> = s
        override suspend fun save(session: Session) { s.value = session }
        override suspend fun clear() { s.value = null }
        override fun getCachedToken(): String? = s.value?.token
    }

    private val server = MockWebServer().also { it.start() }
    private val store = TestStore(DemoSeed.demoWorker)
    private val api: ConnectApi = ApiClientFactory
        .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, store, debug = false))
        .create(ConnectApi::class.java)

    private val tempDir = File(System.getProperty("java.io.tmpdir") ?: ".", "connect_test_${System.currentTimeMillis()}").also { it.mkdirs() }
    private val mockContext = object : ContextWrapper(tempDir) {}

    private val cache = MemoryCacheStore()
    private val repo = RealConnectRepository({ api }, store, mockContext.toContext(), cache)

    @After
    fun tearDown() {
        server.close()
        tempDir.deleteRecursively()
    }

    private fun ok(data: String, code: Int = 200) = server.enqueue(
        MockResponse.Builder().code(code).body("""{"success":true,"data":$data,"message":"ok"}""").build(),
    )

    @Test
    fun getMyQrReturnsAndPersistsToken() = runBlocking {
        ok("""{"token":"token_abc_123","createdAt":"2026-11-24T10:00:00Z"}""")

        val res = repo.getMyQr()
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("token_abc_123", dto.token)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.url.encodedPath.contains("me/qr"))
    }

    @Test
    fun rotateMyQrRequestsNewToken() = runBlocking {
        ok("""{"token":"token_rotated_456","createdAt":"2026-11-24T10:00:00Z"}""")

        val res = repo.rotateMyQr()
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("token_rotated_456", dto.token)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.body?.utf8().orEmpty().contains("\"rotate\":true"))
    }

    @Test
    fun resolveReturnsContactPreview() = runBlocking {
        ok("""{"displayName":"Tanabe Mai","language":"ja"}""")

        val res = repo.resolve("token_abc_123")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("token_abc_123", dto.token)
        assertEquals("Tanabe Mai", dto.name)
        assertEquals("ja", dto.language)

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.url.encodedPath.contains("contacts/resolve"))
        assertEquals("token_abc_123", req.url.queryParameter("token"))
    }

    @Test
    fun addContactSendsIdempotencyKeyAndPrefixesId() = runBlocking {
        ok("""{"id":"42","displayName":"Sato Misaki","language":"ja","muted":false,"hidden":false,"lastMessageAt":null,"unread":0,"createdAt":"2026-11-24T10:00:00Z"}""", code = 201)

        val res = repo.addContact("valid_token_xyz")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("tc_42", dto.id)
        assertEquals("Sato Misaki", dto.name)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertNotNull(req.headers["Idempotency-Key"])
        assertTrue(req.body?.utf8().orEmpty().contains("\"token\":\"valid_token_xyz\""))
    }

    @Test
    fun getContactsFiltersHiddenAndAddsPrefix() = runBlocking {
        ok("""{"items":[{"id":"1","displayName":"User One","language":"ja","muted":false,"hidden":false,"lastMessageAt":"Hello","unread":1,"createdAt":"2026-11-24T10:00:00Z"},{"id":"2","displayName":"User Two","language":"en","muted":false,"hidden":true,"lastMessageAt":null,"unread":0,"createdAt":"2026-11-24T10:00:00Z"}],"nextCursor":null}""")

        val res = repo.getContacts()
        assertTrue(res is AppResult.Success)
        val list = (res as AppResult.Success).value
        assertEquals(1, list.size)
        assertEquals("tc_1", list[0].id)
        assertEquals("User One", list[0].name)
    }

    @Test
    fun getMessagesUsesConditionalGetAndHandles304() = runBlocking {
        // First call: 200 with ETag
        server.enqueue(
            MockResponse.Builder()
                .code(200)
                .setHeader("ETag", "\"etag_v1\"")
                .body("""{"success":true,"data":{"items":[{"id":"m1","fromMe":true,"textSrc":"こんにちは","srcLang":"ja","text":"Hello","createdAt":"2026-11-24T10:00:00Z"}],"nextCursor":null},"message":"ok"}""")
                .build(),
        )

        val res1 = repo.getMessages("tc_42")
        assertTrue(res1 is AppResult.Success)
        val list1 = (res1 as AppResult.Success).value
        assertEquals(1, list1.size)
        assertEquals("m1", list1[0].id)
        assertEquals("こんにちは", list1[0].ja)
        assertEquals("Hello", list1[0].en)

        val req1 = server.takeRequest()
        assertEquals("GET", req1.method)
        assertTrue(req1.url.encodedPath.contains("contacts/42/messages"))

        // Second call: 304 Not Modified
        server.enqueue(
            MockResponse.Builder()
                .code(304)
                .setHeader("ETag", "\"etag_v1\"")
                .build(),
        )

        val res2 = repo.getMessages("tc_42")
        assertTrue(res2 is AppResult.Success)
        val list2 = (res2 as AppResult.Success).value
        assertEquals(1, list2.size)
        assertEquals("m1", list2[0].id)

        val req2 = server.takeRequest()
        assertEquals("\"etag_v1\"", req2.headers["If-None-Match"])
    }

    @Test
    fun sendMessageSendsIdempotencyKey() = runBlocking {
        ok("""{"id":"m2","fromMe":true,"textSrc":"ありがとう","srcLang":"ja","text":"Thank you","createdAt":"2026-11-24T10:05:00Z"}""", code = 201)

        val res = repo.sendMessage("tc_42", "ありがとう", "ja")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("m2", dto.id)
        assertEquals("ありがとう", dto.ja)
        assertEquals("Thank you", dto.en)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.url.encodedPath.contains("contacts/42/messages"))
        assertNotNull(req.headers["Idempotency-Key"])
    }

    @Test
    fun deletingAMessageAndTheWholeChatCallTheirRoutes() = runBlocking { // ADR 0014
        ok("null")
        ok("null")

        assertTrue(repo.deleteMessage("tc_42", "m2") is AppResult.Success)
        assertTrue(repo.clearMessages("tc_42") is AppResult.Success)

        val one = server.takeRequest()
        assertEquals("DELETE", one.method)
        assertEquals("/api/contacts/42/messages/m2", one.url.encodedPath)
        val all = server.takeRequest()
        assertEquals("DELETE", all.method)
        assertEquals("/api/contacts/42/messages", all.url.encodedPath)
    }

    @Test
    fun blockCallsBackendWithoutPrefix() = runBlocking {
        ok("null")

        val res = repo.block("tc_42")
        assertTrue(res is AppResult.Success)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.url.encodedPath.contains("blocks"))
        assertTrue(req.body?.utf8().orEmpty().contains("\"contactId\":\"42\""))
    }

    // ----- Talk Threads tests -----

    @Test
    fun getTalksReturnsListAndPrefixesIds() = runBlocking {
        ok("""{"items":[{"id":"101","title":"Yamada-san","updatedAt":"2026-11-24T12:00:00Z","entryCount":3}],"nextCursor":null}""")

        val res = repo.getTalks()
        assertTrue(res is AppResult.Success)
        val list = (res as AppResult.Success).value
        assertEquals(1, list.size)
        assertEquals("tt_101", list[0].id)
        assertEquals("Yamada-san", list[0].title)
        assertEquals(3, list[0].entryCount)

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads"))
    }

    @Test
    fun createTalkSendsIdempotencyKeyAndValidates() = runBlocking {
        ok("""{"id":"102","title":"Sato-san","updatedAt":"2026-11-24T12:00:00Z","entryCount":0}""", code = 201)

        val res = repo.createTalk("Sato-san")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("tt_102", dto.id)
        assertEquals("Sato-san", dto.title)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads"))
        assertNotNull(req.headers["Idempotency-Key"])
        assertTrue(req.body?.utf8().orEmpty().contains("\"title\":\"Sato-san\""))
    }

    @Test
    fun renameTalkSendsPatch() = runBlocking {
        ok("""{"id":"102","title":"Sato-san (Senior)","updatedAt":"2026-11-24T12:00:00Z","entryCount":0}""")

        val res = repo.renameTalk("tt_102", "Sato-san (Senior)")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("tt_102", dto.id)
        assertEquals("Sato-san (Senior)", dto.title)

        val req = server.takeRequest()
        assertEquals("PATCH", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads/102"))
        assertTrue(req.body?.utf8().orEmpty().contains("\"title\":\"Sato-san (Senior)\""))
    }

    @Test
    fun deleteTalkSendsDelete() = runBlocking {
        ok("null")

        val res = repo.deleteTalk("tt_102")
        assertTrue(res is AppResult.Success)

        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads/102"))
    }

    @Test
    fun getEntriesReturnsMappedTalkEntries() = runBlocking {
        ok("""{"items":[{"id":"e1","transcript":"膝が痛い","plainJa":"膝が痛い","meaning":"My knee hurts","terms":[{"dialect":"ひざ","plain":"膝","meaning":"knee"}],"needsStaff":true,"needsEdit":false,"ownText":null,"grade":null,"replyOptions":[{"ja":"大丈夫ですか","meaning":"Are you okay?"}],"replyJa":null,"confirmed":null,"createdAt":"2026-11-24T12:00:00Z"}],"nextCursor":null}""")

        val res = repo.getEntries("tt_101")
        assertTrue(res is AppResult.Success)
        val list = (res as AppResult.Success).value
        assertEquals(1, list.size)
        val e = list[0]
        assertEquals("e1", e.id)
        assertEquals("膝が痛い", e.said)
        assertEquals("膝が痛い", e.plainJa)
        assertTrue(e.needsStaff)
        assertEquals(1, e.terms.size)
        assertEquals(1, e.replyOptions.size)
        assertEquals("大丈夫ですか", e.replyOptions[0].ja)
        assertEquals("Are you okay?", e.replyOptions[0].meaning)

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads/101/entries"))
    }

    @Test
    fun addEntrySendsMultipartAndIdempotencyKey() = runBlocking {
        ok("""{"id":"e2","transcript":"散歩休みます","plainJa":"散歩を休みます","meaning":"I will skip the walk","terms":[],"needsStaff":false,"needsEdit":false,"ownText":null,"grade":null,"replyOptions":[],"replyJa":null,"confirmed":null,"createdAt":"2026-11-24T12:05:00Z"}""", code = 201)

        val res = repo.addEntry("tt_101", "散歩休みます")
        assertTrue(res is AppResult.Success)
        val dto = (res as AppResult.Success).value
        assertEquals("e2", dto.id)
        assertEquals("散歩休みます", dto.said)

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads/101/entries"))
        assertNotNull(req.headers["Idempotency-Key"])
        val ct = req.headers["Content-Type"].orEmpty()
        assertTrue(ct.contains("multipart/form-data"))
        assertTrue(req.body?.utf8().orEmpty().contains("散歩休みます"))
    }

    @Test
    fun updateEntrySendsPatchWithExactField() = runBlocking {
        // 1. Understand sends ownText
        ok("""{"id":"e1","transcript":"膝が痛い","plainJa":"膝が痛い","meaning":"knee hurts","terms":[],"needsStaff":false,"needsEdit":false,"ownText":"knee hurts","grade":"green","replyOptions":[],"replyJa":null,"confirmed":null,"createdAt":"2026-11-24T12:00:00Z"}""")

        val res1 = repo.updateEntry("tt_101", "e1", com.techtsunagu.wakatta.feature.connect.domain.EntryAction.Understand("knee hurts"))
        assertTrue(res1 is AppResult.Success)
        val dto1 = (res1 as AppResult.Success).value
        assertEquals(com.techtsunagu.wakatta.core.model.TalkGrade.GREEN, dto1.grade)

        val req1 = server.takeRequest()
        assertEquals("PATCH", req1.method)
        assertTrue(req1.url.encodedPath.contains("talk-threads/101/entries/e1"))
        val b1 = req1.body?.utf8().orEmpty()
        assertTrue(b1.contains("\"ownText\":\"knee hurts\""))
        assertTrue(!b1.contains("replyJa"))
        assertTrue(!b1.contains("confirmed"))

        // 2. Reply sends replyJa
        ok("""{"id":"e1","transcript":"膝が痛い","plainJa":"膝が痛い","meaning":"knee hurts","terms":[],"needsStaff":false,"needsEdit":false,"ownText":"knee hurts","grade":"green","replyOptions":[],"replyJa":"大丈夫ですか","confirmed":null,"createdAt":"2026-11-24T12:00:00Z"}""")

        val res2 = repo.updateEntry("tt_101", "e1", com.techtsunagu.wakatta.feature.connect.domain.EntryAction.Reply(0, "大丈夫ですか"))
        assertTrue(res2 is AppResult.Success)
        val dto2 = (res2 as AppResult.Success).value
        assertEquals("大丈夫ですか", dto2.replyJa)

        val req2 = server.takeRequest()
        val b2 = req2.body?.utf8().orEmpty()
        assertTrue(b2.contains("\"replyJa\":\"大丈夫ですか\""))
        assertTrue(!b2.contains("ownText"))
        assertTrue(!b2.contains("confirmed"))

        // 3. Confirm sends confirmed
        ok("""{"id":"e1","transcript":"膝が痛い","plainJa":"膝が痛い","meaning":"knee hurts","terms":[],"needsStaff":false,"needsEdit":false,"ownText":"knee hurts","grade":"green","replyOptions":[],"replyJa":"大丈夫ですか","confirmed":true,"createdAt":"2026-11-24T12:00:00Z"}""")

        val res3 = repo.updateEntry("tt_101", "e1", com.techtsunagu.wakatta.feature.connect.domain.EntryAction.Confirm(true))
        assertTrue(res3 is AppResult.Success)
        val dto3 = (res3 as AppResult.Success).value
        assertEquals(true, dto3.confirmed)

        val req3 = server.takeRequest()
        val b3 = req3.body?.utf8().orEmpty()
        assertTrue(b3.contains("\"confirmed\":true"))
        assertTrue(!b3.contains("ownText"))
        assertTrue(!b3.contains("replyJa"))
    }

    @Test
    fun deleteEntrySendsDelete() = runBlocking {
        ok("null")

        val res = repo.deleteEntry("tt_101", "e1")
        assertTrue(res is AppResult.Success)

        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertTrue(req.url.encodedPath.contains("talk-threads/101/entries/e1"))
    }

    @Test
    fun rateLimitedReturnsTypedError() = runBlocking {
        server.enqueue(
            MockResponse.Builder()
                .code(429)
                .setHeader("Retry-After", "30")
                .body("""{"error":{"code":"RATE_LIMITED","message":"Too many requests","details":{}}}""")
                .build(),
        )

        val res = repo.getTalks()
        assertTrue(res is AppResult.Failure)
        val err = (res as AppResult.Failure).error
        assertTrue(err is AppError.RateLimited)
        assertEquals(30L, (err as AppError.RateLimited).retryAfter)
    }
}

private open class ContextWrapper(val dir: File) {
    fun toContext(): Context = object : android.content.ContextWrapper(null) {
        override fun getCacheDir(): File = dir
        override fun getSharedPreferences(name: String, mode: Int): android.content.SharedPreferences {
            return TestSharedPreferences()
        }
    }
}

private class TestSharedPreferences : android.content.SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map
    override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
    override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): android.content.SharedPreferences.Editor = EditorImpl(map)
    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private class EditorImpl(val map: MutableMap<String, Any?>) : android.content.SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): android.content.SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): android.content.SharedPreferences.Editor = this
        override fun putInt(key: String?, value: Int): android.content.SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putLong(key: String?, value: Long): android.content.SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putFloat(key: String?, value: Float): android.content.SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun putBoolean(key: String?, value: Boolean): android.content.SharedPreferences.Editor { map[key ?: ""] = value; return this }
        override fun remove(key: String?): android.content.SharedPreferences.Editor { map.remove(key); return this }
        override fun clear(): android.content.SharedPreferences.Editor { map.clear(); return this }
        override fun commit(): Boolean = true
        override fun apply() {}
    }
}
