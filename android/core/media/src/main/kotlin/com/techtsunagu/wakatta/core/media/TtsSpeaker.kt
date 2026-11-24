package com.techtsunagu.wakatta.core.media

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
import com.techtsunagu.wakatta.core.model.Speaker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Read-aloud through the system TextToSpeech. The engine needs a moment to start: text asked for
 * before that is spoken when it is ready. A phone without the voice (often Bengali) says so.
 */
@Singleton
class TtsSpeaker @Inject constructor(@ApplicationContext private val context: Context) : Speaker {
    private val main = Handler(Looper.getMainLooper())
    private var ready: Boolean? = null // null: the engine is starting
    private var pending: Pair<String, String>? = null

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        main.post {
            ready = status == TextToSpeech.SUCCESS
            pending?.let { (text, tag) -> speak(text, tag) }
            pending = null
        }
    }

    override fun isAvailable(languageTag: String): Boolean = when (ready) {
        null -> true
        false -> false
        true -> runCatching { tts.isLanguageAvailable(Locale.forLanguageTag(languageTag)) >= TextToSpeech.LANG_AVAILABLE }
            .getOrDefault(false)
    }

    override fun speak(text: String, languageTag: String) {
        when {
            text.isBlank() -> Unit
            ready == null -> pending = text to languageTag
            !isAvailable(languageTag) -> Toast.makeText(context, R.string.media_voice_missing, Toast.LENGTH_LONG).show()
            else -> {
                tts.language = Locale.forLanguageTag(languageTag)
                tts.speak(text.take(TextToSpeech.getMaxSpeechInputLength()), TextToSpeech.QUEUE_FLUSH, null, "wakatta")
            }
        }
    }

    override fun stop() {
        pending = null
        tts.stop()
    }
}
