package com.techtsunagu.wakatta.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

@Composable
internal fun ChatMessageBubble(
    message: ChatMessageUiModel,
    isForeman: Boolean,
    glossaryTerms: List<GlossaryTerm>,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMine = message.isMine
    val bubbleColor = if (isMine) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        if (!isMine) {
            Avatar(
                name = message.fromUserName,
                size = 36.dp,
                modifier = Modifier.padding(end = 8.dp, top = 4.dp),
            )
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            if (!isMine) {
                Text(
                    text = message.fromUserName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                )
            }

            // Message Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMine) 16.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 16.dp,
                ),
                color = bubbleColor,
                contentColor = textColor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Voice playback bar
                    if (message.isVoice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.chat_voice_message),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.voiceDuration.orEmpty(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { onEvent(ChatEvent.PlayVoice(message.originalText, "ja")) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.chat_read_aloud),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    // Main text: always the original as spoken; a Worker opens the translation below it
                    val displayText = message.originalText.ifEmpty { message.translatedText }

                    GlossaryHighlightedText(
                        text = displayText,
                        terms = glossaryTerms,
                        onTermClick = { onEvent(ChatEvent.SelectGlossaryTerm(it)) },
                    )

                    // Translation toggle for Workers
                    if (!isForeman && message.originalText.isNotEmpty() && message.originalText != message.translatedText) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (message.showOriginal) {
                                stringResource(R.string.chat_hide_translation)
                            } else {
                                stringResource(R.string.chat_show_translation)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            ),
                            modifier = Modifier
                                .clickable { onEvent(ChatEvent.ToggleShowOriginal(message.id)) }
                                .padding(vertical = 4.dp),
                        )
                        AnimatedVisibility(visible = message.showOriginal) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                            ) {
                                Text(
                                    text = message.translatedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message.time,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }

            // Nudged Banner for Worker
            if (!isForeman && message.isNudged) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.chat_nudged_notice),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chat_nudged_notice),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            // Check Questions Flow for Worker
            if (!isForeman && message.hasCheck && message.workerQuestions.isNotEmpty()) {
                WorkerCheckCard(
                    message = message,
                    onEvent = onEvent,
                )
            }

            // Wakatta Status Pill / Fold for Foreman
            if (isForeman && message.hasCheck && isMine) {
                ForemanWakattaPill(
                    message = message,
                    onEvent = onEvent,
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GlossaryHighlightedText(
    text: String,
    terms: List<GlossaryTerm>,
    onTermClick: (GlossaryTerm) -> Unit,
    modifier: Modifier = Modifier,
) {
    // If text contains a glossary term, render interactive text
    val matchedTerm = terms.firstOrNull { text.contains(it.ja) }
    if (matchedTerm != null) {
        val parts = text.split(matchedTerm.ja)
        FlowRow(modifier = modifier) {
            parts.forEachIndexed { index, part ->
                if (part.isNotEmpty()) {
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    )
                }
                if (index < parts.size - 1) {
                    Text(
                        text = matchedTerm.ja,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 15.sp,
                        ),
                        modifier = Modifier
                            .clickable { onTermClick(matchedTerm) }
                            .padding(horizontal = 2.dp),
                    )
                }
            }
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            modifier = modifier,
        )
    }
}
