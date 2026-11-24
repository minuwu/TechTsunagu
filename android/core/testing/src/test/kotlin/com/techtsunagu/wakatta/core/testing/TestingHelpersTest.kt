package com.techtsunagu.wakatta.core.testing

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TestingHelpersTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun jsonFixture_loadsExistingFixture() {
        val content = JsonFixture.load("sample_envelope")
        assertNotNull(content)
        assertTrue(content.contains("\"success\": true"))
        assertTrue(content.contains("\"message\": \"ok\""))
    }

    @Test
    fun mockWebServerHelper_enqueuesAndServesEnvelope() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueueEnvelope(
                dataJson = """{"id":"test1"}""",
                success = true,
                message = null,
                code = 200
            )

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(server.url("/api/test"))
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
                val body = response.body?.string().orEmpty()
                assertTrue(body.contains("\"success\":true"))
                assertTrue(body.contains("\"id\":\"test1\""))
            }
        } finally {
            server.close()
        }
    }
}
