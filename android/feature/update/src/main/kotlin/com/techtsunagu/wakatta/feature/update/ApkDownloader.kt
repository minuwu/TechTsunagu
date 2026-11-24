package com.techtsunagu.wakatta.feature.update

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Request

sealed interface DownloadResult {
    data class Done(val file: File) : DownloadResult
    data object ChecksumMismatch : DownloadResult
    data object Failed : DownloadResult
}

interface ApkDownloader {
    /** Downloads [url] and returns the file only if its SHA-256 equals [expectedSha256]. */
    suspend fun download(url: String, expectedSha256: String, onProgress: (Int) -> Unit): DownloadResult
}

internal class OkHttpApkDownloader(
    private val client: Call.Factory,
    private val dir: File,
) : ApkDownloader {
    override suspend fun download(url: String, expectedSha256: String, onProgress: (Int) -> Unit): DownloadResult =
        withContext(Dispatchers.IO) {
            val dest = File(dir, "wakatta-update.apk")
            try {
                dir.mkdirs()
                val request = try {
                    Request.Builder().url(url).build()
                } catch (_: IllegalArgumentException) {
                    dest.delete()
                    return@withContext DownloadResult.Failed
                }
                val actual = client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        dest.delete()
                        return@use null
                    }
                    val body = response.body
                    copyAndHash(body.byteStream(), body.contentLength(), dest, onProgress)
                }
                when {
                    actual == null -> {
                        dest.delete()
                        DownloadResult.Failed
                    }
                    expectedSha256.isNotBlank() && actual.equals(expectedSha256.trim(), ignoreCase = true) ->
                        DownloadResult.Done(dest)
                    else -> {
                        dest.delete()
                        DownloadResult.ChecksumMismatch
                    }
                }
            } catch (e: CancellationException) {
                dest.delete()
                throw e
            } catch (_: IOException) {
                dest.delete()
                DownloadResult.Failed
            }
        }

    private suspend fun copyAndHash(input: InputStream, total: Long, dest: File, onProgress: (Int) -> Unit): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(16 * 1024)
        var done = 0L
        var lastPercent = -1
        input.use { src ->
            dest.outputStream().use { out ->
                while (true) {
                    coroutineContext.ensureActive()
                    val n = src.read(buffer)
                    if (n < 0) break
                    out.write(buffer, 0, n)
                    digest.update(buffer, 0, n)
                    done += n
                    val percent = if (total > 0) (done * 100 / total).toInt().coerceIn(0, 100) else 0
                    if (percent != lastPercent) {
                        lastPercent = percent
                        onProgress(percent)
                    }
                }
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

/** Debug only: counts to 100 without touching the network. */
internal class FakeApkDownloader : ApkDownloader {
    override suspend fun download(url: String, expectedSha256: String, onProgress: (Int) -> Unit): DownloadResult {
        for (p in 0..100 step 20) {
            onProgress(p)
            delay(150)
        }
        return DownloadResult.Done(File("fake.apk"))
    }
}
