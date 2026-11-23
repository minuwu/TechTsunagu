package com.techtsunagu.wakatta.feature.update

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.VersionGateState
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppConfigRepositoryTest {
    private val server = MockWebServer()
    private val client = OkHttpClient()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.close()

    private fun reply(code: Int, body: String = "") = MockResponse.Builder().code(code).body(body).build()

    private fun manifest(current: Long) =
        StaticManifestAppConfigRepository(client, server.url("/release/latest.json").toString(), current)

    private fun api(version: String = "1.2.0"): ApiAppConfigRepository {
        val retrofit = ApiClientFactory.retrofit(server.url("/").toString().trimEnd('/'), client)
        val api = retrofit.create(AppConfigApi::class.java)
        return ApiAppConfigRepository(api, version)
    }

    private val latestJson =
        """{"versionName":"1.3.0","versionCode":3,"apkFile":"wakatta-1.3.0.apk","sha256":"ab","minSupportedVersionCode":2}"""

    @Test
    fun manifestResolvesApkNextToTheManifestAndGatesByCode() = runTest {
        server.enqueue(reply(200, latestJson))
        val offer = (manifest(current = 1).fetch() as AppResult.Success).value
        assertEquals(VersionGateResult.Hard, offer.level)
        assertEquals(server.url("/release/wakatta-1.3.0.apk").toString(), offer.downloadUrl)
        assertEquals("ab", offer.sha256)
        server.enqueue(reply(200, latestJson))
        assertEquals(VersionGateResult.Soft, (manifest(2).fetch() as AppResult.Success).value.level)
    }

    @Test
    fun status426BecomesUpgradeRequired() = runTest {
        server.enqueue(reply(426))
        assertEquals(AppResult.Failure(AppError.UpgradeRequired), manifest(1).fetch())

        server.enqueue(reply(426))
        assertEquals(AppResult.Failure(AppError.UpgradeRequired), api().fetch())
    }

    @Test
    fun badManifestAndServerErrorAreFailures() = runTest {
        server.enqueue(reply(200, "{}"))
        assertTrue(manifest(1).fetch() is AppResult.Failure)
        server.enqueue(reply(503))
        assertEquals(AppResult.Failure(AppError.Server), manifest(1).fetch())
        assertEquals(AppResult.Failure(AppError.NotFound), StaticManifestAppConfigRepository(client, "", 1).fetch())
    }

    @Test
    fun appConfigIsGatedByVersionNameAndSendsRequest() = runTest {
        server.enqueue(
            reply(
                200,
                """{"success":true,"data":{"minVersion":"1.2.0","latestVersion":"1.10.0","downloadUrl":null,"sha256":null,"serverTime":"2026-11-23T14:00:00Z"},"message":"ok"}""",
            ),
        )
        val offer = (api("1.9.0-debug").fetch() as AppResult.Success).value
        assertEquals(VersionGateResult.Soft, offer.level)
        assertNull(offer.downloadUrl)
        assertNull(offer.sha256)
        val request = server.takeRequest()
        assertEquals("/api/app/config", request.url.encodedPath)
    }

    @Test
    fun fallbackUsesTheManifestOnlyWhenTheBackendFails() = runTest {
        server.enqueue(reply(500))
        server.enqueue(reply(200, latestJson))
        val repo = FallbackAppConfigRepository(api(), manifest(3))
        val offer = (repo.fetch() as AppResult.Success).value
        assertEquals("1.3.0", offer.latestVersionName)
        // 426 from the backend is final: the manifest is not asked.
        server.enqueue(reply(426))
        assertEquals(AppResult.Failure(AppError.UpgradeRequired), repo.fetch())
        assertEquals(3, server.requestCount)
    }

    @Test
    fun a426SetsTheGateToHard() = runTest {
        server.enqueue(reply(200, latestJson))
        val gate = UpdateVersionGate(manifest(2))
        gate.refresh()
        assertTrue(gate.state.value is VersionGateState.Soft)

        server.enqueue(reply(426))
        gate.refresh()
        assertTrue(gate.state.value is VersionGateState.Hard)
    }

    @Test
    fun requireSetsHard() {
        val gate = UpdateVersionGate(FakeAppConfigRepository())
        assertEquals(VersionGateState.UpToDate, gate.state.value)
        gate.require()
        assertTrue(gate.state.value is VersionGateState.Hard)
    }
}
