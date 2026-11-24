package com.techtsunagu.wakatta.feature.course

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.course.domain.Door

@StringRes
private fun categoryRes(category: String): Int = when (category) {
    "core" -> R.string.course_category_core
    "trade" -> R.string.course_category_trade
    else -> R.string.course_category_language
}

@Composable
internal fun CourseList(state: CourseUiState, onEvent: (CourseEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "intro", contentType = "intro") {
            Text(stringResource(R.string.course_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.courses.groupBy { it.category }.forEach { (category, list) ->
            item(key = "h:$category", contentType = "header") {
                Text(
                    stringResource(categoryRes(category)),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp).semantics { heading() },
                )
            }
            items(list, key = { it.id }, contentType = { "course" }) { CourseCard(it, state, onEvent) }
        }
    }
}

@Composable
private fun CourseCard(item: CourseItem, state: CourseUiState, onEvent: (CourseEvent) -> Unit) {
    val total = Door.entries.size
    WakattaCard(modifier = Modifier.fillMaxWidth(), onClick = { onEvent(CourseEvent.OpenCourse(item.id)) }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LineText(item.title, state.furigana, state.hasTranslation && state.translation)
            Text(
                stringResource(R.string.course_minutes, item.minutes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (item.doorsDone == total) {
                StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.course_passport_stamped))
            } else if (item.doorsDone > 0) {
                Text(stringResource(R.string.course_doors_done, item.doorsDone, total), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun DoorsHome(state: CourseUiState, onEvent: (CourseEvent) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(Door.entries, key = { it.name }, contentType = { "door" }) { door ->
            WakattaCard(modifier = Modifier.fillMaxWidth(), onClick = { onEvent(CourseEvent.OpenDoor(door)) }) {
                Row(Modifier.padding(16.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(door.nameRes()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(door.subRes()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.progress.isDone(door)) StatusChip(WakattaStatus.GREEN, label = stringResource(R.string.course_done))
                }
            }
        }
        item(key = "passport", contentType = "passport") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.course_passport_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(if (state.progress.passport) R.string.course_passport_earned else R.string.course_passport_locked),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = { onEvent(CourseEvent.StartOver) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.course_reset))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SaveErrorText() {
    Text(
        stringResource(R.string.course_error_save),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )
}
