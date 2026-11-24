package com.techtsunagu.wakatta.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar

@Composable
fun SignInScreen(
    onNavigateToChangePin: () -> Unit = {},
    onSignInSuccess: () -> Unit = {},
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SignInEffect.NavigateToHome -> onSignInSuccess()
                SignInEffect.NavigateToChangePin -> onNavigateToChangePin()
            }
        }
    }
    SignInContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun SignInContent(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(title = stringResource(R.string.auth_signin_title))
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
                        onActionClick = { onEvent(SignInEvent.Retry) },
                    )
                }
                else -> {
                    RoleChoice(role = state.role, onRoleChange = { onEvent(SignInEvent.OnRoleChanged(it)) })

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.shopCode,
                        onValueChange = { onEvent(SignInEvent.OnShopCodeChanged(it)) },
                        label = { Text(stringResource(R.string.auth_shop_code_label)) },
                        placeholder = { Text(stringResource(R.string.auth_shop_code_hint)) },
                        isError = state.shopCode.isNotEmpty() && !state.isShopCodeValid,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.isRosterLoading) {
                        LoadingState(message = stringResource(R.string.auth_loading))
                    } else if (state.isEmpty) {
                        EmptyState(
                            title = stringResource(R.string.auth_empty),
                            message = stringResource(R.string.auth_shop_code_hint),
                        )
                    } else if (state.roster.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.auth_select_worker),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                        ) {
                            items(
                                items = state.roster,
                                key = { it.id },
                                contentType = { "roster_item" },
                            ) { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onEvent(SignInEvent.OnUserSelected(user)) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (state.selectedUser?.id == user.id) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = state.selectedUser?.id == user.id,
                                            onClick = { onEvent(SignInEvent.OnUserSelected(user)) },
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(
                                                text = user.displayName,
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                            Text(
                                                text = roleLabel(user.role),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.pin,
                            onValueChange = { onEvent(SignInEvent.OnPinChanged(it)) },
                            label = { Text(stringResource(R.string.auth_pin_label)) },
                            placeholder = { Text(stringResource(R.string.auth_pin_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = state.pin.isNotEmpty() && !state.isPinValid,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onEvent(SignInEvent.SubmitSignIn) },
                            enabled = state.isShopCodeValid && state.selectedUser != null && state.isPinValid,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.auth_submit))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleChoice(role: Role, onRoleChange: (Role) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.auth_role_label),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = role == Role.WORKER,
                onClick = { onRoleChange(Role.WORKER) },
                label = { Text(stringResource(R.string.auth_role_worker)) },
            )
            FilterChip(
                selected = role == Role.FOREMAN,
                onClick = { onRoleChange(Role.FOREMAN) },
                label = { Text(stringResource(R.string.auth_role_foreman)) },
            )
        }
    }
}

@Composable
private fun roleLabel(wireRole: String): String = stringResource(
    if (wireRole == "foreman") R.string.auth_role_foreman else R.string.auth_role_worker,
)
