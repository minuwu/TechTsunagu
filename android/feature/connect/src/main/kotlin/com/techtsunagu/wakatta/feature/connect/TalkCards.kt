package com.techtsunagu.wakatta.feature.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.TalkGrade
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.connect.domain.TalkEntryDto

/** The chip colour: what the other person said wins over the owner's own grade. */
internal fun entryStatus(entry: TalkEntryDto): WakattaStatus? = when {
    entry.confirmed == true -> WakattaStatus.GREEN
    entry.confirmed == false -> WakattaStatus.RED
    entry.grade == TalkGrade.GREEN -> WakattaStatus.GREEN
    entry.grade == TalkGrade.ORANGE -> WakattaStatus.ORANGE
    entry.grade == TalkGrade.RED -> WakattaStatus.RED
    else -> null
}

@Composable
internal fun ExchangeCard(entry: TalkEntryDto, lang: String, onEvent: (TalkEvent) -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SaidHeader(entry, onEvent)
            if (entry.grade == null) {
                AttemptSection(entry, onEvent)
            } else {
                ResultSection(entry, lang)
                MeaningSection(entry, lang)
                if (entry.needsStaff) StaffFlag()
                ReplySection(entry, lang, onEvent)
            }
        }
    }
}

@Composable
private fun SaidHeader(entry: TalkEntryDto, onEvent: (TalkEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(
            text = stringResource(R.string.connect_said, entry.at),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        entryStatus(entry)?.let { StatusChip(status = it) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = entry.said,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onEvent(TalkEvent.Say(entry.id, reply = false)) }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = stringResource(R.string.connect_play_again))
        }
    }
}

@Composable
private fun AttemptSection(entry: TalkEntryDto, onEvent: (TalkEvent) -> Unit) {
    var text by rememberSaveable(entry.id) { mutableStateOf("") }
    Text(stringResource(R.string.connect_your_words), style = MaterialTheme.typography.labelLarge)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.connect_example)) },
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { onEvent(TalkEvent.Understand(entry.id, text)) },
            enabled = text.isNotBlank(),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.connect_check))
        }
    }
    TextButton(onClick = { onEvent(TalkEvent.ShowMeaning(entry.id)) }, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
        Text(stringResource(R.string.connect_show_meaning))
    }
}

@Composable
private fun ResultSection(entry: TalkEntryDto, lang: String) {
    if (!entry.ownText.isNullOrEmpty()) {
        Text(stringResource(R.string.connect_your_understanding), style = MaterialTheme.typography.labelMedium)
        Text(entry.ownText, style = MaterialTheme.typography.bodyLarge)
    }
    val message = when {
        entry.skipped -> R.string.connect_grade_skipped
        entry.grade == TalkGrade.GREEN -> R.string.connect_grade_green
        entry.grade == TalkGrade.ORANGE -> R.string.connect_grade_orange
        else -> R.string.connect_grade_red
    }
    ResultRow(status = entryStatus(entry) ?: WakattaStatus.WAIT, text = stringResource(message)) {
        if (entry.grade != TalkGrade.GREEN) TriText(entry.hint, lang, style = MaterialTheme.typography.bodySmall)
    }
}

/** A coloured icon with a short message; the colour only says understood (green), close (amber) or not (red). */
@Composable
private fun ResultRow(status: WakattaStatus, text: String, extra: @Composable () -> Unit = {}) {
    val color = StatusColors.forStatus(status)
    val icon: ImageVector = when (status) {
        WakattaStatus.GREEN -> Icons.Default.Check
        WakattaStatus.RED -> Icons.Default.Close
        else -> Icons.Default.Info
    }
    Surface(color = color.containerColor, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = color.color)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text, style = MaterialTheme.typography.bodyMedium, color = color.onContainerColor)
                extra()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeaningSection(entry: TalkEntryDto, lang: String) {
    Text(stringResource(R.string.connect_meaning), style = MaterialTheme.typography.labelMedium)
    TriText(entry.meaning, lang, style = MaterialTheme.typography.titleSmall)
    if (lang != "ja") Text(entry.plainJa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        entry.terms.forEach { term ->
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                Text(
                    text = "${term.from} → ${term.to}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StaffFlag() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.connect_icon_info), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.connect_staff_flag), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ReplySection(entry: TalkEntryDto, lang: String, onEvent: (TalkEvent) -> Unit) {
    when {
        entry.replyJa == null -> ChooseReply(entry, lang, onEvent)
        entry.confirmed == null -> ShowToThem(entry, entry.replyJa, onEvent)
        entry.confirmed -> ResultRow(WakattaStatus.GREEN, stringResource(R.string.connect_confirmed_yes))
        else -> {
            ResultRow(WakattaStatus.RED, stringResource(R.string.connect_confirmed_no))
            FilledTonalButton(onClick = { onEvent(TalkEvent.RetryReply(entry.id)) }, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
                Text(stringResource(R.string.connect_try_another))
            }
        }
    }
}

@Composable
private fun ChooseReply(entry: TalkEntryDto, lang: String, onEvent: (TalkEvent) -> Unit) {
    Text(stringResource(R.string.connect_choose_reply), style = MaterialTheme.typography.labelLarge)
    entry.replyOptions.forEachIndexed { index, option ->
        OutlinedButton(
            onClick = { onEvent(TalkEvent.Reply(entry.id, index)) },
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(if (lang == "ja") option.ja else option.en)
                if (lang != "ja") Text(option.ja, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ShowToThem(entry: TalkEntryDto, replyJa: String, onEvent: (TalkEvent) -> Unit) {
    Text(stringResource(R.string.connect_show_screen), style = MaterialTheme.typography.labelLarge)
    Text(replyJa, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    FilledTonalButton(
        onClick = { onEvent(TalkEvent.Say(entry.id, reply = true)) },
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
    ) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.connect_read_aloud))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onEvent(TalkEvent.Confirm(entry.id, understood = true)) },
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
        ) { Text(stringResource(R.string.connect_yes)) }
        OutlinedButton(
            onClick = { onEvent(TalkEvent.Confirm(entry.id, understood = false)) },
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
        ) { Text(stringResource(R.string.connect_no)) }
    }
}
