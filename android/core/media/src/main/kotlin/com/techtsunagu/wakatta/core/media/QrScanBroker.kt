package com.techtsunagu.wakatta.core.media

import android.os.SystemClock
import com.techtsunagu.wakatta.core.model.QrScanner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Two different codes in frame give nothing; a value just passed on is ignored for [debounceMs]. */
internal class ScanGate(private val debounceMs: Long = 1_500) {
    private var last: String? = null
    private var lastAtMs = 0L

    fun accept(valuesInFrame: List<String>, nowMs: Long): String? {
        val value = valuesInFrame.distinct().singleOrNull() ?: return null
        if (value == last && nowMs - lastAtMs < debounceMs) return null
        last = value
        lastAtMs = nowMs
        return value
    }
}

/** The `QrScanner` the features inject. [QrCamera] feeds it; the features read and check the raw text. */
@Singleton
class QrScanBroker @Inject constructor() : QrScanner {
    private val gate = ScanGate()
    private val results = MutableSharedFlow<String>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun scanResults(): Flow<String> = results.asSharedFlow()

    internal fun onFrame(valuesInFrame: List<String>) {
        gate.accept(valuesInFrame, SystemClock.elapsedRealtime())?.let { results.tryEmit(it) }
    }
}
