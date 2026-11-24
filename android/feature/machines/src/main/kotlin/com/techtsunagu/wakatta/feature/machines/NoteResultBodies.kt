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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.machines.ui.StepIcon

@Composable
internal fun NotePassBody(
    state: NoteUiState,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(StatusColors.GreenContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StatusColors.Green,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.machines_note_check_passed),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = StatusColors.Green
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.machines_note_unlocking_machine, state.machineName),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(text = stringResource(R.string.machines_panel_screen_title))
        }
    }
}

@Composable
internal fun NoteFailBody(
    state: NoteUiState,
    onEvent: (NoteEvent) -> Unit,
) {
    val totalQ = state.currentVersion?.questions?.size ?: 0
    val correctQ = totalQ - state.wrongQuestionIndices.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "fail_header") {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(StatusColors.RedContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusColors.Red,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.machines_note_check_failed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = StatusColors.Red
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.machines_note_failed_score, correctQ, totalQ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (state.wrongSteps.isNotEmpty()) {
            item(key = "steps_header") {
                Text(
                    text = stringResource(R.string.machines_note_read_steps_again),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(
                items = state.wrongSteps,
                key = { it.index },
                contentType = { "missed_step" }
            ) { step ->
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        StepIcon(iconKey = step.icon)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (state.userLanguage) {
                                "bn" -> step.textBn
                                "en" -> step.textEn
                                else -> step.textJa
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item(key = "actions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEvent(NoteEvent.ReturnToRead) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(text = stringResource(R.string.machines_note_reread_btn))
                }

                Button(
                    onClick = { onEvent(NoteEvent.RetryCheck) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.machines_note_retry_btn))
                }
            }
        }
    }
}

@Composable
internal fun NoteConflictBody(
    onEvent: (NoteEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(StatusColors.OrangeContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusColors.Orange,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.machines_note_conflict_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = StatusColors.Orange,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.machines_note_conflict_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onEvent(NoteEvent.ReloadAfterConflict) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = stringResource(R.string.machines_note_conflict_btn),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
