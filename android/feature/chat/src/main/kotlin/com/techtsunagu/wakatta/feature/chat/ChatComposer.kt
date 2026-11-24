package com.techtsunagu.wakatta.feature.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.launch

/**
 * The prototype composer (.ch-comp): live dictation with caret and wave, a 4 s ring around send with a seconds
 * badge, a green check pop when it goes, and a hint row that opens above the field whenever it is not idle.
 */
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

    val recording = status == ComposerStatus.RECORDING
    val counting = status == ComposerStatus.COUNTDOWN
    val sending = status == ComposerStatus.SENDING
    val dictating = recording || counting || sending
    val showSend = counting || sending || inputText.isNotBlank() || hasPendingAudio
    val fieldColor by animateColorAsState(
        if (recording) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        tween(300),
        label = "field",
    )
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.imePadding().padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 10.dp)) {
            AnimatedVisibility(
                visible = dictating || (hasPendingAudio && status == ComposerStatus.EDITING),
                enter = expandVertically(tween(300)) + fadeIn(),
                exit = shrinkVertically(tween(300)) + fadeOut(),
            ) {
                HintRow(status, countdownSeconds, onEvent)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .background(fieldColor, RoundedCornerShape(28.dp))
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (dictating) {
                        // Tap the transcript during the countdown to stop it and edit (prototype toEdit).
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable(enabled = counting) { onEvent(ChatEvent.CancelCountdown) }
                                .padding(vertical = 12.dp),
                        ) {
                            DictationText(inputText, caret = recording)
                            if (recording) Wave()
                        }
                    } else {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { onEvent(ChatEvent.OnInputTextChanged(it)) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
                            decorationBox = { inner ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        stringResource(R.string.chat_input_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                inner()
                            },
                        )
                    }
                }
                ActionButton(
                    showSend = showSend,
                    recording = recording,
                    counting = counting,
                    sending = sending,
                    typeInstead = permissionDeniedCount >= 2 && !recording,
                    countdownSeconds = countdownSeconds,
                    countdownProgress = countdownProgress,
                    onMic = handleMicClick,
                    onSend = { onEvent(ChatEvent.SendNow) },
                )
            }
        }
    }
}

private val blinkEasing = androidx.compose.animation.core.Easing { if (it < 0.5f) 0f else 1f }

@Composable
private fun HintRow(status: ComposerStatus, countdownSeconds: () -> Int, onEvent: (ChatEvent) -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            when (status) {
                ComposerStatus.RECORDING -> {
                    val blink by rememberInfiniteTransition(label = "rec").animateFloat(
                        1f, 0f, infiniteRepeatable(tween(500, easing = blinkEasing), RepeatMode.Reverse), label = "dot",
                    )
                    Box(Modifier.size(8.dp).graphicsLayer { alpha = blink }.background(StatusColors.Red, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.chat_listening), style = MaterialTheme.typography.bodyMedium, color = muted)
                }
                ComposerStatus.COUNTDOWN -> Text(
                    stringResource(R.string.chat_auto_send_countdown, countdownSeconds()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = muted,
                )
                ComposerStatus.SENDING -> {
                    Icon(Icons.Default.Check, null, tint = StatusColors.Green, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.chat_sending), style = MaterialTheme.typography.bodyMedium, color = muted)
                }
                else -> {
                    Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.chat_voice_message), style = MaterialTheme.typography.bodyMedium, color = muted)
                }
            }
        }
        if (status == ComposerStatus.COUNTDOWN || status == ComposerStatus.EDITING) {
            Row(
                Modifier
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp))
                    .clickable { onEvent(ChatEvent.CancelCountdown) }
                    .padding(start = 8.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.chat_cancel), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Live transcript with a blinking caret while listening (prototype .ch-dict / .ch-caret). */
@Composable
private fun DictationText(text: String, caret: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val blink by rememberInfiniteTransition(label = "caret").animateFloat(
        1f, 0f, infiniteRepeatable(tween(500, easing = blinkEasing), RepeatMode.Reverse), label = "c",
    )
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 6,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (caret) {
            Box(
                Modifier
                    .padding(start = 1.dp, bottom = 3.dp)
                    .size(width = 2.dp, height = 20.dp)
                    .graphicsLayer { alpha = blink }
                    .background(primary),
            )
        }
    }
}

