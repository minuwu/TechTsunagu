package com.techtsunagu.wakatta.feature.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.model.WakattaStatus
import kotlinx.coroutines.delay

private const val REEL_LINE_MS = 2600f
private const val SLOW_SPEED = 0.75f

@Composable
internal fun ReadDoor(state: CourseUiState, course: CourseView, onEvent: (CourseEvent) -> Unit) {
    val comic = state.readMode == ReadMode.COMIC
    val lines = if (comic) course.panels.map { it.text } else course.handbook
    val showTr = state.hasTranslation && state.translation
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "modes", contentType = "modes") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Toggle(comic, R.string.course_mode_comic) { onEvent(CourseEvent.SetReadMode(ReadMode.COMIC)) }
                Toggle(!comic, R.string.course_mode_handbook) { onEvent(CourseEvent.SetReadMode(ReadMode.HANDBOOK)) }
                Toggle(state.furigana, R.string.course_furigana) { onEvent(CourseEvent.ToggleFurigana) }
                if (state.hasTranslation) Toggle(state.translation, R.string.course_translation) { onEvent(CourseEvent.ToggleTranslation) }
            }
        }
        itemsIndexed(lines, key = { i, _ -> "${state.readMode}:$i" }, contentType = { _, _ -> "line" }) { i, line ->
            val foreman = comic && course.panels[i].foreman
            val colors = CardDefaults.cardColors(
                containerColor = if (foreman) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (foreman) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            )
            WakattaCard(modifier = Modifier.fillMaxWidth(), colors = colors) {
                Column(Modifier.padding(12.dp)) {
                    if (comic) {
                        Text(
                            stringResource(if (foreman) R.string.course_role_foreman else R.string.course_role_worker),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    LineText(line, state.furigana, showTr)
                }
            }
        }
        item(key = "finish", contentType = "finish") {
            Button(
                onClick = { onEvent(CourseEvent.FinishRead) },
                enabled = !state.progress.read,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(if (state.progress.read) R.string.course_done else R.string.course_finish_read)) }
        }
    }
}

@Composable
internal fun Toggle(on: Boolean, label: Int, onClick: () -> Unit) {
    FilterChip(selected = on, onClick = onClick, label = { Text(stringResource(label)) })
}

@Composable
internal fun WatchDoor(state: CourseUiState, course: CourseView, onEvent: (CourseEvent) -> Unit) {
    val reel = state.reel
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // The timer only runs while the screen is visible; the VM also pauses on ON_STOP.
    LaunchedEffect(reel.playing, reel.index, reel.slow) {
        if (reel.playing) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay((REEL_LINE_MS / (if (reel.slow) SLOW_SPEED else 1f)).toLong())
                onEvent(CourseEvent.ReelTick)
            }
        }
    }
    val line = course.reel.getOrNull(reel.index) ?: return
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.course_watch_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            LineText(line, state.furigana, state.hasTranslation && state.translation, Modifier.padding(16.dp).heightIn(min = 96.dp), big = true)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            course.reel.indices.forEach { i ->
                val on = i <= reel.index && (reel.playing || state.progress.watch)
                Box(
                    Modifier
                        .size(10.dp)
                        .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onEvent(CourseEvent.ReelToggle) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(if (reel.playing) R.string.course_pause else R.string.course_play))
            }
            OutlinedButton(onClick = { onEvent(CourseEvent.ReelReplay) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.course_replay))
            }
            Toggle(reel.slow, R.string.course_speed) { onEvent(CourseEvent.ReelSlow) }
        }
        if (state.progress.watch) StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.course_done))
    }
}
