package com.techtsunagu.wakatta.core.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Records an m4a for the server to transcribe, on phones whose speech recognition is missing or fails. */
interface VoiceRecorder {
    /** False when the microphone cannot be opened (another app holds it, or no permission). */
    fun start(): Boolean

    /** The recording, or null when nothing usable was recorded. */
    fun stop(): File?

    fun cancel()
}

@Singleton
class DeviceVoiceRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) : VoiceRecorder {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    override fun start(): Boolean {
        cancel()
        val out = File(context.cacheDir, "dictation.m4a").also { it.delete() }
        @Suppress("DEPRECATION")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(16_000)
            r.setAudioEncodingBitRate(32_000)
            r.setMaxDuration(RecordingLimits.MAX_DURATION_MS.toInt())
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            file = out
            true
        } catch (_: Exception) {
            r.release()
            false
        }
    }

    override fun stop(): File? {
        val r = recorder ?: return null
        recorder = null
        val ok = try {
            r.stop()
            true
        } catch (_: RuntimeException) {
            false // stop() throws when almost nothing was recorded
        } finally {
            r.release()
        }
        return file?.takeIf { ok && it.length() > 0 && RecordingLimits.isWithinSize(it.length()) }
    }

    override fun cancel() {
        recorder?.let { r ->
            try { r.stop() } catch (_: RuntimeException) {}
            r.release()
        }
        recorder = null
        file?.delete()
    }
}
