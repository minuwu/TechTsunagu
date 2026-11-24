package com.techtsunagu.wakatta.core.media

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** The state of a microphone button: [toggle] starts and stops, [errorRes] is the message to show. */
class Dictation internal constructor(
    val isListening: Boolean,
    val seconds: Int,
    val errorRes: Int?,
    val toggle: () -> Unit,
)

/**
 * Voice to text for a text field: the spoken words are added to [text] until the user taps again.
 * Asks for the microphone at the tap. [language] is a short code such as "ja".
 */
@Composable
fun rememberDictation(language: String, text: String, onText: (String) -> Unit): Dictation {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentText by rememberUpdatedState(text)
    val currentOnText by rememberUpdatedState(onText)
    var job by remember { mutableStateOf<Job?>(null) }
    var seconds by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<Int?>(null) }

    fun listen() {
        val source = EntryPointAccessors.fromApplication(context.applicationContext, MediaEntryPoint::class.java).dictation()
        error = null
        seconds = 0
        if (!source.isAvailable()) {
            error = R.string.media_dictation_unavailable
            return
        }
        job = scope.launch {
            val clock = launch {
                while (isActive) {
                    delay(1_000)
                    seconds++
                }
            }
            source.continuous(dictationTag(language), currentText).collect { event ->
                when (event) {
                    is DictationEvent.Partial -> currentOnText(event.text)
                    is DictationEvent.Final -> currentOnText(event.text)
                    is DictationEvent.Error -> error = when (event.kind) {
                        DictationErrorKind.NO_SPEECH -> R.string.media_dictation_no_speech
                        DictationErrorKind.NO_PERMISSION -> R.string.media_mic_denied
                        else -> R.string.media_dictation_unavailable
                    }
                    DictationEvent.Listening -> Unit
                }
            }
            clock.cancel()
            job = null
        }
    }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen() else error = R.string.media_mic_denied
    }
    return Dictation(job != null, seconds, error) {
        val running = job
        when {
            running != null -> {
                running.cancel()
                job = null
            }
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> listen()
            else -> ask.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

/** The recognizer ends at each pause; this starts it again, and every event carries the whole text so far. */
internal fun DictationSource.continuous(languageTag: String, initial: String): Flow<DictationEvent> = flow {
    val text = DictationAccumulator(initial)
    var silent = 0
    while (true) {
        var heard = false
        var failed: DictationErrorKind? = null
        start(languageTag).collect { event ->
            when (event) {
                is DictationEvent.Partial -> emit(DictationEvent.Partial(text.onPartial(event.text)))
                is DictationEvent.Final -> {
                    heard = true
                    emit(DictationEvent.Final(text.onFinal(event.text)))
                }
                is DictationEvent.Error -> failed = event.kind
                DictationEvent.Listening -> emit(event)
            }
        }
        silent = if (heard) 0 else silent + 1
        val kind = failed ?: DictationErrorKind.NO_SPEECH
        if (kind != DictationErrorKind.NO_SPEECH || silent >= MAX_SILENT_ROUNDS) {
            emit(DictationEvent.Error(kind))
            return@flow
        }
    }
}

private const val MAX_SILENT_ROUNDS = 3
