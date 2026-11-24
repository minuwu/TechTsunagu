package com.techtsunagu.wakatta.feature.update

import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ApkDownloaderTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val server = MockWebServer()
    private val bytes = ByteArray(50_000) { (it % 251).toByte() }
    private lateinit var downloader: OkHttpApkDownloader

    @Before
    fun setUp() {
        server.start()
        downloader = OkHttpApkDownloader(OkHttpClient(), File(tmp.root, "update"))
    }

    @After
    fun tearDown() = server.close()

    private fun sha(b: ByteArray) = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }

    private fun serve() = server.enqueue(MockResponse.Builder().body(Buffer().write(bytes)).build())

    @Test
    fun matchingChecksumReturnsTheFileAndReports100() = runTest {
        serve()
        var last = -1
        val result = downloader.download(server.url("/a.apk").toString(), sha(bytes).uppercase()) { last = it }
        assertTrue(result is DownloadResult.Done)
        assertEquals(bytes.size.toLong(), (result as DownloadResult.Done).file.length())
        assertEquals(100, last)
    }

    @Test
    fun wrongChecksumDeletesTheFileAndRefuses() = runTest {
        serve()
        val result = downloader.download(server.url("/a.apk").toString(), "0".repeat(64)) {}
        assertEquals(DownloadResult.ChecksumMismatch, result)
        assertFalse(File(tmp.root, "update/wakatta-update.apk").exists())
    }

    @Test
    fun blankChecksumIsRefused() = runTest {
        serve()
        assertEquals(DownloadResult.ChecksumMismatch, downloader.download(server.url("/a.apk").toString(), "") {})
        assertFalse(File(tmp.root, "update/wakatta-update.apk").exists())
    }

    @Test
    fun httpErrorFailsAndDeletesFile() = runTest {
        server.enqueue(MockResponse.Builder().code(404).build())
        assertEquals(DownloadResult.Failed, downloader.download(server.url("/a.apk").toString(), sha(bytes)) {})
        assertFalse(File(tmp.root, "update/wakatta-update.apk").exists())
    }

    @Test
    fun cancellationDeletesTheFile() = runTest {
        serve()
        val job = launch {
            downloader.download(server.url("/a.apk").toString(), sha(bytes)) {}
        }
        job.cancel()
        assertFalse(File(tmp.root, "update/wakatta-update.apk").exists())
    }
}
