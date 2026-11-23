package com.techtsunagu.wakatta.feature.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.core.testing.MockWebServerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class RealDeviceRegistrarTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var server: MockWebServer
    private lateinit var deviceApi: DeviceApi
    private val sessionFlow = MutableStateFlow<Session?>(null)
    private val sessionStore = object : SessionStore {
        override val session: StateFlow<Session?> = sessionFlow
        override suspend fun save(session: Session) { sessionFlow.value = session }
        override suspend fun clear() { sessionFlow.value = null }
        override fun getCachedToken(): String? = sessionFlow.value?.token
    }
    private val config = AppConfig(
        useMock = false,
        apiBaseUrl = "",
        updateManifestUrl = "",
        versionName = "1.0.0",
        versionCode = 1,
        isDebug = true,
    )

    @Before
    fun setUp() {
        context.getSharedPreferences("wakatta_notifications_device_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        server = MockWebServer()
        server.start()
        val okHttpClient = ApiClientFactory.okHttp(
            versionName = config.versionName,
            language = { "en" },
            sessionStore = sessionStore,
            debug = true,
        )
        val retrofit = ApiClientFactory.retrofit(server.url("/").toString().trimEnd('/'), okHttpClient)
        deviceApi = retrofit.create(DeviceApi::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun registerDeviceSendsPutRequestWithCorrectBody() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = """{"registered":true,"lastSeenAt":"2026-11-23T10:00:00Z"}""",
            ),
        )

        val registrar = RealDeviceRegistrar(
            context = context,
            api = { deviceApi },
            config = config,
            sessionStore = sessionStore,
        )

        registrar.register(notificationsEnabled = true, fcmToken = "test-token-123")

        val recorded = server.takeRequest()
        assertEquals("PUT", recorded.method)
        assertEquals("/api/devices/me", recorded.url.encodedPath)
        val body = recorded.body?.utf8() ?: ""
        assertTrue(body.contains("\"platform\":\"android\""))
        assertTrue(body.contains("\"fcmToken\":\"test-token-123\""))
        assertTrue(body.contains("\"appVersion\":\"1.0.0\""))
        assertTrue(body.contains("\"notificationsEnabled\":true"))
        assertNotNull(registrar.getCachedToken())
        assertEquals("test-token-123", registrar.getCachedToken())
    }

    @Test
    fun unregisterDeviceSendsDeleteRequestWithInstallId() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = "null",
            ),
        )

        val registrar = RealDeviceRegistrar(
            context = context,
            api = { deviceApi },
            config = config,
            sessionStore = sessionStore,
        )

        registrar.unregister()

        val recorded = server.takeRequest()
        assertEquals("DELETE", recorded.method)
        assertEquals("/api/devices/me", recorded.url.encodedPath)
        val installId = registrar.getOrCreateInstallId()
        assertEquals(installId, recorded.url.queryParameter("installId"))
    }

    @Test
    fun sessionTransitionsTriggerRegisterAndUnregister() = runTest {
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = """{"registered":true,"lastSeenAt":"2026-11-23T10:00:00Z"}""",
            ),
        )
        server.enqueue(
            MockWebServerHelper.envelopeResponse(
                dataJson = "null",
            ),
        )

        val registrar = RealDeviceRegistrar(
            context = context,
            api = { deviceApi },
            config = config,
            sessionStore = sessionStore,
        )
        registrar.saveToken("cached-token")

        // Trigger sign-in
        sessionFlow.value = Session(
            token = "t1",
            user = User("u1", "Rahim", Role.WORKER, "bn", "s1", "d1"),
        )

        val putRequest = server.takeRequest()
        assertEquals("PUT", putRequest.method)

        // Trigger sign-out
        sessionFlow.value = null

        val deleteRequest = server.takeRequest()
        assertEquals("DELETE", deleteRequest.method)
    }
}
