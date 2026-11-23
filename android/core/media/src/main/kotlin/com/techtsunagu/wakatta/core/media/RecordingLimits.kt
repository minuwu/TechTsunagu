package com.techtsunagu.wakatta.core.media

object RecordingLimits {
    const val MAX_DURATION_SECONDS: Long = 60L
    const val MAX_DURATION_MS: Long = 60_000L
    const val MAX_FILE_BYTES: Long = 8 * 1024 * 1024L // 8 MB
    const val MIME_TYPE: String = "audio/mp4"

    fun isWithinDuration(durationMs: Long): Boolean = durationMs in 0..MAX_DURATION_MS
    fun isWithinSize(fileSizeBytes: Long): Boolean = fileSizeBytes in 0..MAX_FILE_BYTES
}
