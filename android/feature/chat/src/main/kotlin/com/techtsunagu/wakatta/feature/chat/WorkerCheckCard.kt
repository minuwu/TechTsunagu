package com.techtsunagu.wakatta.feature.chat

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
@OptIn(ExperimentalLayoutApi::class)
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

    val shape = RoundedCornerShape(16.dp)
    // prototype .ch-chk: a plain surface card under the bubble
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 14.dp),
    ) {
        if (message.kind == MessageKind.REEXPLAIN) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(R.string.chat_reexplain_for_you),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        val status = message.workerAnswerStatus
        // header: Wakatta? · N quick questions · progress dots
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text("Wakatta?", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), maxLines = 1)
            Text(
                pluralStringResource(R.plurals.chat_quick_questions, questions.size, questions.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (status == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    questions.indices.forEach { i ->
                        val filled = i <= picks.size
                        Box(
                            Modifier.size(8.dp).clip(CircleShape)
                                .background(if (filled) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.5.dp, if (filled) Color.Transparent else MaterialTheme.colorScheme.outline, CircleShape),
                        )
                    }
                }
            }
        }

        if (status == null) {
            val i = picks.size.coerceAtMost(questions.lastIndex)
            val choose = { option: Int ->
                if (picks.size < questions.size) {
                    picks += option
                    if (picks.size == questions.size) onEvent(ChatEvent.SubmitAnswers(message.id, picks.toList()))
                }
            }
            // each question slides in from the right (prototype ch-slide)
            AnimatedContent(
                targetState = i,
                transitionSpec = { (slideInHorizontally { it / 8 } + fadeIn()) togetherWith fadeOut() },
                label = "question",
            ) { qi ->
                val question = questions[qi]
                Column {
                    Text(
                        stringResource(R.string.chat_question_n_of, qi + 1, questions.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Text(
                        pick(question.prompt, lang),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    val enabled = picks.size < questions.size
                    question.options.forEachIndexed { optIdx, opt ->
                        OptionRow(pick(opt, lang), muted = false, enabled = enabled) { choose(optIdx) }
                    }
                    OptionRow(stringResource(R.string.chat_option_not_sure), muted = true, enabled = enabled) { choose(-1) }
                }
            }
            // All picked but no result yet: the send failed or is on its way; the server keeps the first answer.
            if (picks.size == questions.size) {
                FilledTonalButton(
                    onClick = { onEvent(ChatEvent.SubmitAnswers(message.id, picks.toList())) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).sizeIn(minHeight = 48.dp),
                ) { Text(stringResource(R.string.chat_retry)) }
            }
        } else {
            val answers = message.workerAnswers
            val key = message.answerKey
            val correct = key.indices.count { answers.getOrNull(it) == key[it] }
            val tone = StatusColors.forStatus(status).color
            // result banner (prototype .ch-res): big icon, the verdict, k of n correct
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(14.dp))
                    .background(tone.copy(alpha = 0.16f)).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    when (status) {
                        WakattaStatus.GREEN -> Icons.Default.CheckCircle
                        WakattaStatus.ORANGE -> Icons.Default.Warning
                        else -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = tone,
                    modifier = Modifier.size(34.dp),
                )
                Column {
                    Text(
                        when (status) {
                            WakattaStatus.GREEN -> stringResource(R.string.chat_result_green)
                            WakattaStatus.ORANGE -> stringResource(R.string.chat_result_orange)
                            else -> stringResource(R.string.chat_result_red)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    if (key.isNotEmpty()) {
                        Text(
                            stringResource(R.string.chat_correct_count, correct, key.size),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Each missed question with what the Worker picked and the right answer.
            questions.forEachIndexed { qi, q ->
                val right = key.getOrNull(qi) ?: return@forEachIndexed
                val mine = answers.getOrNull(qi)
                if (mine == right) return@forEachIndexed
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(pick(q.prompt, lang), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    val yours = mine?.let { q.options.getOrNull(it) }?.let { pick(it, lang) }
                        ?: stringResource(R.string.chat_option_not_sure)
                    AnswerLine(ok = false, text = stringResource(R.string.chat_your_answer, yours))
                    q.options.getOrNull(right)?.let {
                        AnswerLine(ok = true, text = stringResource(R.string.chat_right_answer, pick(it, lang)))
                    }
                }
            }

            if (status == WakattaStatus.GREEN && message.addedTasks.isNotEmpty()) {
                Row(
                    Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusColors.forStatus(WakattaStatus.GREEN).color,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        stringResource(R.string.chat_added_to_board),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlowRow(
                    Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    message.addedTasks.forEach { title -> SuggestionChip(onClick = {}, label = { Text(title) }) }
                }
            } else if (status != WakattaStatus.GREEN && message.kind != MessageKind.REEXPLAIN) {
                if (message.asked) {
                    Text(
                        stringResource(R.string.chat_asked),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                } else {
                    FilledTonalButton(
                        onClick = { onEvent(ChatEvent.AskExplainAgain(message.id)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).sizeIn(minHeight = 48.dp),
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.chat_ask_again))
                    }
                }
            }
        }
    }
}

/** One answer choice (prototype .ch-or): a 52dp row, radius 14, outlined; "Not sure" is muted, never red. */
@Composable
private fun OptionRow(text: String, muted: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .sizeIn(minHeight = 52.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text,
            style = if (muted) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
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
