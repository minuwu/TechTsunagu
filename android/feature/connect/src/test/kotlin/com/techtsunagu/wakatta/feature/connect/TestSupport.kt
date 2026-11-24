package com.techtsunagu.wakatta.feature.connect

import com.techtsunagu.wakatta.core.media.DictationEvent
import com.techtsunagu.wakatta.core.media.DictationSource
import com.techtsunagu.wakatta.core.media.QrMatrix
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CacheEntry
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.QrEncoder
import com.techtsunagu.wakatta.core.model.QrScanner
import com.techtsunagu.wakatta.core.model.ShareFile
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.feature.connect.data.FakeConnectRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

fun newRepo() = FakeConnectRepository(null, 0L)

/** A 3x3 grid; the real encoder is covered in `:core:media`. */
class StubEncoder : QrEncoder {
    override fun encode(content: String, width: Int, height: Int): Any = QrMatrix(3, 3, BooleanArray(9) { it % 2 == 0 })
}

class StubScanner(private val results: Flow<String> = emptyFlow()) : QrScanner {
    override fun scanResults(): Flow<String> = results
}

class RecordingShare : ShareFile {
    val shared = mutableListOf<Pair<File, String>>()
    override fun share(file: File, mimeType: String) {
        shared += file to mimeType
    }
}

class RecordingSpeaker(private val available: Boolean = true) : Speaker {
    val spoken = mutableListOf<String>()
    override fun speak(text: String, languageTag: String) {
        spoken += text
    }
    override fun isAvailable(languageTag: String) = available
    override fun stop() = Unit
}

class StubDictationSource(
    private val available: Boolean = true,
    private val events: Flow<DictationEvent> = emptyFlow(),
) : DictationSource {
    val started = mutableListOf<String>()
    var isStopped = false
    override fun isAvailable() = available
    override fun start(languageTag: String): Flow<DictationEvent> {
        started += languageTag
        return events
    }
    override fun stop() {
        isStopped = true
    }
}

/** The seeded token of a sample person (what a scan of that person's QR reads). */
suspend fun FakeConnectRepository.tokenOf(name: String): String = getSamplePeople().first { it.name == name }.token

suspend fun <T> AppResult<T>.value(): T = (this as AppResult.Success).value

class MemoryCacheStore : CacheStore {
    val entries = mutableMapOf<String, CacheEntry>()
    override suspend fun get(key: String): CacheEntry? = entries[key]
    override suspend fun put(key: String, json: String, etag: String?) {
        entries[key] = CacheEntry(key, json, etag)
    }
    override suspend fun evict(prefix: String) {
        entries.keys.removeAll { it.startsWith(prefix) }
    }
}
