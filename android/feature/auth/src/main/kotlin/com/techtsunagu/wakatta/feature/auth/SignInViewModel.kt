package com.techtsunagu.wakatta.feature.auth

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SignInUiState(
    val role: Role = Role.WORKER,
    val shopCode: String = "",
    val isShopCodeValid: Boolean = false,
    val roster: List<RosterUser> = emptyList(),
    val selectedUser: RosterUser? = null,
    val pin: String = "",
    val isPinValid: Boolean = false,
    val isLoading: Boolean = false,
    val isRosterLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: AuthErrorKind? = null,
    val errorArg: String = "",
    val isSuccess: Boolean = false,
)

sealed interface SignInEvent {
    data class OnRoleChanged(val role: Role) : SignInEvent
    data class OnShopCodeChanged(val code: String) : SignInEvent
    data class OnUserSelected(val user: RosterUser) : SignInEvent
    data class OnPinChanged(val pin: String) : SignInEvent
    data object SubmitSignIn : SignInEvent
    data object Retry : SignInEvent
}

sealed interface SignInEffect {
    data object NavigateToHome : SignInEffect
    data object NavigateToChangePin : SignInEffect
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
) : MviViewModel<SignInUiState, SignInEvent, SignInEffect>(SignInUiState()) {

    private var rosterJob: Job? = null

    override fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.OnRoleChanged -> {
                updateState { it.copy(role = event.role) }
                loadRoster()
            }
            is SignInEvent.OnShopCodeChanged -> {
                val code = event.code.trim().uppercase()
                updateState { it.copy(shopCode = code, isShopCodeValid = AuthRules.validateShopCode(code)) }
                loadRoster()
            }
            is SignInEvent.OnUserSelected -> updateState { it.copy(selectedUser = event.user) }
            is SignInEvent.OnPinChanged ->
                updateState { it.copy(pin = event.pin, isPinValid = AuthRules.validatePin(event.pin)) }
            SignInEvent.SubmitSignIn -> submit()
            SignInEvent.Retry -> {
                updateState { it.copy(error = null, errorArg = "", isLoading = false) }
                loadRoster()
            }
        }
    }

    /** Fetches the names for the current shop code and role; a newer request replaces an older one. */
    private fun loadRoster() {
        rosterJob?.cancel()
        val current = state.value
        updateState {
            it.copy(roster = emptyList(), selectedUser = null, isEmpty = false, error = null, errorArg = "")
        }
        if (!current.isShopCodeValid) {
            updateState { it.copy(isRosterLoading = false) }
            return
        }
        updateState { it.copy(isRosterLoading = true) }
        rosterJob = viewModelScope.launch {
            when (val result = repository.roster(current.shopCode, current.role)) {
                is AppResult.Success -> updateState {
                    it.copy(isRosterLoading = false, roster = result.value, isEmpty = result.value.isEmpty())
                }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    updateState { it.copy(isRosterLoading = false, error = kind, errorArg = arg) }
                }
            }
        }
    }

    private fun submit() {
        val current = state.value
        val user = current.selectedUser
        if (!current.isShopCodeValid || user == null || !current.isPinValid || current.isLoading) return
        updateState { it.copy(isLoading = true, error = null, errorArg = "") }
        viewModelScope.launch {
            when (val result = repository.signIn(user.id, current.pin)) {
                is AppResult.Success -> {
                    updateState { it.copy(isLoading = false, isSuccess = true, pin = "", isPinValid = false) }
                    sendEffect(
                        if (result.value.mustChangePin) SignInEffect.NavigateToChangePin else SignInEffect.NavigateToHome,
                    )
                }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    updateState {
                        it.copy(isLoading = false, error = kind, errorArg = arg, pin = "", isPinValid = false)
                    }
                }
            }
        }
    }
}
