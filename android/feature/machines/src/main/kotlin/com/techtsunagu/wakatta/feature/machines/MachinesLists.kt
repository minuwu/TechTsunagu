package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.machines.ui.MachineStatusBadge

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ForemanMachinesList(
    machines: List<MachineItemUiState>,
    onMachineClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onRename: (MachineItemUiState) -> Unit = {},
    onArchive: (MachineItemUiState) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 88.dp), // room above the Add machine button
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = machines,
            key = { it.id },
            contentType = { "foreman_machine" }
        ) { machine ->
            WakattaCard(
                onClick = { onMachineClick(machine.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = machine.nameJa,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = machine.nameTranslated,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (machine.lockedWorkerCount == 0) {
                            Text(
                                text = stringResource(R.string.machines_all_signed_off, machine.workerStatuses.size),
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusColors.Green
                            )
                        } else {
                            Text(
                                text = stringResource(
                                    R.string.machines_signed_off_ratio,
                                    machine.workerStatuses.size - machine.lockedWorkerCount,
                                    machine.workerStatuses.size
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusColors.Orange
                            )
                        }
                        MachineMenu(onRename = { onRename(machine) }, onArchive = { onArchive(machine) })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        machine.workerStatuses.forEach { w ->
                            WorkerStatusChip(worker = w)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun WorkerStatusChip(worker: MachineWorkerChipState) {
    val bg = if (worker.isReady) StatusColors.GreenContainer else StatusColors.GreyContainer
    val fg = if (worker.isReady) StatusColors.OnGreenContainer else StatusColors.OnGreyContainer
    val icon = if (worker.isReady) Icons.Default.Check else Icons.Default.Lock

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                name = worker.displayName,
                size = 18.dp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = worker.displayName.split(" ").firstOrNull() ?: worker.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
internal fun WorkerMachinesList(
    machines: List<MachineItemUiState>,
    onMachineClick: (String) -> Unit,
    onTakeCheckClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = machines,
            key = { it.id },
            contentType = { "worker_machine" }
        ) { machine ->
            WakattaCard(
                onClick = if (machine.isReady) { { onMachineClick(machine.id) } } else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (machine.isReady) StatusColors.GreenContainer else StatusColors.RedContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (machine.isReady) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = if (machine.isReady) stringResource(R.string.machines_cd_ready) else stringResource(R.string.machines_cd_lock),
                                tint = if (machine.isReady) StatusColors.Green else StatusColors.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = machine.nameTranslated,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            MachineStatusBadge(
                                status = machine.status,
                                isReady = machine.isReady
                            )
                        }
                    }

                    if (!machine.isReady) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onTakeCheckClick(machine.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.machines_take_check_to_unlock),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
