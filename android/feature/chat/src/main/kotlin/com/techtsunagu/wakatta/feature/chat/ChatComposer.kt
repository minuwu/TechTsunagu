package com.techtsunagu.wakatta.feature.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import kotlinx.coroutines.launch

@Composable
internal fun ChatComposer(
    status: ComposerStatus,
    inputText: String,
    countdownSeconds: () -> Int,
    countdownProgress: () -> Float,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
    hasPendingAudio: Boolean = false,
    onMicClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var permissionDeniedCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedCount = 0
            onEvent(ChatEvent.StartRecording)
        } else {
            permissionDeniedCount++
            if (permissionDeniedCount == 1 && snackbarHostState != null) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.chat_mic_rationale))
                }
            }
        }
    }

    val handleMicClick: () -> Unit = {
        if (onMicClick != null) {
            onMicClick()
        } else {
            if (status == ComposerStatus.RECORDING) {
                onEvent(ChatEvent.StopRecording)
            } else if (permissionDeniedCount >= 2) {
                if (snackbarHostState != null) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.chat_dictation_type_instead))
                    }
                }
            } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    onEvent(ChatEvent.StartRecording)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Auto-send Countdown Banner
            AnimatedVisibility(visible = status == ComposerStatus.COUNTDOWN) {
                CountdownBanner(countdownSeconds, countdownProgress, onEvent)
            }

            // Recording Banner
            AnimatedVisibility(visible = status == ComposerStatus.RECORDING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.chat_listening),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            // Pending Voice Message (after failed send or cancelled countdown)
            AnimatedVisibility(visible = hasPendingAudio && status == ComposerStatus.EDITING) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.chat_voice_message),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = { onEvent(ChatEvent.CancelCountdown) },
                        modifier = Modifier.sizeIn(minHeight = 40.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.chat_cancel),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Mic Button
                if (permissionDeniedCount >= 2 && status != ComposerStatus.RECORDING) {
                    TextButton(
                        onClick = handleMicClick,
                        modifier = Modifier.sizeIn(minHeight = 48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.chat_type_instead),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    IconButton(
                        onClick = handleMicClick,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = stringResource(R.string.chat_mic),
                            tint = if (status == ComposerStatus.RECORDING) Color.Red else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }

                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { onEvent(ChatEvent.OnInputTextChanged(it)) },
                    placeholder = { Text(text = stringResource(R.string.chat_input_placeholder)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                )

                // Send Button
                val canSend = inputText.isNotBlank() || status == ComposerStatus.COUNTDOWN || hasPendingAudio
                IconButton(
                    onClick = { onEvent(ChatEvent.SendNow) },
                    enabled = canSend,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send),
                        tint = if (canSend) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownBanner(
    countdownSeconds: () -> Int,
    countdownProgress: () -> Float,
    onEvent: (ChatEvent) -> Unit,
) {
    val seconds = countdownSeconds()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Countdown Ring drawn using drawBehind (Rule P-08)
            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .drawBehind {
                        val prog = countdownProgress()
                        val stroke = 3.dp.toPx()
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = stroke),
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = 360f * prog,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = seconds.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = stringResource(R.string.chat_auto_send_countdown, seconds),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f),
            )

            TextButton(
                onClick = { onEvent(ChatEvent.CancelCountdown) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.chat_cancel),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
