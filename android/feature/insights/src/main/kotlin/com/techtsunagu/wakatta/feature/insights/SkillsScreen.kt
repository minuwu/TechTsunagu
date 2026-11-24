package com.techtsunagu.wakatta.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.insights.domain.SkillCell
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private sealed interface GridEntry {
    val key: String

    data object Corner : GridEntry { override val key = "corner" }
    data class MachineHead(val id: String, val name: String) : GridEntry { override val key = "m:$id" }
    data class WorkerHead(val id: String, val name: String) : GridEntry { override val key = "w:$id" }
    data class Cell(val workerId: String, val workerName: String, val machineName: String, val cell: SkillCell) : GridEntry {
        override val key = "c:$workerId|${cell.machineId}"
    }
}

@Composable
fun SkillsScreen(
    onBackClick: () -> Unit = {},
    onOpenLearning: (String) -> Unit = {},
    viewModel: SkillsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SkillsContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick, onOpenLearning = onOpenLearning)
}

@Composable
fun SkillsContent(
    state: SkillsUiState,
    onEvent: (SkillsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOpenLearning: (String) -> Unit = {},
) {
    InsightsScaffold(
        title = stringResource(R.string.insights_skills_screen_title),
        onBackClick = onBackClick,
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.isEmpty,
        emptyMessage = stringResource(R.string.insights_skills_empty),
        onRetry = { onEvent(SkillsEvent.Retry) },
        modifier = modifier,
    ) {
        state.matrix?.let { matrix ->
            val entries = remember(matrix, state.language) { gridEntries(matrix, state.language) }
            SkillGrid(entries, columns = matrix.machines.size + 1, onEvent = onEvent)
            val selected = entries.filterIsInstance<GridEntry.Cell>().firstOrNull {
                it.workerId == state.selectedWorkerId && it.cell.machineId == state.selectedMachineId
            }
            if (selected != null) {
                CellSheet(selected, onDismiss = { onEvent(SkillsEvent.DismissCell) }, onOpenLearning = {
                    onEvent(SkillsEvent.DismissCell)
                    onOpenLearning(selected.workerId)
                })
            }
        }
    }
}

private fun gridEntries(matrix: SkillMatrix, language: String): List<GridEntry> {
    val names = matrix.machines.associate { it.id to pick(it.name, language) }
    return buildList {
        add(GridEntry.Corner)
        matrix.machines.forEach { add(GridEntry.MachineHead(it.id, names.getValue(it.id))) }
        matrix.rows.forEach { row ->
            add(GridEntry.WorkerHead(row.workerId, row.workerName))
            row.cells.forEach { add(GridEntry.Cell(row.workerId, row.workerName, names[it.machineId].orEmpty(), it)) }
        }
    }
}

@Composable
private fun SkillGrid(entries: List<GridEntry>, columns: Int, onEvent: (SkillsEvent) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(entries, key = { it.key }, contentType = { it::class }) { entry ->
            when (entry) {
                GridEntry.Corner -> Spacer(Modifier.heightIn(min = 1.dp))
                is GridEntry.MachineHead -> HeadText(entry.name)
                is GridEntry.WorkerHead -> HeadText(entry.name.substringBefore(' '))
                is GridEntry.Cell -> SkillCellButton(entry) { onEvent(SkillsEvent.SelectCell(entry.workerId, entry.cell.machineId)) }
            }
        }
        item(key = "legend", span = { GridItemSpan(maxLineSpan) }, contentType = "legend") {
            Legend()
        }
    }
}

@Composable
private fun HeadText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 4.dp),
    )
}

@Composable
private fun statusOf(status: SkillStatus): Pair<WakattaStatus, Int> = when (status) {
    SkillStatus.VALID -> WakattaStatus.GREEN to R.string.insights_skill_valid
    SkillStatus.EXPIRING -> WakattaStatus.ORANGE to R.string.insights_skill_expiring
    SkillStatus.EXPIRED -> WakattaStatus.RED to R.string.insights_skill_expired
    SkillStatus.NONE, SkillStatus.UNKNOWN -> WakattaStatus.RED to R.string.insights_skill_missing
}

@Composable
private fun SkillCellButton(entry: GridEntry.Cell, onClick: () -> Unit) {
    val (chip, labelRes) = statusOf(entry.cell.status)
    val color = StatusColors.forStatus(chip)
    val label = stringResource(labelRes)
    val text = when (entry.cell.status) {
        SkillStatus.VALID -> entry.cell.signedAtMillis?.let { shortDate(it) } ?: label
        SkillStatus.EXPIRING -> stringResource(R.string.insights_skill_days_left, entry.cell.daysLeft ?: 0)
        SkillStatus.EXPIRED -> label
        SkillStatus.NONE, SkillStatus.UNKNOWN -> stringResource(R.string.insights_skill_none)
    }
    val description = stringResource(R.string.insights_skill_cell_cd, entry.workerName, entry.machineName, label)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(color.containerColor, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = when (entry.cell.status) {
                SkillStatus.VALID -> Icons.Filled.Check
                SkillStatus.EXPIRING -> Icons.Filled.HourglassTop
                else -> Icons.Filled.Lock
            },
            contentDescription = null,
            tint = color.onContainerColor,
        )
        Text(text, fontSize = 11.sp, color = color.onContainerColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Legend() {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.insights_skill_valid))
            StatusChip(WakattaStatus.ORANGE, label = stringResource(R.string.insights_skill_expiring_legend))
            StatusChip(WakattaStatus.RED, label = stringResource(R.string.insights_skill_missing))
        }
        Text(
            stringResource(R.string.insights_skills_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CellSheet(entry: GridEntry.Cell, onDismiss: () -> Unit, onOpenLearning: () -> Unit) {
    val (chip, labelRes) = statusOf(entry.cell.status)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(entry.workerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(entry.machineName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(chip, label = stringResource(labelRes))
            }
            val none = stringResource(R.string.insights_skill_none)
            DetailRow(R.string.insights_skill_signed_on, entry.cell.signedAtMillis?.let { longDate(it) } ?: none)
            DetailRow(R.string.insights_skill_version, entry.cell.version ?: "-")
            DetailRow(R.string.insights_skill_expires, entry.cell.expiresAtMillis?.let { longDate(it) } ?: "-")
            OutlinedButton(onClick = onOpenLearning, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.insights_skill_view_learning))
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: Int, value: String) {
    Column {
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun shortDate(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    return remember(millis, locale) {
        java.text.SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "Md"), locale).format(Date(millis))
    }
}

@Composable
private fun longDate(millis: Long): String {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    return remember(millis, locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(millis)) }
}
