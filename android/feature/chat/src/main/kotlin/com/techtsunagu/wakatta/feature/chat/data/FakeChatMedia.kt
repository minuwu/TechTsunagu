package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Speaker
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

@Singleton
class FakeAudioRecorder @Inject constructor() : AudioRecorder {
    private var isRecording = false

    override fun start(): Flow<Pair<Float, Int>> = flow {
        isRecording = true
        var elapsed = 0
        while (isRecording && elapsed <= 60) {
            val level = 0.3f + (elapsed % 5) * 0.15f
            emit(Pair(level, elapsed))
            delay(1000)
            elapsed++
        }
    }

    override suspend fun stop(): File? {
        isRecording = false
        return null
    }
}

@Singleton
class FakeSpeaker @Inject constructor() : Speaker {
    private var speaking = false

    override fun speak(text: String, languageTag: String) {
        speaking = true
    }

    override fun isAvailable(languageTag: String): Boolean = true

    override fun stop() {
        speaking = false
    }
}

@Singleton
class FakeOutboxWriter @Inject constructor() : OutboxWriter {
    private val pending = MutableStateFlow(0)

    override suspend fun enqueue(kind: String, idempotencyKey: String, payloadJson: String) {
        pending.value++
    }

    override fun pendingCount(kind: String): Flow<Int> = pending.asStateFlow()
}
