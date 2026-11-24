package com.techtsunagu.wakatta.feature.notifications

import com.techtsunagu.wakatta.core.common.LanguageManagerImpl
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.core.testing.MockWebServerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class RealSenderNamesTest {
    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit
    private val sessionFlow = MutableStateFlow<Session?>(null)
    private val sessionStore = object : SessionStore {
        override val session: StateFlow<Session?> = sessionFlow
        override suspend fun save(session: Session) { sessionFlow.value = session }
        override suspend fun clear() { sessionFlow.value = null }
        override fun getCachedToken(): String? = "test_token"
    }
    private val workerRepo = object : WorkerRepository {
        override suspend fun getWorkers(): AppResult<List<WorkerInfo>> =
            AppResult.Success(listOf(WorkerInfo("w10", "Ken", "ja")))
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val okHttpClient = ApiClientFactory.okHttp("1.0.0", { "en" }, sessionStore, true)
        retrofit = ApiClientFactory.retrofit(server.url("/").toString().trimEnd('/'), okHttpClient)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun senderFoundInContactsApiReturnsDisplayName() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = """{"items":[{"id":"c1","displayName":"Taro"}]}""",
            ),
        )
        val senders = RealSenderNames(dagger.Lazy { retrofit }, dagger.Lazy { workerRepo }, LanguageManagerImpl("en"))
        val name = senders.nameOf("c1")
        assertEquals("Taro", name)
    }

    @Test
    fun senderFoundInWorkerRepoReturnsDisplayName() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = """{"items":[]}""",
            ),
        )
        val senders = RealSenderNames(dagger.Lazy { retrofit }, dagger.Lazy { workerRepo }, LanguageManagerImpl("en"))
        val name = senders.nameOf("w10")
        assertEquals("Ken", name)
    }

    @Test
    fun unknownSenderReturnsGenericLocalizedTitle() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = """{"items":[]}""",
            ),
        )
        val sendersEn = RealSenderNames(dagger.Lazy { retrofit }, dagger.Lazy { workerRepo }, LanguageManagerImpl("en"))
        assertEquals("New message", sendersEn.nameOf("unknown_id"))

        val sendersJa = RealSenderNames(dagger.Lazy { retrofit }, dagger.Lazy { workerRepo }, LanguageManagerImpl("ja"))
        assertEquals("新しいメッセージ", sendersJa.nameOf("unknown_id"))
    }
}
