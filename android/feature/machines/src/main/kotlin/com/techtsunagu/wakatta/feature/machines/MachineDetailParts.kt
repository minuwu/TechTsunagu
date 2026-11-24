package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.feature.machines.ui.MachineStatusBadge

@Composable
internal fun WorkerStatusBanner(
    state: MachineUiState,
    onOpenNoteClick: (String) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (state.isReady) StatusColors.GreenContainer else StatusColors.RedContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isReady) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (state.isReady) StatusColors.Green else StatusColors.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = state.nameTranslated,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    MachineStatusBadge(status = state.skillStatus, isReady = state.isReady)
                }
            }

            if (!state.isReady) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onOpenNoteClick(state.machineId) },
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
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun AssignOperatorDialog(
    state: MachineUiState,
    onDismiss: () -> Unit,
    onAssign: (String?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.machines_shift_assignment)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.machines_change_operator),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.availableWorkers.forEach { worker ->
                    OutlinedButton(
                        onClick = { onAssign(worker.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = worker.displayName)
                    }
                }
                TextButton(
                    onClick = { onAssign(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.machines_clear_operator),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.machines_panel_cancel))
            }
        }
    )
}
