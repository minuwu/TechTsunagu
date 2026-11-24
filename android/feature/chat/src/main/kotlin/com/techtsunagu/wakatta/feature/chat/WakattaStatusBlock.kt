package com.techtsunagu.wakatta.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

@Composable
internal fun ForemanWakattaPill(
    message: ChatMessageUiModel,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = uiLanguage()
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { onEvent(ChatEvent.ToggleWkFold(message.id)) },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Status dots for workers
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    message.workerStatuses.values.forEach { st ->
                        val color = StatusColors.forStatus(st).color
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                val statuses = message.workerStatuses.values
                Text(
                    text = if (statuses.isEmpty()) stringResource(R.string.chat_wk_title)
                    else stringResource(
                        R.string.chat_wk_summary,
                        statuses.count { it == WakattaStatus.GREEN },
                        statuses.size,
                    ),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = if (message.isWkFoldExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        // Expanded Fold Details
        AnimatedVisibility(visible = message.isWkFoldExpanded) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.chat_wk_progress_title),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val allIn = message.workerStatuses.isNotEmpty() &&
                        message.workerStatuses.values.none { it == WakattaStatus.WAIT }
                    listOf(
                        R.string.chat_wk_step_1, R.string.chat_wk_step_2, R.string.chat_wk_step_3,
                        R.string.chat_wk_step_4, R.string.chat_wk_step_5, R.string.chat_wk_step_6,
                    ).forEachIndexed { i, step ->
                        val done = if (i == 5) allIn else i <= message.stage
                        Text(
                            text = (if (done) "✓ " else "○ ") + stringResource(step),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (message.senderQuestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.chat_wk_questions, message.senderQuestions.size),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        message.senderQuestions.forEachIndexed { qi, q ->
                            Text(
                                text = "Q${qi + 1}. ${pick(q.prompt, lang)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            q.options.getOrNull(q.correctIndex)?.let {
                                Text(text = "✓ ${pick(it, lang)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.chat_wk_worker_status),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    message.workerStatuses.forEach { (workerId, status) ->
                        val workerName = message.workerNames[workerId] ?: workerId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workerName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                )
                                val total = message.senderQuestions.size
                                val correct = message.workerAnswerMap[workerId]?.let { a ->
                                    message.senderQuestions.indices.count { a.getOrNull(it) == message.senderQuestions[it].correctIndex }
                                }
                                Text(
                                    text = stringResource(
                                        if (workerId in message.workerRead) R.string.chat_wk_read else R.string.chat_wk_unread,
                                    ) + (correct?.let { " · $it/$total" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusChip(status = status)

                            Spacer(modifier = Modifier.width(6.dp))

                            if (status == WakattaStatus.WAIT && workerId in message.workerNudged) {
                                Text(text = stringResource(R.string.chat_nudged), fontSize = 12.sp)
                            } else if (status == WakattaStatus.WAIT) {
                                OutlinedButton(
                                    onClick = { onEvent(ChatEvent.NudgeWorker(message.id, workerId)) },
                                    modifier = Modifier.sizeIn(minHeight = 36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(text = stringResource(R.string.chat_nudge), fontSize = 12.sp)
                                }
                            } else if (status != WakattaStatus.GREEN) {
                                Button(
                                    onClick = { onEvent(ChatEvent.OpenReexplain(message.id, workerId)) },
                                    modifier = Modifier.sizeIn(minHeight = 36.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(text = stringResource(R.string.chat_reexplain), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
