package com.techtsunagu.wakatta.feature.course.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.techtsunagu.wakatta.feature.course.domain.TonePlayer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

private const val SAMPLE_RATE = 22_050

/** Generates the tone in memory, so no sound files ship in the APK. */
@Singleton
class AudioTrackTonePlayer @Inject constructor() : TonePlayer {

    private var track: AudioTrack? = null

    @Synchronized
    override fun play(hz: Int, millis: Int) {
        track?.release()
        val samples = SAMPLE_RATE * millis / 1000
        val pcm = ShortArray(samples) { (sin(2 * PI * hz * it / SAMPLE_RATE) * Short.MAX_VALUE * 0.3).toInt().toShort() }
        track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .also {
                it.write(pcm, 0, pcm.size)
                it.play()
            }
    }
}
