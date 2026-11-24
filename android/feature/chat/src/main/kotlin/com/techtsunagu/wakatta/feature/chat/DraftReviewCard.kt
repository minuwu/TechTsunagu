package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

/**
 * The Foreman's look at the AI draft before any Worker gets it: Japanese, English, the check questions and Tasks,
 * then Send or Edit. An untranslated draft says so, and its Send says it goes out in Japanese only.
 */
@Composable
internal fun DraftReviewCard(
    message: ChatMessageUiModel,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = uiLanguage()
    val ja = message.text["ja"].orEmpty().ifEmpty { message.originalText }
    val en = message.text["en"].orEmpty()
    var editing by rememberSaveable(message.id) { mutableStateOf(false) }
    var edited by rememberSaveable(message.id) { mutableStateOf(ja) }

    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.chat_draft_review_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        )
        if (message.needsEdit) {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Text(
                    stringResource(R.string.chat_draft_not_translated_notice),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }
        }

        if (editing) {
            OutlinedTextField(
                value = edited,
                onValueChange = { edited = it },
                label = { Text(stringResource(R.string.chat_draft_japanese)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                OutlinedButton(onClick = { editing = false }, modifier = Modifier.sizeIn(minHeight = 48.dp)) {
                    Text(stringResource(R.string.chat_cancel))
                }
                Button(
                    onClick = {
                        onEvent(ChatEvent.EditDraft(message.id, edited.trim()))
                        editing = false
                    },
                    enabled = edited.isNotBlank(),
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(stringResource(R.string.chat_draft_save))
                }
            }
            return@Column
        }

        Labelled(stringResource(R.string.chat_draft_japanese), ja)
        if (en.isNotBlank()) Labelled(stringResource(R.string.chat_draft_english), en)
        if (message.senderQuestions.isNotEmpty()) {
            Labelled(
                stringResource(R.string.chat_wk_questions, message.senderQuestions.size),
                message.senderQuestions.mapIndexed { i, q ->
                    val answer = q.options.getOrNull(q.correctIndex)?.let { pick(it, lang) }.orEmpty()
                    "${i + 1}. ${pick(q.prompt, lang)} → $answer"
                }.joinToString("\n"),
            )
        }
        if (message.addedTasks.isNotEmpty()) {
            Labelled(stringResource(R.string.chat_draft_tasks), message.addedTasks.joinToString("\n") { "・$it" })
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            OutlinedButton(
                onClick = {
                    edited = ja
                    editing = true
                },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(stringResource(R.string.chat_draft_edit))
            }
            // a draft without Japanese has nothing to send: only Edit helps
            Button(
                onClick = { onEvent(ChatEvent.SendDraft(message.id)) },
                enabled = ja.isNotBlank(),
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(stringResource(if (message.needsEdit) R.string.chat_draft_send_japanese_only else R.string.chat_send))
            }
        }
    }
}

@Composable
private fun Labelled(label: String, text: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
