package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.StatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OverrideSheet(
    state: PanelUiState,
    onDismiss: () -> Unit,
    onWorkerSelected: (String?) -> Unit,
    onMinutesChanged: (Int) -> Unit,
    onReasonChanged: (String) -> Unit,
    onReviewClicked: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.machines_panel_override_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.machines_panel_override_duration_label),
                style = MaterialTheme.typography.labelLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 120, 240, 480).forEach { mins ->
                    val selected = state.overrideMinutes == mins
                    OutlinedButton(
                        onClick = { onMinutesChanged(mins) },
                        colors = if (selected) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        }
                    ) {
                        Text(text = stringResource(R.string.machines_panel_mins_format, mins))
                    }
                }
            }

            Text(
                text = stringResource(R.string.machines_panel_override_reason_label),
                style = MaterialTheme.typography.labelLarge
            )

            OutlinedTextField(
                value = state.overrideReason,
                onValueChange = onReasonChanged,
                placeholder = { Text(text = stringResource(R.string.machines_panel_override_reason_hint)) },
                modifier = Modifier.fillMaxWidth(),
                isError = state.overrideReasonErrorRes != null,
                supportingText = state.overrideReasonErrorRes?.let { res -> { Text(text = stringResource(res)) } },
                minLines = 2
            )

            Button(
                onClick = onReviewClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.machines_panel_override_review),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun ConfirmOverrideDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.machines_panel_override_confirm_title)) },
        text = { Text(text = stringResource(R.string.machines_panel_override_confirm_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusColors.Orange,
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(R.string.machines_panel_override_confirm_btn))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.machines_panel_cancel))
            }
        }
    )
}
