package com.techtsunagu.wakatta.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.HeatCell
import com.techtsunagu.wakatta.feature.insights.domain.TermStat

@Composable
internal fun VocabCard(terms: List<TermStat>, pinningTerm: String?, pinError: AppError?, onPin: (String) -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (pinError != null) {
                Text(stringResource(pinError.insightsMessageRes()), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (terms.isEmpty()) Text(stringResource(R.string.insights_vocab_none), style = MaterialTheme.typography.bodyMedium)
            terms.forEachIndexed { i, t -> VocabRow(i + 1, t, pinningTerm == null, onPin) }
        }
    }
}

@Composable
private fun VocabRow(rank: Int, stat: TermStat, canPin: Boolean, onPin: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("$rank", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f)) {
            Text(stat.term, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (stat.termEn.isNotEmpty()) {
                Text(stat.termEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.insights_vocab_people_times, stat.workerCount, stat.count),
                    style = MaterialTheme.typography.bodySmall,
                )
                TrendMark(stat.trend)
            }
        }
        if (stat.inGlossary) {
            StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.insights_vocab_in_glossary))
        } else {
            FilledTonalButton(onClick = { onPin(stat.term) }, enabled = canPin, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.insights_vocab_add))
            }
        }
    }
}

/** More recent confusions than before is bad (red), fewer is good (green). */
@Composable
private fun TrendMark(trend: Int) {
    val color = when {
        trend > 0 -> StatusColors.Red
        trend < 0 -> StatusColors.Green
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        when {
            trend > 0 -> Icon(Icons.AutoMirrored.Filled.TrendingUp, stringResource(R.string.insights_trend_worse), Modifier.size(16.dp), color)
            trend < 0 -> Icon(Icons.AutoMirrored.Filled.TrendingDown, stringResource(R.string.insights_trend_better), Modifier.size(16.dp), color)
        }
        Text(if (trend > 0) "+$trend" else if (trend < 0) "$trend" else "±0", style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
internal fun HeatmapCard(
    data: AnalyticsData,
    selectedTerm: String?,
    selectedWorkerId: String?,
    onSelect: (String, String) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.width(TERM_WIDTH))
                data.heatWorkers.forEach {
                    Text(
                        it.name.substringBefore(' '),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            data.heat.forEach { row ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(row.term, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(TERM_WIDTH))
                    data.heatWorkers.forEach { w ->
                        val cell = row.cells.firstOrNull { it.workerId == w.id } ?: HeatCell(w.id, 0, emptyList())
                        val cd = stringResource(R.string.insights_heat_cell_cd, row.term, w.name.substringBefore(' '), cell.count)
                        HeatSquare(cell.count, row.term == selectedTerm && w.id == selectedWorkerId, cd, Modifier.weight(1f)) {
                            onSelect(row.term, w.id)
                        }
                    }
                }
            }
            HeatLegend()
            HeatDetail(data, selectedTerm, selectedWorkerId)
        }
    }
}

private val TERM_WIDTH = 88.dp

@Composable
private fun HeatSquare(count: Int, selected: Boolean, description: String, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier
            .heightIn(min = 48.dp)
            .background(heatColor(count), shape)
            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (count > 0) Text("$count", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** Red intensity grows with the number of Confusions (red = Confusion). */
@Composable
private fun heatColor(count: Int) = when (count.coerceAtMost(3)) {
    0 -> MaterialTheme.colorScheme.surfaceVariant
    1 -> StatusColors.Red.copy(alpha = 0.25f)
    2 -> StatusColors.Red.copy(alpha = 0.5f)
    else -> StatusColors.Red.copy(alpha = 0.8f)
}

@Composable
private fun HeatLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(0, 1, 2, 3).forEach { n ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(14.dp).background(heatColor(n), RoundedCornerShape(3.dp)))
                Text(if (n == 3) "3+" else "$n", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun HeatDetail(data: AnalyticsData, term: String?, workerId: String?) {
    val cell = data.heat.firstOrNull { it.term == term }?.cells?.firstOrNull { it.workerId == workerId }
    if (term == null || cell == null) {
        Text(stringResource(R.string.insights_heat_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val name = data.heatWorkers.firstOrNull { it.id == workerId }?.name.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$name · $term · ${cell.count}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (cell.agesMinutes.isEmpty()) Text(stringResource(R.string.insights_heat_none), style = MaterialTheme.typography.bodyMedium)
        cell.agesMinutes.forEach {
            Text(
                stringResource(R.string.insights_heat_source, term, agoText(it)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
