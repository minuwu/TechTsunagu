package com.techtsunagu.wakatta.feature.support

import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.ShareFile
import com.techtsunagu.wakatta.feature.support.data.FakeInterviewRepository
import com.techtsunagu.wakatta.feature.support.data.FakeSupportLogRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

class RecordingOutbox : OutboxWriter {
    val payloads = mutableListOf<String>()
    override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
        payloads += payloadJson
    }
    override fun pendingCount(kind: String): Flow<Int> = flowOf(0)
}

class RecordingShare : ShareFile {
    val shared = mutableListOf<Pair<File, String>>()
    override fun share(file: File, mimeType: String) {
        shared += file to mimeType
    }
}

fun newInterviewRepo(outbox: OutboxWriter = RecordingOutbox()) = FakeInterviewRepository(outbox, 0L)

fun newLogRepo(interviews: FakeInterviewRepository = newInterviewRepo()) =
    FakeSupportLogRepository(interviews, null, 0L)
