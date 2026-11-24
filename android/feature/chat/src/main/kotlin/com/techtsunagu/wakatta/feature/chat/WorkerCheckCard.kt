package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.appLanguage
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

/** The UI language: question and option text follow it. */
@Composable
internal fun uiLanguage(): String = appLanguage(LocalConfiguration.current.locales[0].language)

/**
 * The Worker's Wakatta? Check: one question at a time, no right or wrong shown until the last answer is in;
 * then the server grades and sends the answer key, and the card shows each miss with the right answer.
 */
@Composable
internal fun WorkerCheckCard(
    message: ChatMessageUiModel,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = uiLanguage()
    val questions = message.workerQuestions
    // Picks so far; a list survives rotation as an IntArray.
    var picks by rememberSaveable(message.id) { mutableStateOf(intArrayOf()) }

    WakattaCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (message.kind == MessageKind.REEXPLAIN) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.chat_reexplain_for_you),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
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
                    text = stringResource(R.string.chat_questions_title, questions.size),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                )
                if (message.workerAnswerStatus == null) {
                    questions.indices.forEach { i ->
                        val color = if (i < picks.size) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                        Box(modifier = Modifier.size(if (i == picks.size) 10.dp else 8.dp).background(color, CircleShape))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val status = message.workerAnswerStatus
            if (status == null) {
                val i = picks.size.coerceAtMost(questions.lastIndex)
                val question = questions[i]
                Text(
                    text = stringResource(R.string.chat_question_n_of, i + 1, questions.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pick(question.prompt, lang),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                val choose = { option: Int ->
                    if (picks.size < questions.size) {
                        picks += option
                        if (picks.size == questions.size) onEvent(ChatEvent.SubmitAnswers(message.id, picks.toList()))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    question.options.forEachIndexed { optIdx, opt ->
                        OutlinedButton(
                            onClick = { choose(optIdx) },
                            enabled = picks.size < questions.size,
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) {
                            Text(text = pick(opt, lang), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    TextButton(
                        onClick = { choose(-1) },
                        enabled = picks.size < questions.size,
                        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.chat_option_not_sure), color = MaterialTheme.colorScheme.error)
                    }
                    // All picked but no result yet: the send failed or is on its way; the server keeps the first answer.
                    if (picks.size == questions.size) {
                        Button(
                            onClick = { onEvent(ChatEvent.SubmitAnswers(message.id, picks.toList())) },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                        ) { Text(text = stringResource(R.string.chat_retry)) }
                    }
                }
            } else {
                val answers = message.workerAnswers
                val key = message.answerKey
                val correct = key.indices.count { answers.getOrNull(it) == key[it] }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusColors.forStatus(status).color.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatusChip(status = status)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = when (status) {
                                    WakattaStatus.GREEN -> stringResource(R.string.chat_result_green)
                                    WakattaStatus.ORANGE -> stringResource(R.string.chat_result_orange)
                                    else -> stringResource(R.string.chat_result_red)
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            )
                            if (key.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.chat_correct_count, correct, key.size),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                // Each missed question with what the Worker picked and the right answer.
                questions.forEachIndexed { qi, q ->
                    val right = key.getOrNull(qi) ?: return@forEachIndexed
                    val mine = answers.getOrNull(qi)
                    if (mine == right) return@forEachIndexed
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = pick(q.prompt, lang),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        val yours = mine?.let { q.options.getOrNull(it) }?.let { pick(it, lang) }
                            ?: stringResource(R.string.chat_option_not_sure)
                        AnswerLine(ok = false, text = stringResource(R.string.chat_your_answer, yours))
                        q.options.getOrNull(right)?.let {
                            AnswerLine(ok = true, text = stringResource(R.string.chat_right_answer, pick(it, lang)))
                        }
                    }
                }

                if (status == WakattaStatus.GREEN && message.addedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.chat_added_to_board),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    message.addedTasks.forEach { taskTitle ->
                        Text(text = "• $taskTitle", style = MaterialTheme.typography.bodySmall)
                    }
                } else if (status != WakattaStatus.GREEN && message.kind != MessageKind.REEXPLAIN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (message.asked) {
                        Text(
                            text = stringResource(R.string.chat_asked),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Button(
                            onClick = { onEvent(ChatEvent.AskExplainAgain(message.id)) },
                            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.chat_ask_again))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerLine(ok: Boolean, text: String) {
    val color = StatusColors.forStatus(if (ok) WakattaStatus.GREEN else WakattaStatus.RED).color
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
        Icon(
            imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}
