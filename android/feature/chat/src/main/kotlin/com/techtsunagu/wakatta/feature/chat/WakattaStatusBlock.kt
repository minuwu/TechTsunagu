package com.techtsunagu.wakatta.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel

private val STEPS = listOf(
    R.string.chat_wk_step_1, R.string.chat_wk_step_2, R.string.chat_wk_step_3,
    R.string.chat_wk_step_4, R.string.chat_wk_step_5, R.string.chat_wk_step_6,
)
private val ING = listOf(
    R.string.chat_wk_ing_1, R.string.chat_wk_ing_2, R.string.chat_wk_ing_3,
    R.string.chat_wk_ing_4, R.string.chat_wk_ing_5, R.string.chat_wk_ing_6,
)
private const val LAST_STAGE = 5

private fun statusLabel(s: WakattaStatus) = when (s) {
    WakattaStatus.GREEN -> R.string.chat_status_green
    WakattaStatus.ORANGE -> R.string.chat_status_orange
    WakattaStatus.RED -> R.string.chat_status_red
    else -> R.string.chat_status_wait
}

/** The Foreman's fold under a voice message (prototype .ch-wk): a pill that opens a progress board. */
@Composable
internal fun ForemanWakattaPill(
    message: ChatMessageUiModel,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = uiLanguage()
    val stage = message.stage
    val done = stage >= LAST_STAGE
    val statuses = message.workerStatuses
    val allIn = statuses.isNotEmpty() && statuses.values.none { it == WakattaStatus.WAIT }
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        // pill
        Box(Modifier.fillMaxWidth().clickable { onEvent(ChatEvent.ToggleWkFold(message.id)) }) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(start = 14.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (done) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        statuses.values.forEach { Dot(it) }
                    }
                    Text(
                        summary(statuses.values.toList()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.5.dp)
                    Text(
                        stringResource(ING[(stage + 1).coerceIn(0, LAST_STAGE)]) + "… ${stage + 2}/6",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                }
                val turn by animateFloatAsState(if (message.isWkFoldExpanded) 180f else 0f, tween(300), label = "chev")
                Icon(
                    Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp).graphicsLayer { rotationZ = turn },
                )
            }
            if (!done) {
                val pct by animateFloatAsState((stage + 1) / 6f, tween(800), label = "prog")
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(pct)
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        // board
        AnimatedVisibility(
            visible = message.isWkFoldExpanded,
            enter = expandVertically(tween(300)) + fadeIn(),
            exit = shrinkVertically(tween(300)) + fadeOut(),
        ) {
            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Heading(stringResource(R.string.chat_wk_progress_title))
                STEPS.chunked(2).forEachIndexed { row, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pair.forEachIndexed { col, label ->
                            val i = row * 2 + col
                            val dn = if (i == LAST_STAGE) done && allIn else i <= stage
                            val active = !dn && (i == stage + 1 || (i == LAST_STAGE && done))
                            Step(stringResource(label), dn, active, Modifier.weight(1f))
                        }
                    }
                }

                if (message.senderQuestions.isNotEmpty()) {
                    Heading(stringResource(R.string.chat_wk_questions, message.senderQuestions.size))
                    message.senderQuestions.forEachIndexed { qi, q -> QuestionCard(qi, pick(q.prompt, lang), q.options.map { pick(it, lang) }, q.correctIndex) }
                }

                Heading(stringResource(R.string.chat_wk_answers))
                statuses.forEach { (workerId, status) ->
                    WorkerRow(message, workerId, status, done, onEvent)
                }
            }
        }
    }
}

@Composable
private fun summary(list: List<WakattaStatus>) = buildAnnotatedString {
    var first = true
    listOf(WakattaStatus.GREEN, WakattaStatus.ORANGE, WakattaStatus.RED, WakattaStatus.WAIT).forEach { s ->
        val c = list.count { it == s || (s == WakattaStatus.WAIT && it == WakattaStatus.UNKNOWN) }
        if (c == 0) return@forEach
        if (!first) append(" · ")
        first = false
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append("$c") }
        append(" ")
        append(stringResource(statusLabel(s)))
    }
}

/** 14dp status dot; waiting is a hollow ring. */
@Composable
private fun Dot(status: WakattaStatus) {
    val wait = status == WakattaStatus.WAIT || status == WakattaStatus.UNKNOWN
    val m = Modifier.size(14.dp)
    if (wait) {
        Box(m.border(2.dp, StatusColors.Grey, CircleShape))
    } else {
        Box(m.background(StatusColors.forStatus(status).color, CircleShape))
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun Step(label: String, done: Boolean, active: Boolean, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(18.dp)
                .background(if (done) StatusColors.Green else MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                done -> Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                active -> CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionCard(index: Int, prompt: String, options: List<String>, correct: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(24.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Q${index + 1}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(prompt, style = MaterialTheme.typography.bodyMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                options.forEachIndexed { j, o ->
                    val ok = j == correct
                    Row(
                        Modifier
                            .height(28.dp)
                            .background(
                                if (ok) StatusColors.GreenContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (ok) {
                            Icon(Icons.Default.Check, null, tint = StatusColors.OnGreenContainer, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            o,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (ok) FontWeight.Medium else FontWeight.Normal),
                            color = if (ok) StatusColors.OnGreenContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerRow(
    message: ChatMessageUiModel,
    workerId: String,
    status: WakattaStatus,
    done: Boolean,
    onEvent: (ChatEvent) -> Unit,
) {
    val name = message.workerNames[workerId] ?: workerId
    val answers = message.workerAnswerMap[workerId]
    val read = workerId in message.workerRead
    val waiting = status == WakattaStatus.WAIT || status == WakattaStatus.UNKNOWN
    Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
        Avatar(name = name, size = 32.dp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                message.senderQuestions.forEachIndexed { i, q ->
                    val a = answers?.getOrNull(i)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Q${i + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        when {
                            waiting || a == null -> Text(" –", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            a == q.correctIndex -> Icon(Icons.Default.Check, null, tint = StatusColors.Green, modifier = Modifier.size(14.dp))
                            else -> Icon(Icons.Default.Close, null, tint = StatusColors.Red, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (read) {
                        Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                    }
                    Text(
                        stringResource(if (read) R.string.chat_wk_read else R.string.chat_wk_unread),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        StatusPill(status)
    }
    // indented action under the name (prototype .ch-again: margin-left 42px)
    val nudged = workerId in message.workerNudged
    when {
        status == WakattaStatus.ORANGE || status == WakattaStatus.RED -> ActionChip(
            Icons.Default.Refresh,
            stringResource(R.string.chat_reexplain),
        ) { onEvent(ChatEvent.OpenReexplain(message.id, workerId)) }
        waiting && done && nudged -> Row(
            Modifier.padding(start = 42.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.chat_nudged), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        waiting && done -> ActionChip(Icons.Default.NotificationsNone, stringResource(R.string.chat_nudge)) {
            onEvent(ChatEvent.NudgeWorker(message.id, workerId))
        }
    }
}

@Composable
private fun StatusPill(status: WakattaStatus) {
    val c = StatusColors.forStatus(status)
    val wait = status == WakattaStatus.WAIT || status == WakattaStatus.UNKNOWN
    Text(
        stringResource(statusLabel(status)),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        color = if (wait) MaterialTheme.colorScheme.onSurfaceVariant else c.onContainerColor,
        maxLines = 1,
        modifier = Modifier
            .background(if (wait) MaterialTheme.colorScheme.surfaceContainerHighest else c.containerColor, RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun ActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp),
        modifier = Modifier.padding(start = 42.dp).height(36.dp),
    ) {
        Icon(icon, null, Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
