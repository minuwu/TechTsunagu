package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
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
    showSender: Boolean = true,
) {
    val isMine = message.isMine
    val bubbleColor = if (isMine) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        if (!isMine && showSender) {
            Avatar(
                name = message.fromUserName.ifEmpty { "?" },
                containerColor = personColor(message.fromUserId),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(end = 8.dp, top = 18.dp)
                    // the Foreman: a thin primary ring with a gap, easy to find, never pulling the eye
                    .then(
                        if (message.fromForeman) {
                            Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape).padding(2.5.dp)
                        } else {
                            Modifier
                        },
                    ),
                size = if (message.fromForeman) 31.dp else 36.dp,
            )
        } else if (!isMine) {
            Spacer(Modifier.width(44.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            if (!isMine && showSender && message.fromUserName.isNotEmpty()) {
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
                    topStart = if (isMine) 20.dp else 6.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = if (isMine) 6.dp else 20.dp,
                ),
                color = bubbleColor,
                contentColor = textColor,
            ) {
                Column(modifier = Modifier.width(IntrinsicSize.Max).padding(horizontal = 14.dp, vertical = 10.dp)) {
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
                                tint = LocalContentColor.current,
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
                                    tint = LocalContentColor.current,
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
                        linkColor = if (isMine) textColor else MaterialTheme.colorScheme.primary,
                    )

                    // Translation toggle for Workers
                    if (!isForeman && !isMine && message.originalText.isNotEmpty() && message.originalText != message.translatedText) {
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
                        color = LocalContentColor.current.copy(alpha = 0.7f),
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
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp)
    val link = TextLinkStyles(
        SpanStyle(color = linkColor, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline),
    )
    val found = remember(text, terms) {
        terms.filter { it.ja.isNotEmpty() }.flatMap { term ->
            Regex(Regex.escape(term.ja)).findAll(text).map { it.range to term }.toList()
        }.sortedBy { it.first.first }
    }
    if (found.isEmpty()) {
        Text(text = text, style = style, modifier = modifier)
        return
    }
    val annotated = buildAnnotatedString {
        var at = 0
        found.forEach { (range, term) ->
            if (range.first < at) return@forEach // overlapping terms: the first one wins
            append(text.substring(at, range.first))
            withLink(LinkAnnotation.Clickable(term.ja, link) { onTermClick(term) }) { append(text.substring(range)) }
            at = range.last + 1
        }
        append(text.substring(at))
    }
    Text(text = annotated, style = style, modifier = modifier)
}

/** Eight calm tones; one person keeps one tone in every chat so faces are told apart at a glance. */
private val PersonTones = listOf(
    Color(0xFF1E6FB8), Color(0xFF2E7D5B), Color(0xFFB4543A), Color(0xFF7A5AA6),
    Color(0xFF0E7C86), Color(0xFF9A6B12), Color(0xFFB03A6E), Color(0xFF4F6A2E),
)

internal fun personColor(userId: String): Color = PersonTones[Math.floorMod(userId.hashCode(), PersonTones.size)]
