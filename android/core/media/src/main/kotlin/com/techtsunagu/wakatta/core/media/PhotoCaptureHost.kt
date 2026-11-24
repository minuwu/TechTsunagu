package com.techtsunagu.wakatta.core.media

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Place once, at the root of the app: asks "camera or gallery" for each request of [PhotoCaptureBroker]. */
@Composable
fun PhotoCaptureHost(broker: PhotoCaptureBroker) {
    val request by broker.request.collectAsStateWithLifecycle()
    var waiting by rememberSaveable { mutableStateOf(false) } // a system screen is open
    fun done(result: Uri?) {
        waiting = false
        broker.request.value?.reply?.complete(result)
    }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { done(it) }
    val take = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        done(if (ok) broker.request.value?.cameraUri else null)
    }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val uri = broker.request.value?.cameraUri
        // A refusal, or a phone without a camera app, brings the choice back.
        if (!granted || uri == null || runCatching { take.launch(uri) }.isFailure) waiting = false
    }
    if (request == null || waiting) return
    AlertDialog(
        onDismissRequest = { done(null) },
        title = { Text(stringResource(R.string.media_photo_title)) },
        confirmButton = {
            TextButton(
                onClick = {
                    waiting = true
                    // The manifest declares CAMERA, so the system camera refuses to start without it.
                    askCamera.launch(Manifest.permission.CAMERA)
                },
            ) { Text(stringResource(R.string.media_photo_take)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    waiting = true
                    pick.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            ) { Text(stringResource(R.string.media_photo_gallery)) }
        },
    )
}
