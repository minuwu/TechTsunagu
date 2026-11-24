package com.techtsunagu.wakatta.feature.machines

import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar

@Composable
fun MachineScreen(
    machineId: String,
    onBackClick: () -> Unit = {},
    onOpenPanelClick: (String) -> Unit = {},
    onOpenNoteClick: (String) -> Unit = {},
    onAskClick: (String) -> Unit = {},
    viewModel: MachineViewModel = hiltViewModel(),
) {
    RefreshOnReturn { viewModel.onEvent(MachineEvent.Refresh) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(machineId) {
        viewModel.onEvent(MachineEvent.Init(machineId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MachineEffect.NavigateToPanel -> onOpenPanelClick(effect.machineId)
                is MachineEffect.NavigateToNote -> onOpenNoteClick(effect.machineId)
            }
        }
    }

    LaunchedEffect(state.qrPrintedToastRes) {
        state.qrPrintedToastRes?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(MachineEvent.DismissToast)
        }
    }

    MachineContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onOpenPanelClick = onOpenPanelClick,
        onOpenNoteClick = onOpenNoteClick,
        onAskClick = { onAskClick(machineId) },
    )
}

@Composable
fun MachineContent(
    state: MachineUiState,
    onEvent: (MachineEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOpenPanelClick: (String) -> Unit = {},
    onOpenNoteClick: (String) -> Unit = {},
    onAskClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Ask Wakatta about this Machine: its instruction ranks first (XREQ-016)
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAskClick) {
                Text(stringResource(R.string.machines_ask_wakatta))
            }
        },
        topBar = {
            WakattaTopBar(
                title = if (state.nameTranslated.isNotEmpty()) state.nameTranslated else stringResource(R.string.machines_machine_screen_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
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
                        onRetry = { onEvent(MachineEvent.Retry) },
                        retryLabel = stringResource(R.string.machines_retry),
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.machines_machine_screen_title),
                        message = stringResource(R.string.machines_empty),
                    )
                }
                else -> {
                    MachineDetailBody(
                        state = state,
                        onEvent = onEvent,
                        onOpenPanelClick = onOpenPanelClick,
                        onOpenNoteClick = onOpenNoteClick,
                    )
                }
            }
        }
    }

    if (state.showAssignDialog) {
        AssignOperatorDialog(
            state = state,
            onDismiss = { onEvent(MachineEvent.DismissAssignDialog) },
            onAssign = { wId -> onEvent(MachineEvent.AssignWorker(wId)) }
        )
    }
}
