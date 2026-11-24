package com.techtsunagu.wakatta.feature.insights

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.WorkerBar
import kotlin.math.roundToInt

private val WEEK_LABELS = listOf(
    R.string.insights_week_3_ago, R.string.insights_week_2_ago, R.string.insights_week_last, R.string.insights_week_this,
)
private const val CHART_MIN = 40f
private const val CHART_MAX = 100f
private val GRID_LINES = listOf(50f, 70f, 90f)

@Composable
internal fun KpiRow(cleanAiShare: Double?, firstTryPassShare: Double?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Kpi(R.string.insights_kpi_clean_ai, percentText(cleanAiShare), Modifier.weight(1f))
        Kpi(R.string.insights_kpi_first_try, percentText(firstTryPassShare), Modifier.weight(1f))
    }
}

@Composable
private fun percentText(share: Double?): String =
    share?.let { stringResource(R.string.insights_kpi_percent, (it * 100).roundToInt()) } ?: "—"

@Composable
private fun Kpi(@StringRes label: Int, value: String, modifier: Modifier) {
    WakattaCard(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun TrendCard(trend: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    val line = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val now = trend.lastOrNull() ?: 0
    val chartCd = stringResource(R.string.insights_trend_cd, now)
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .semantics { contentDescription = chartCd }
                    .drawWithCache {
                        val stepX = if (trend.size > 1) size.width / (trend.size - 1) else 0f
                        fun y(v: Float) = size.height * (CHART_MAX - v.coerceIn(CHART_MIN, CHART_MAX)) / (CHART_MAX - CHART_MIN)
                        val points = trend.mapIndexed { i, v -> Offset(i * stepX, y(v.toFloat())) }
                        val path = Path().apply {
                            points.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
                        }
                        onDrawBehind {
                            GRID_LINES.forEach { drawLine(grid, Offset(0f, y(it)), Offset(size.width, y(it)), 1.dp.toPx()) }
                            drawPath(path, line, style = Stroke(width = 3.dp.toPx()))
                            points.forEachIndexed { i, p -> drawCircle(line, if (i == selected) 8.dp.toPx() else 5.dp.toPx(), p) }
                        }
                    },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val labels = WEEK_LABELS.takeLast(trend.size)
                trend.forEachIndexed { i, v ->
                    val on = i == selected
                    Column(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .background(if (on) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onSelect(i) }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(labels[i]), style = MaterialTheme.typography.labelSmall)
                        Text("$v%", style = MaterialTheme.typography.titleSmall, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
internal fun FunnelCard(data: AnalyticsData) {
    val rows = listOf(
        Triple(R.string.insights_funnel_sent, data.sent, MaterialTheme.colorScheme.primary),
        Triple(R.string.insights_funnel_understood, data.understood, StatusColors.Green),
        Triple(R.string.insights_funnel_followup, data.stats.flagged, StatusColors.Red),
        Triple(R.string.insights_funnel_explained, data.stats.explained, StatusColors.Orange),
        Triple(R.string.insights_funnel_reunderstood, data.stats.understood, StatusColors.Green),
    )
    val max = rows.maxOf { it.second }.coerceAtLeast(1)
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { (label, value, color) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(label), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(96.dp))
                    Box(Modifier.weight(1f).height(10.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp))) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((value.toFloat() / max).coerceIn(0.06f, 1f))
                                .background(color, RoundedCornerShape(5.dp)),
                        )
                    }
                    val displayValue = if (value >= 0) "$value" else "—"
                    Text(displayValue, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun BarsCard(bars: List<WorkerBar>, onOpenLearning: (String) -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 4.dp)) {
            bars.forEach { bar ->
                val total = (bar.green + bar.orange + bar.red).coerceAtLeast(1)
                val pct = 100 * bar.green / total
                val cd = stringResource(R.string.insights_bar_cd, bar.name, pct)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable { onOpenLearning(bar.workerId) }
                        .clearAndSetSemantics { contentDescription = cd }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(name = bar.name)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(bar.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Row(Modifier.fillMaxWidth().height(8.dp)) {
                            if (bar.green > 0) Box(Modifier.weight(bar.green.toFloat()).fillMaxHeight().background(StatusColors.Green))
                            if (bar.orange > 0) Box(Modifier.weight(bar.orange.toFloat()).fillMaxHeight().background(StatusColors.Orange))
                            if (bar.red > 0) Box(Modifier.weight(bar.red.toFloat()).fillMaxHeight().background(StatusColors.Red))
                        }
                    }
                    Text("$pct%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
