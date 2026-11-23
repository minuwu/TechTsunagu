package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

@Composable
internal fun WorkerCheckCard(
    message: ChatMessageUiModel,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    WakattaCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.chat_questions_title, message.workerQuestions.size),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (message.workerAnswerStatus == null) {
                // Questions list
                message.workerQuestions.forEachIndexed { qIdx, question ->
                    Text(
                        text = "${qIdx + 1}. ${pick(question.prompt, "en")}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        question.options.forEachIndexed { optIdx, opt ->
                            OutlinedButton(
                                onClick = {
                                    onEvent(ChatEvent.AnswerQuestion(message.id, qIdx, optIdx))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 48.dp),
                            ) {
                                Text(
                                    text = pick(opt, "en"),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        // "Not sure" button
                        TextButton(
                            onClick = {
                                onEvent(ChatEvent.AnswerQuestion(message.id, qIdx, -1))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 48.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.chat_option_not_sure),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            } else {
                // Result state
                val status = message.workerAnswerStatus
                StatusChip(status = status)
                Spacer(modifier = Modifier.height(6.dp))

                val resultText = when (status) {
                    WakattaStatus.GREEN -> stringResource(R.string.chat_result_green)
                    WakattaStatus.ORANGE -> stringResource(R.string.chat_result_orange)
                    else -> stringResource(R.string.chat_result_red)
                }
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                )

                if (status == WakattaStatus.GREEN && message.addedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.chat_added_to_board),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    message.addedTasks.forEach { taskTitle ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                        ) {
                            Text(
                                text = "• $taskTitle",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(6.dp),
                            )
                        }
                    }
                } else if (status != WakattaStatus.GREEN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onEvent(ChatEvent.AskExplainAgain(message.id)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.chat_ask_again))
                    }
                }
            }
        }
    }
}
