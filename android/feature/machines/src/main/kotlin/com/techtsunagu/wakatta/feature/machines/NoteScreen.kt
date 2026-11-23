package com.techtsunagu.wakatta.feature.machines

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import kotlinx.coroutines.delay

@Composable
fun NoteScreen(
    machineId: String,
    onBackClick: () -> Unit = {},
    viewModel: NoteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(machineId) {
        viewModel.onEvent(NoteEvent.Init(machineId))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NoteEffect.CheckPassed -> {
                    delay(1500L)
                    onBackClick()
                }
                is NoteEffect.SubmitFailed ->
                    Toast.makeText(context, R.string.machines_note_submit_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    NoteContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@Composable
fun NoteContent(
    state: NoteUiState,
    onEvent: (NoteEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = if (state.machineName.isNotEmpty()) {
                    "${stringResource(R.string.machines_note_screen_title)} · ${state.machineName}"
                } else {
                    stringResource(R.string.machines_note_screen_title)
                },
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
                        onRetry = { onEvent(NoteEvent.Retry) },
                        retryLabel = stringResource(R.string.machines_retry)
                    )
                }
                state.currentVersion == null -> {
                    EmptyState(
                        title = stringResource(R.string.machines_note_screen_title),
                        message = stringResource(R.string.machines_empty),
                    )
                }
                state.phase == NotePhase.READ -> {
                    NoteReadBody(state = state, onEvent = onEvent)
                }
                state.phase == NotePhase.QUIZ -> {
                    NoteQuizBody(state = state, onEvent = onEvent)
                }
                state.phase == NotePhase.PASS -> {
                    NotePassBody(state = state, onBackClick = onBackClick)
                }
                state.phase == NotePhase.FAIL -> {
                    NoteFailBody(state = state, onEvent = onEvent)
                }
                state.phase == NotePhase.CONFLICT -> {
                    NoteConflictBody(onEvent = onEvent)
                }
            }
        }
    }
}
