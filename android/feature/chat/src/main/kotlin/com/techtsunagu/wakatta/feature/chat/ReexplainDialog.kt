package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDialogState

@Composable
internal fun ReexplainDialog(
    dialogState: ReexplainDialogState,
    onEvent: (ChatEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(ChatEvent.DismissReexplain) },
        title = {
            Text(
                text = stringResource(R.string.chat_reexplain_title, dialogState.workerName),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.chat_reexplain_confused_term, dialogState.confusingTerm),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.chat_reexplain_easy_ja),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
                OutlinedTextField(
                    value = dialogState.easyJaText,
                    onValueChange = { onEvent(ChatEvent.UpdateReexplainText(it, dialogState.explanation)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Text(
                    text = stringResource(R.string.chat_reexplain_worker_lang),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
                OutlinedTextField(
                    value = dialogState.explanation[dialogState.workerLang].orEmpty(),
                    onValueChange = {
                        onEvent(
                            ChatEvent.UpdateReexplainText(
                                dialogState.easyJaText,
                                dialogState.explanation + (dialogState.workerLang to it),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Text(
                    text = stringResource(R.string.chat_reexplain_new_question),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = pick(dialogState.newQuestionPrompt, "ja"),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        dialogState.newQuestionOptions.forEachIndexed { i, opt ->
                            Text(
                                text = "${i + 1}. ${pick(opt, "ja")} ${if (i == dialogState.correctOptionIndex) "✓" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(ChatEvent.SubmitReexplain(dialogState.messageId)) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.chat_reexplain_approve_send, dialogState.workerName))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(ChatEvent.DismissReexplain) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.chat_cancel))
            }
        },
    )
}
