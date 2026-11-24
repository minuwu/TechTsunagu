package com.techtsunagu.wakatta

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Voice typing goes both ways (a Japanese Foreman, a Worker in English), so both offline packs are wanted. */
private val PACKS = listOf("ja-JP", "en-US")
private const val MANAGE_LANGUAGES = "com.google.android.speech.embedded.MANAGE_LANGUAGES"

/**
 * Asks once, after sign-in, to download the device's Japanese and English speech packs so dictation works offline.
 * Android 13+: only when a pack is missing, and the system downloads it. Older: opens Google's offline speech screen.
 */
@Composable
fun SpeechPackPrompt(signedIn: Boolean) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("speech_packs", Context.MODE_PRIVATE) }
    var missing by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(signedIn) {
        if (!signedIn || prefs.getBoolean("asked", false)) return@LaunchedEffect
        missing = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> missingPacks(context)
            Intent(MANAGE_LANGUAGES).resolveActivity(context.packageManager) != null -> PACKS
            else -> emptyList()
        }.takeIf { it.isNotEmpty() }
    }
    val packs = missing ?: return
    fun done() {
        prefs.edit().putBoolean("asked", true).apply()
        missing = null
    }
    AlertDialog(
        onDismissRequest = ::done,
        title = { Text(stringResource(R.string.app_speech_packs_title)) },
        text = { Text(stringResource(R.string.app_speech_packs_body)) },
        confirmButton = {
            TextButton(onClick = {
                download(context, packs)
                done()
            }) { Text(stringResource(R.string.app_speech_packs_download)) }
        },
        dismissButton = { TextButton(onClick = ::done) { Text(stringResource(R.string.app_speech_packs_later)) } },
    )
}

private fun download(context: Context, packs: List<String>) {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            packs.forEach { recognizer.triggerModelDownload(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE, it)) }
            recognizer.destroy()
        } else {
            context.startActivity(Intent(MANAGE_LANGUAGES).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun missingPacks(context: Context): List<String> {
    if (!SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) return emptyList()
    val recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
    return try {
        suspendCancellableCoroutine { cont ->
            recognizer.checkRecognitionSupport(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
                context.mainExecutor,
                object : RecognitionSupportCallback {
                    override fun onSupportResult(support: RecognitionSupport) {
                        val have = (support.installedOnDeviceLanguages + support.pendingOnDeviceLanguages).map { it.substringBefore('-') }
                        val can = support.supportedOnDeviceLanguages.map { it.substringBefore('-') }
                        cont.resume(PACKS.filter { it.substringBefore('-') !in have && it.substringBefore('-') in can })
                    }

                    override fun onError(error: Int) = cont.resume(emptyList())
                },
            )
        }
    } finally {
        recognizer.destroy()
    }
}