/** 24 bars that breathe while listening (prototype .ch-wave). */
@Composable
private fun Wave() {
    val primary = MaterialTheme.colorScheme.primary
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(1400, easing = LinearEasing)), label = "p",
    )
    Canvas(Modifier.fillMaxWidth().height(30.dp).padding(top = 8.dp)) {
        val n = 24
        val gap = size.width / n
        repeat(n) { i ->
            val m = 0.35f + 0.65f * ((i * 37) % 11) / 10f
            val h = size.height * (0.14f + 0.86f * m * (0.5f + 0.5f * sin(phase * (1 + i % 3) + i)))
            val x = gap * i + gap / 2
            drawLine(primary, Offset(x, (size.height - h) / 2), Offset(x, (size.height + h) / 2), 3.dp.toPx(), StrokeCap.Round)
        }
    }
}

/** Mic and send share one 56dp slot and cross-scale at 0.4; a 68dp ring closes over 4 s, then a green check pops. */
@Composable
private fun ActionButton(
    showSend: Boolean,
    recording: Boolean,
    counting: Boolean,
    sending: Boolean,
    typeInstead: Boolean,
    countdownSeconds: () -> Int,
    countdownProgress: () -> Float,
    onMic: () -> Unit,
    onSend: () -> Unit,
) {
    if (typeInstead && !showSend) {
        TextButton(onClick = onMic, modifier = Modifier.sizeIn(minHeight = 56.dp)) {
            Text(stringResource(R.string.chat_type_instead), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        return
    }
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val track = MaterialTheme.colorScheme.primaryContainer
    val sendScale by animateFloatAsState(if (showSend) 1f else 0.4f, tween(280), label = "s")
    val ringAlpha by animateFloatAsState(if (counting || sending) 1f else 0f, tween(200), label = "r")
    val okScale by animateFloatAsState(
        if (sending) 1f else 0f,
        spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "ok",
    )
    val micColor by animateColorAsState(if (recording) StatusColors.Red else primary, label = "m")
    // one click target: the hidden circle must not swallow taps meant for the visible one
    Box(
        Modifier.size(56.dp).clickable(
            interactionSource = null,
            indication = ripple(bounded = false, radius = 28.dp),
            enabled = !sending,
        ) { if (showSend) onSend() else onMic() },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.requiredSize(68.dp).graphicsLayer { alpha = ringAlpha }) {
            val stroke = 4.dp.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arc = Size(size.width - stroke, size.height - stroke)
            drawArc(track, 0f, 360f, false, topLeft, arc, style = Stroke(stroke))
            val sweep = if (sending) 360f else 360f * (1f - countdownProgress())
            drawArc(primary, -90f, sweep, false, topLeft, arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        if (recording) {
            val k by rememberInfiniteTransition(label = "pulse").animateFloat(
                0f, 1f, infiniteRepeatable(tween(1300, easing = LinearOutSlowInEasing)), label = "k",
            )
            Box(
                Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = 1 + k
                        scaleY = 1 + k
                        alpha = 0.45f * (1 - k)
                    }
                    .background(StatusColors.Red, CircleShape),
            )
        }
        val sendAlpha = ((sendScale - 0.4f) / 0.6f).coerceIn(0f, 1f)
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer {
                    val s = 1.4f - sendScale
                    scaleX = s
                    scaleY = s
                    alpha = 1f - sendAlpha
                }
                .background(micColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Mic, stringResource(R.string.chat_mic), tint = onPrimary, modifier = Modifier.size(28.dp))
        }
        Box(
            Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = sendScale
                    scaleY = sendScale
                    alpha = sendAlpha
                }
                .background(primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.chat_send), tint = onPrimary, modifier = Modifier.size(24.dp))
            Box(
                Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = okScale
                        scaleY = okScale
                        alpha = okScale.coerceIn(0f, 1f)
                    }
                    .background(StatusColors.Green, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
        }
        if (counting) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .sizeIn(minWidth = 22.dp, minHeight = 22.dp)
                    .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(11.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    countdownSeconds().toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}
