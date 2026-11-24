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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.PanelState

@Composable
internal fun PanelBody(
    state: PanelUiState,
    onEvent: (PanelEvent) -> Unit,
    onOpenNoteClick: (String) -> Unit,
    onOpenNearMissClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "hero_banner", contentType = "panel_hero") {
            PanelHeroSection(state = state)
        }

        if (state.state == PanelState.OVERRIDE && state.activeOverride != null) {
            item(key = "override_banner", contentType = "override_info") {
                ActiveOverrideBanner(
                    override = state.activeOverride,
                    isForeman = state.isForeman,
                    onEndOverride = { onEvent(PanelEvent.EndOverride) }
                )
            }
        }

        item(key = "presence_info", contentType = "presence") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (state.activeWorkerName != null) StatusColors.Green else StatusColors.Grey,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.activeWorkerName?.let {
                            stringResource(R.string.machines_panel_presence_worker, it)
                        } ?: stringResource(R.string.machines_panel_presence),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (state.isForeman) {
            item(key = "foreman_worker_list_header", contentType = "header") {
                Text(
                    text = stringResource(R.string.machines_panel_per_worker_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(
                items = state.perWorkerStatuses,
                key = { it.workerId },
                contentType = { "worker_status_row" }
            ) { worker ->
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(name = worker.displayName, size = 36.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = worker.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (worker.hasSignOff) {
                                    stringResource(R.string.machines_panel_signed_off_on, worker.signOffVersion ?: state.noteVersion)
                                } else {
                                    stringResource(R.string.machines_panel_no_signoff_yet, state.noteVersion)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (!worker.isReady) {
                            OutlinedButton(
                                onClick = { onEvent(PanelEvent.RemindWorker(worker.workerId)) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.machines_panel_remind),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            item(key = "override_button", contentType = "override_btn") {
                Button(
                    onClick = { onEvent(PanelEvent.OpenOverrideSheet) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusColors.OrangeContainer,
                        contentColor = StatusColors.OnOrangeContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.machines_panel_override_btn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item(key = "note_link", contentType = "note_btn") {
                Button(
                    onClick = {
                        onEvent(PanelEvent.OpenNote)
                        onOpenNoteClick(state.machineId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.machines_panel_view_note_and_questions, state.noteVersion),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Worker actions (none while the state is unknown: the hero text says to ask the foreman)
            if (state.state != PanelState.UNKNOWN) item(key = "worker_actions", contentType = "worker_panel_actions") {
                if (state.state == PanelState.READY || state.isUserSignedOff || state.state == PanelState.OVERRIDE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onEvent(PanelEvent.OpenNote)
                                onOpenNoteClick(state.machineId)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = stringResource(R.string.machines_panel_read_note))
                        }
                        OutlinedButton(
                            onClick = {
                                onEvent(PanelEvent.OpenNearMiss)
                                onOpenNearMissClick(state.machineId)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = stringResource(R.string.machines_panel_near_miss))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.machines_panel_locked_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onEvent(PanelEvent.OpenNote)
                                onOpenNoteClick(state.machineId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.machines_take_check_to_unlock),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
