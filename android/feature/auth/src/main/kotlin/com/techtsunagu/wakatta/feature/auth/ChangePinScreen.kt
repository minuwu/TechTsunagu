package com.techtsunagu.wakatta.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar

@Composable
fun ChangePinScreen(
    onPinChanged: () -> Unit = {},
    viewModel: ChangePinViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ChangePinEffect.NavigateBack -> onPinChanged()
            }
        }
    }
    ChangePinContent(
        state = state,
        onEvent = viewModel::onEvent,
        onPinChanged = onPinChanged,
    )
}

@Composable
fun ChangePinContent(
    state: ChangePinUiState,
    onEvent: (ChangePinEvent) -> Unit,
    modifier: Modifier = Modifier,
    onPinChanged: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.auth_changepin_title),
                onBackClick = onPinChanged,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.isLoading -> {
                    LoadingState(message = stringResource(R.string.auth_loading))
                }
                state.error != null -> {
                    EmptyState(
                        title = stringResource(R.string.auth_error_title),
                        message = authErrorMessage(state.error, state.errorArg),
                        actionLabel = stringResource(R.string.auth_retry),
                        onActionClick = { onEvent(ChangePinEvent.Retry) },
                    )
                }
                else -> {
                    OutlinedTextField(
                        value = state.currentPin,
                        onValueChange = { onEvent(ChangePinEvent.OnCurrentPinChanged(it)) },
                        label = { Text(stringResource(R.string.auth_current_pin_label)) },
                        placeholder = { Text(stringResource(R.string.auth_pin_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.newPin,
                        onValueChange = { onEvent(ChangePinEvent.OnNewPinChanged(it)) },
                        label = { Text(stringResource(R.string.auth_new_pin_label)) },
                        placeholder = { Text(stringResource(R.string.auth_pin_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.newPin.isNotEmpty() && !state.isNewPinValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.confirmPin,
                        onValueChange = { onEvent(ChangePinEvent.OnConfirmPinChanged(it)) },
                        label = { Text(stringResource(R.string.auth_confirm_pin_label)) },
                        placeholder = { Text(stringResource(R.string.auth_pin_hint)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.confirmPin.isNotEmpty() && !state.doPinsMatch,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onEvent(ChangePinEvent.SubmitChangePin) },
                        enabled = state.currentPin.isNotEmpty() && state.isNewPinValid && state.doPinsMatch,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.auth_change_pin_submit))
                    }
                }
            }
        }
    }
}
