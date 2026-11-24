package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar

@Composable
fun MachinesScreen(
    onMachineClick: (String) -> Unit = {},
    onTakeCheckClick: (String) -> Unit = onMachineClick,
    viewModel: MachinesViewModel = hiltViewModel(),
) {
    RefreshOnReturn { viewModel.onEvent(MachinesEvent.Refresh) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MachinesEffect.NavigateToMachine -> onMachineClick(effect.machineId)
                is MachinesEffect.NavigateToCheck -> onTakeCheckClick(effect.machineId)
            }
        }
    }

    MachinesContent(
        state = state,
        onEvent = viewModel::onEvent,
        onMachineClick = onMachineClick,
        onTakeCheckClick = onTakeCheckClick,
    )
}

@Composable
fun MachinesContent(
    state: MachinesUiState,
    onEvent: (MachinesEvent) -> Unit,
    modifier: Modifier = Modifier,
    onMachineClick: (String) -> Unit = {},
    onTakeCheckClick: (String) -> Unit = onMachineClick,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(title = stringResource(R.string.machines_screen_title))
        },
        floatingActionButton = {
            if (state.isForeman) {
                ExtendedFloatingActionButton(
                    onClick = { onEvent(MachinesEvent.OpenDialog(MachineDialog.Add)) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.machines_add)) },
                )
            }
        },
    ) { innerPadding ->
        MachineDialogs(state, onEvent)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    LoadingState(message = stringResource(R.string.machines_loading))
                }
                state.error != null -> {
                    ErrorState(
                        message = stringResource(state.error.machinesMessageRes()),
                        onRetry = { onEvent(MachinesEvent.Retry) },
                        retryLabel = stringResource(R.string.machines_retry),
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.machines_screen_title),
                        message = stringResource(R.string.machines_empty),
                    )
                }
                state.isForeman -> {
                    ForemanMachinesList(
                        machines = state.machines,
                        onMachineClick = { id ->
                            onEvent(MachinesEvent.MachineClicked(id))
                            onMachineClick(id)
                        },
                        onRename = { onEvent(MachinesEvent.OpenDialog(MachineDialog.Rename(it))) },
                        onArchive = { onEvent(MachinesEvent.OpenDialog(MachineDialog.Archive(it))) },
                    )
                }
                else -> {
                    WorkerMachinesList(
                        machines = state.machines,
                        onMachineClick = { id ->
                            onEvent(MachinesEvent.MachineClicked(id))
                            onMachineClick(id)
                        },
                        onTakeCheckClick = { id ->
                            onEvent(MachinesEvent.TakeCheckClicked(id))
                            onTakeCheckClick(id)
                        }
                    )
                }
            }
        }
    }
}
