package com.techtsunagu.wakatta.feature.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.course.domain.DRILLS_NEEDED
import kotlinx.coroutines.delay

private const val SLING_MAX_ANGLE = 120
private const val SLING_STEP = 5
private const val TICK_MS = 1000L

@Composable
internal fun PlayDoor(state: CourseUiState, course: CourseView, onEvent: (CourseEvent) -> Unit) {
    val drill = state.drill
    if (drill != null) {
        DrillPanel(state, drill) { onEvent(CourseEvent.DrillInput(it)) }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "count", contentType = "count") {
            Text(
                stringResource(R.string.course_drills_passed, state.progress.drills.size, course.drills.size, DRILLS_NEEDED),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        items(course.drills, key = { it.id }, contentType = { "drill" }) { d ->
            WakattaCard(modifier = Modifier.fillMaxWidth(), onClick = { onEvent(CourseEvent.OpenDrill(d.id)) }) {
                Row(Modifier.padding(16.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    LineText(d.name, state.furigana, state.hasTranslation && state.translation, Modifier.weight(1f))
                    if (d.id in state.progress.drills) StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.course_drill_pass))
                }
            }
        }
    }
}

@Composable
private fun DrillPanel(state: CourseUiState, drill: DrillView, send: (DrillEvent) -> Unit) {
    val showTr = state.hasTranslation && state.translation
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LineText(drill.name, state.furigana, showTr, big = true)
        LineText(drill.goal, state.furigana, showTr)
        val d = state.drillState
        when (drill.kind) {
            DrillKind.SPOT -> SpotDrill(state, drill, d, send)
            DrillKind.SLING -> SlingDrill(drill, d, send)
            DrillKind.REFLEX -> ReflexDrill(d, send)
            DrillKind.RING -> RingDrill(drill, d, send)
            DrillKind.SEQ -> SeqDrill(state, drill, d, send)
        }
    }
}

@Composable
private fun Result(passed: Boolean, retry: DrillEvent, send: (DrillEvent) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusChip(
            if (passed) WakattaStatus.GREEN else WakattaStatus.RED,
            label = stringResource(if (passed) R.string.course_drill_pass else R.string.course_drill_fail),
        )
        OutlinedButton(onClick = { send(retry) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.course_retry_drill)) }
    }
}

@Composable
private fun SpotDrill(state: CourseUiState, drill: DrillView, d: DrillState, send: (DrillEvent) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(d.phase, d.secsLeft) {
        if (d.phase == DrillPhase.RUN) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(TICK_MS)
                send(DrillEvent.SpotTick)
            }
        }
    }
    if (d.phase == DrillPhase.IDLE) {
        Button(onClick = { send(DrillEvent.Start) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.course_start)) }
        return
    }
    Text(
        stringResource(R.string.course_time_left, d.secsLeft) + " · " + stringResource(R.string.course_spot_found, d.goodTaps.size),
        style = MaterialTheme.typography.titleSmall,
    )
    drill.labels.forEachIndexed { i, label ->
        val good = i in d.goodTaps
        val miss = i in d.missTaps
        val container = when {
            good -> StatusColors.GreenContainer
            miss -> StatusColors.RedContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        }
        WakattaCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = container, contentColor = MaterialTheme.colorScheme.onSurface),
            onClick = if (d.phase == DrillPhase.RUN && !good && !miss) ({ send(DrillEvent.SpotTap(i)) }) else null,
        ) { LineText(label, state.furigana, state.hasTranslation && state.translation, Modifier.padding(12.dp)) }
    }
    if (d.phase == DrillPhase.DONE) Result(d.passed, DrillEvent.Start, send)
}

