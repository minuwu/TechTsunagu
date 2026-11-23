package com.techtsunagu.wakatta.feature.chat

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus

@Composable
internal fun ChatComposer(
    status: ComposerStatus,
    inputText: String,
    countdownSeconds: () -> Int,
    countdownProgress: () -> Float,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
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

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Mic Button
                IconButton(
                    onClick = {
                        if (status == ComposerStatus.RECORDING) {
                            onEvent(ChatEvent.StopRecording)
                        } else {
                            onEvent(ChatEvent.StartRecording)
                        }
                    },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.chat_mic),
                        tint = if (status == ComposerStatus.RECORDING) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
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
                IconButton(
                    onClick = { onEvent(ChatEvent.SendNow) },
                    enabled = inputText.isNotBlank() || status == ComposerStatus.COUNTDOWN,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.chat_send),
                        tint = if (inputText.isNotBlank() || status == ComposerStatus.COUNTDOWN) {
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
