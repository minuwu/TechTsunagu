package com.techtsunagu.wakatta.feature.course

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.stripFurigana

private const val VIEW_W = 100f
private const val VIEW_H = 60f
private const val MAX_GAP = 20

@Composable
internal fun InspectDoor(state: CourseUiState, course: CourseView, onEvent: (CourseEvent) -> Unit) {
    val q = state.inspect
    val item = course.inspect.getOrNull(q.index) ?: return
    val looked = item.spots.indices.count { "${item.id}#$it" in q.seen }
    val solved = item.id in q.right
    val showTr = state.hasTranslation && state.translation
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            course.inspect.forEachIndexed { n, x ->
                val label = (if (x.id in q.right) "✓ " else "") + stripFurigana(x.name.ja)
                androidx.compose.material3.FilterChip(
                    selected = n == q.index,
                    onClick = { onEvent(CourseEvent.InspectItemSelected(n)) },
                    label = { Text(label) },
                )
            }
        }
        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LineText(item.name, state.furigana, showTr, big = true)
                PartDrawing(item, q.gap, q.seen) { onEvent(CourseEvent.InspectSpotTapped(it)) }
                Text(stringResource(R.string.course_inspect_explode), style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = q.gap.toFloat(),
                    onValueChange = { onEvent(CourseEvent.InspectGap(it.toInt())) },
                    valueRange = 0f..MAX_GAP.toFloat(),
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                Text(stringResource(R.string.course_inspect_tap), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.course_inspect_checked, looked, item.spots.size), style = MaterialTheme.typography.bodyMedium)
                item.spots.forEachIndexed { i, s ->
                    if ("${item.id}#$i" in q.seen) LineText(s.check, state.furigana, showTr)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val enabled = looked == item.spots.size && !solved
            Button(onClick = { onEvent(CourseEvent.Judge(false)) }, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.course_inspect_pass))
            }
            OutlinedButton(onClick = { onEvent(CourseEvent.Judge(true)) }, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.course_inspect_reject))
            }
            if (solved) Text(stringResource(R.string.course_inspect_right), color = StatusColors.Green, fontWeight = FontWeight.Bold)
            if (q.wrong) Text(stringResource(R.string.course_inspect_wrong), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}

/** Two parts that the "exploded view" slider pulls apart, with tap dots on the points to check. */
@Composable
private fun PartDrawing(item: InspectView, gap: Int, seen: List<String>, onSpot: (Int) -> Unit) {
    val fill = MaterialTheme.colorScheme.surfaceVariant
    val line = MaterialTheme.colorScheme.onSurface
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(VIEW_W / VIEW_H)) {
        // The body drops and the cap rises with the slider; graphicsLayer moves the cached drawing without redrawing it.
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { translationY = size.height * gap / 4f / VIEW_H }
                .drawWithCache {
                    val u = size.width / VIEW_W
                    onDrawBehind {
                        drawRect(fill, Offset(16 * u, 20 * u), Size(68 * u, 22 * u))
                        drawRect(line, Offset(16 * u, 20 * u), Size(68 * u, 22 * u), style = Stroke(0.8f * u))
                    }
                },
        )
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { translationY = -size.height * gap / 2f / VIEW_H }
                .drawWithCache {
                    val u = size.width / VIEW_W
                    onDrawBehind {
                        drawRect(fill, Offset(40 * u, 8 * u), Size(20 * u, 12 * u))
                        drawRect(line, Offset(40 * u, 8 * u), Size(20 * u, 12 * u), style = Stroke(0.8f * u))
                    }
                },
        )
        if (item.showsDefect) {
            Box(
                Modifier.fillMaxSize().drawWithCache {
                    val u = size.width / VIEW_W
                    val crack = listOf(Offset(36 * u, 22 * u), Offset(46 * u, 30 * u), Offset(38 * u, 36 * u), Offset(48 * u, 42 * u))
                    onDrawBehind {
                        crack.zipWithNext { a, b -> drawLine(StatusColors.Red, a, b, 1.6f * u) }
                    }
                },
            )
        }
        item.spots.forEachIndexed { i, s ->
            val looked = "${item.id}#$i" in seen
            val description = stripFurigana(s.check.ja)
            Box(
                Modifier
                    .offset(x = maxWidth * s.x / 100f - 24.dp, y = maxHeight * s.y / VIEW_H - 24.dp)
                    .size(48.dp)
                    .clickable { onSpot(i) }
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(16.dp).background(if (looked) StatusColors.Green else MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}