@Composable
private fun SlingDrill(drill: DrillView, d: DrillState, send: (DrillEvent) -> Unit) {
    val over = d.tensionKg > drill.wllKg
    Text(stringResource(R.string.course_sling_angle, d.angle), style = MaterialTheme.typography.titleSmall)
    Slider(
        value = d.angle.toFloat(),
        onValueChange = { send(DrillEvent.SlingAngle((it / SLING_STEP).toInt() * SLING_STEP)) },
        valueRange = 0f..SLING_MAX_ANGLE.toFloat(),
        steps = SLING_MAX_ANGLE / SLING_STEP - 1,
        modifier = Modifier.heightIn(min = 48.dp),
    )
    Text(stringResource(R.string.course_sling_tension, d.tensionKg) + " · " + stringResource(R.string.course_sling_limit, drill.wllKg))
    LinearProgressIndicator(
        progress = { (d.tensionKg.toFloat() / drill.loadKg).coerceIn(0f, 1f) },
        color = if (over) StatusColors.Red else StatusColors.Green,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(onClick = { send(DrillEvent.Lift) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.course_sling_lift)) }
    if (d.lifted) {
        Text(
            stringResource(if (d.passed) R.string.course_sling_ok else R.string.course_sling_bad),
            color = if (d.passed) StatusColors.Green else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReflexDrill(d: DrillState, send: (DrillEvent) -> Unit) {
    LaunchedEffect(d.reflex, d.waitMs) {
        if (d.reflex == ReflexPhase.WAIT) {
            delay(d.waitMs)
            send(DrillEvent.ReflexGo)
        }
    }
    Text(stringResource(R.string.course_reflex_idle))
    val waiting = d.reflex == ReflexPhase.WAIT || d.reflex == ReflexPhase.GO
    if (waiting) {
        Button(
            onClick = { send(DrillEvent.ReflexHit) },
            colors = ButtonDefaults.buttonColors(containerColor = StatusColors.Red),
            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(if (d.reflex == ReflexPhase.GO) R.string.course_reflex_go else R.string.course_reflex_wait), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.course_estop), style = MaterialTheme.typography.labelMedium)
            }
        }
    } else {
        Button(onClick = { send(DrillEvent.Start) }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(if (d.reflex == ReflexPhase.IDLE) R.string.course_start else R.string.course_retry_drill))
        }
    }
    if (d.reflex == ReflexPhase.EARLY) Text(stringResource(R.string.course_reflex_early), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
    if (d.reflex == ReflexPhase.DONE) {
        Text(
            stringResource(R.string.course_reflex_result, d.reactionMs, REFLEX_LIMIT_MS) + " · " +
                stringResource(if (d.passed) R.string.course_drill_pass else R.string.course_drill_fail),
            color = if (d.passed) StatusColors.Green else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RingDrill(drill: DrillView, d: DrillState, send: (DrillEvent) -> Unit) {
    Text(stringResource(R.string.course_sound_note), style = MaterialTheme.typography.bodySmall)
    d.order.forEachIndexed { n, wheel ->
        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { send(DrillEvent.RingTap(wheel)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.course_ring_tap, n + 1))
                }
                Toggle(wheel in d.saidSound, R.string.course_ring_ok) { send(DrillEvent.RingSay(wheel, true)) }
                Toggle(wheel in d.saidCracked, R.string.course_ring_reject) { send(DrillEvent.RingSay(wheel, false)) }
            }
        }
    }
    Button(
        onClick = { send(DrillEvent.RingCheck) },
        enabled = d.saidSound.size + d.saidCracked.size == drill.wheels,
        modifier = Modifier.heightIn(min = 48.dp),
    ) { Text(stringResource(R.string.course_check)) }
    if (d.checked) Result(d.passed, DrillEvent.Restart, send)
}

@Composable
private fun SeqDrill(state: CourseUiState, drill: DrillView, d: DrillState, send: (DrillEvent) -> Unit) {
    val finished = d.picked.size == drill.labels.size
    Text(stringResource(R.string.course_seq_hint), style = MaterialTheme.typography.bodySmall)
    d.order.forEach { step ->
        val at = d.picked.indexOf(step)
        WakattaCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (at >= 0) StatusColors.GreenContainer else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            onClick = if (at < 0 && !finished) ({ send(DrillEvent.SeqTap(step)) }) else null,
        ) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (at >= 0) "${at + 1}" else "", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                LineText(drill.labels[step], state.furigana, state.hasTranslation && state.translation)
            }
        }
    }
    Text(stringResource(R.string.course_mistakes, d.mistakes), style = MaterialTheme.typography.bodyMedium)
    if (finished) Result(d.passed, DrillEvent.Restart, send)
}
