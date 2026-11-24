package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private val CODE = Regex("^[a-z0-9-]{1,32}$")

/** The Foreman's add, rename and archive dialogs over the Machines list. */
@Composable
internal fun MachineDialogs(state: MachinesUiState, onEvent: (MachinesEvent) -> Unit) {
    val dialog = state.dialog ?: return
    val close = { onEvent(MachinesEvent.OpenDialog(null)) }
    val error = state.saveError?.let { stringResource(it.machinesMessageRes()) }
    when (dialog) {
        MachineDialog.Add -> NameDialog(
            title = stringResource(R.string.machines_add_title),
            withCode = true,
            initialJa = "",
            initialEn = "",
            saving = state.isSaving,
            error = error,
            onDismiss = close,
            onSave = { code, ja, en -> onEvent(MachinesEvent.Add(code, ja, en)) },
        )
        is MachineDialog.Rename -> NameDialog(
            title = stringResource(R.string.machines_rename_title),
            withCode = false,
            initialJa = dialog.machine.name["ja"].orEmpty(),
            initialEn = dialog.machine.name["en"].orEmpty(),
            saving = state.isSaving,
            error = error,
            onDismiss = close,
            onSave = { _, ja, en -> onEvent(MachinesEvent.Rename(dialog.machine.id, ja, en)) },
        )
        is MachineDialog.Archive -> AlertDialog(
            onDismissRequest = close,
            title = { Text(stringResource(R.string.machines_archive_title, dialog.machine.nameJa)) },
            text = { Text(error ?: stringResource(R.string.machines_archive_body)) },
            confirmButton = {
                TextButton(onClick = { onEvent(MachinesEvent.Archive(dialog.machine.id)) }, enabled = !state.isSaving) {
                    Text(stringResource(R.string.machines_archive), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = close) { Text(stringResource(R.string.machines_cancel)) } },
        )
    }
}

@Composable
private fun NameDialog(
    title: String,
    withCode: Boolean,
    initialJa: String,
    initialEn: String,
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (code: String, ja: String, en: String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    var ja by rememberSaveable { mutableStateOf(initialJa) }
    var en by rememberSaveable { mutableStateOf(initialEn) }
    val codeOk = !withCode || CODE.matches(code)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (withCode) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.lowercase().trim() },
                        label = { Text(stringResource(R.string.machines_field_code)) },
                        supportingText = { Text(stringResource(R.string.machines_field_code_hint)) },
                        isError = code.isNotEmpty() && !codeOk,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    value = ja,
                    onValueChange = { ja = it },
                    label = { Text(stringResource(R.string.machines_field_name_ja)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = en,
                    onValueChange = { en = it },
                    label = { Text(stringResource(R.string.machines_field_name_en)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(code, ja, en) }, enabled = !saving && codeOk && ja.isNotBlank()) {
                Text(stringResource(R.string.machines_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.machines_cancel)) } },
    )
}

/** The menu on a Foreman's machine card: rename or archive. */
@Composable
internal fun MachineMenu(onRename: () -> Unit, onArchive: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Default.MoreVert, stringResource(R.string.machines_more))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.machines_rename)) },
                onClick = {
                    open = false
                    onRename()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.machines_archive), color = MaterialTheme.colorScheme.error) },
                onClick = {
                    open = false
                    onArchive()
                },
            )
        }
    }
}
