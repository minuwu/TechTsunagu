package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.CacheEntry
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient

internal class TestSessionStore(initial: Session? = null) : SessionStore {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<Session?> = state
    override suspend fun save(session: Session) {
        state.value = session
    }
    override suspend fun clear() {
        state.value = null
    }
    override fun getCachedToken(): String? = state.value?.token
}

internal class TestCacheStore : CacheStore {
    val entries = mutableMapOf<String, CacheEntry>()
    override suspend fun get(key: String): CacheEntry? = entries[key]
    override suspend fun put(key: String, json: String, etag: String?) {
        entries[key] = CacheEntry(key, json, etag)
    }
    override suspend fun evict(prefix: String) {
        entries.keys.removeAll { it.startsWith(prefix) }
    }
}

internal fun testSession(token: String = "tok-1") = Session(
    token = token,
    user = User(id = "1", displayName = "Test", role = Role.WORKER, language = "en"),
)

/** A real client stack pointed at a MockWebServer. */
internal class Harness(
    session: Session? = testSession(),
    clientTweak: (OkHttpClient.Builder) -> Unit = {},
) : AutoCloseable {
    val server = MockWebServer().also { it.start() }
    val store = TestSessionStore(session)
    val client: OkHttpClient = ApiClientFactory
        .okHttp("1.2.3", { "ja" }, store, debug = false)
        .newBuilder().also(clientTweak).build()
    val retrofit = ApiClientFactory.retrofit(server.url("/").toString().trimEnd('/'), client)
    val api: AuthApi = retrofit.create(AuthApi::class.java)

    fun enqueue(code: Int, body: String, vararg headers: Pair<String, String>) {
        val builder = MockResponse.Builder().code(code).body(body)
        headers.forEach { (k, v) -> builder.addHeader(k, v) }
        server.enqueue(builder.build())
    }

    fun enqueueOk(dataJson: String) = enqueue(200, """{"success":true,"data":$dataJson,"message":"ok"}""")

    fun enqueueError(code: Int, errorCode: String, detailsJson: String = "{}", vararg headers: Pair<String, String>) =
        enqueue(
            code,
            """{"success":false,"error":{"code":"$errorCode","message":"m","details":$detailsJson}}""",
            *headers,
        )

    override fun close() {
        server.close()
    }
}

/** Walks up from the working directory to the first folder that holds [relative]. */
internal fun findUp(relative: String): File {
    var dir: File? = File(System.getProperty("user.dir")).absoluteFile
    while (dir != null) {
        val candidate = File(dir, relative)
        if (candidate.exists()) return candidate
        dir = dir.parentFile
    }
    error("$relative not found above ${System.getProperty("user.dir")}")
}
