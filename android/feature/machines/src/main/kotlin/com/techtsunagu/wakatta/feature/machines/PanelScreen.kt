package com.techtsunagu.wakatta.feature.machines

import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import kotlinx.coroutines.delay

private const val POLL_INTERVAL_MS = 2_000L

@Composable
fun PanelScreen(
    machineId: String,
    onBackClick: () -> Unit = {},
    onOpenNoteClick: (String) -> Unit = {},
    onOpenNearMissClick: (String) -> Unit = {},
    viewModel: PanelViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(machineId) {
        viewModel.onEvent(PanelEvent.Init(machineId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PanelEffect.NavigateToNote -> onOpenNoteClick(effect.machineId)
                is PanelEffect.NavigateToNearMiss -> onOpenNearMissClick(effect.machineId)
            }
        }
    }

    // Polls only while the screen is visible: one request at once on return, then every 2 seconds.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.onEvent(PanelEvent.PollTick)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            val text = message.arg?.let { context.getString(message.resId, it) } ?: context.getString(message.resId)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(PanelEvent.DismissToast)
        }
    }

    PanelContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onOpenNoteClick = onOpenNoteClick,
        onOpenNearMissClick = onOpenNearMissClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelContent(
    state: PanelUiState,
    onEvent: (PanelEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onOpenNoteClick: (String) -> Unit = {},
    onOpenNearMissClick: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.machines_panel_screen_title),
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
                        onRetry = { onEvent(PanelEvent.Retry) },
                        retryLabel = stringResource(R.string.machines_retry)
                    )
                }
                state.machineId.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.machines_panel_screen_title),
                        message = stringResource(R.string.machines_empty),
                    )
                }
                else -> {
                    PanelBody(
                        state = state,
                        onEvent = onEvent,
                        onOpenNoteClick = onOpenNoteClick,
                        onOpenNearMissClick = onOpenNearMissClick
                    )
                }
            }
        }
    }

    if (state.showOverrideSheet) {
        OverrideSheet(
            state = state,
            onDismiss = { onEvent(PanelEvent.DismissOverrideSheet) },
            onWorkerSelected = { onEvent(PanelEvent.OverrideWorkerSelected(it)) },
            onMinutesChanged = { onEvent(PanelEvent.OverrideMinutesChanged(it)) },
            onReasonChanged = { onEvent(PanelEvent.OverrideReasonChanged(it)) },
            onReviewClicked = { onEvent(PanelEvent.RequestOverrideConfirm) }
        )
    }

    if (state.showOverrideConfirmDialog) {
        ConfirmOverrideDialog(
            onConfirm = { onEvent(PanelEvent.ConfirmOverride) },
            onDismiss = { onEvent(PanelEvent.DismissOverrideConfirm) }
        )
    }
}
