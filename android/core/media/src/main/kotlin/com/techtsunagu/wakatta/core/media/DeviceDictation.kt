package com.techtsunagu.wakatta.core.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

@Singleton
class DeviceDictation @Inject constructor(
    @ApplicationContext private val context: Context,
) : DictationSource {

    private val activeRecognizer = AtomicReference<SpeechRecognizer?>(null)
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    override fun start(languageTag: String): Flow<DictationEvent> {
        if (!isAvailable()) {
            return flow {
                emit(DictationEvent.Error(DictationErrorKind.UNAVAILABLE))
            }
        }

        return callbackFlow {
            val recognizer = withContext(Dispatchers.Main.immediate) {
                try {
                    SpeechRecognizer.createSpeechRecognizer(context).also {
                        activeRecognizer.set(it)
                    }
                } catch (_: Throwable) {
                    null
                }
            }

            if (recognizer == null) {
                trySend(DictationEvent.Error(DictationErrorKind.UNAVAILABLE))
                channel.close()
                return@callbackFlow
            }

            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    trySend(DictationEvent.Listening)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}

                override fun onError(error: Int) {
                    val kind = mapSpeechRecognizerError(error)
                    trySend(DictationEvent.Error(kind))
                    channel.close()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) {
                        trySend(DictationEvent.Final(text))
                    }
                    channel.close()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?: partialResults?.getStringArrayList("android.speech.extra.PARTIAL_RESULTS")
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) {
                        trySend(DictationEvent.Partial(text))
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            }

            withContext(Dispatchers.Main.immediate) {
                recognizer.setRecognitionListener(listener)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                recognizer.startListening(intent)
            }

            awaitClose {
                activeRecognizer.compareAndSet(recognizer, null)
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    try {
                        recognizer.cancel()
                        recognizer.destroy()
                    } catch (_: Throwable) {}
                } else {
                    mainHandler.post {
                        try {
                            recognizer.cancel()
                            recognizer.destroy()
                        } catch (_: Throwable) {}
                    }
                }
            }
        }
    }

    override fun stop() {
        val recognizer = activeRecognizer.get() ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            try {
                recognizer.stopListening()
            } catch (_: Throwable) {}
        } else {
            mainHandler.post {
                try {
                    recognizer.stopListening()
                } catch (_: Throwable) {}
            }
        }
    }
}
