package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.machines.ui.StepIcon

@Composable
internal fun NoteReadBody(
    state: NoteUiState,
    onEvent: (NoteEvent) -> Unit,
) {
    val version = state.currentVersion ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "header", contentType = "header") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = version.version,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.machines_note_safe_use_note),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.machines_note_updated_by, version.updatedAt, version.by),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!state.isForeman && state.isSignedOff) {
            item(key = "signed_off_banner", contentType = "signed_banner") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusColors.GreenContainer,
                    contentColor = StatusColors.OnGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.machines_note_signed_off_banner),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(
            items = version.steps,
            key = { it.index },
            contentType = { "step_item" }
        ) { step ->
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        StepIcon(iconKey = step.icon)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (state.isForeman) {
                                Text(
                                    text = step.textJa,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "EN: ${step.textEn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = stringResource(R.string.machines_note_back_translation),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = step.backJa,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                val text = when (state.userLanguage) {
                                    "bn" -> step.textBn
                                    "en" -> step.textEn
                                    else -> step.textJa
                                }
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!state.isForeman) {
                            IconButton(onClick = { onEvent(NoteEvent.SpeakText(step.textEn)) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.machines_cd_speaker),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.isForeman && version.questions.isNotEmpty()) {
            item(key = "foreman_questions_preview", contentType = "q_preview") {
                Text(
                    text = stringResource(R.string.machines_note_check_preview_title, version.questions.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        version.questions.forEachIndexed { i, q ->
                            Text(
                                text = "Q${i + 1}: ${q.qJa}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.machines_note_correct_answer, q.optionsJa.getOrElse(q.correctIndex ?: -1) { "" }),
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusColors.Green
                            )
                            if (i < version.questions.size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }

        if (!state.isForeman && version.questions.isNotEmpty()) {
            item(key = "start_check_action", contentType = "action_btn") {
                Button(
                    onClick = { onEvent(NoteEvent.StartCheck) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.machines_note_start_check, version.questions.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
