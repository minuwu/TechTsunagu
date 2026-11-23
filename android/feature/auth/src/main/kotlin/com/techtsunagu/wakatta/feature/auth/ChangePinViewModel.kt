package com.techtsunagu.wakatta.feature.auth

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class ChangePinUiState(
    val currentPin: String = "",
    val newPin: String = "",
    val confirmPin: String = "",
    val isNewPinValid: Boolean = false,
    val doPinsMatch: Boolean = false,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: AuthErrorKind? = null,
    val errorArg: String = "",
    val isSuccess: Boolean = false,
)

sealed interface ChangePinEvent {
    data class OnCurrentPinChanged(val pin: String) : ChangePinEvent
    data class OnNewPinChanged(val pin: String) : ChangePinEvent
    data class OnConfirmPinChanged(val pin: String) : ChangePinEvent
    data object SubmitChangePin : ChangePinEvent
    data object Retry : ChangePinEvent
}

sealed interface ChangePinEffect {
    data object NavigateBack : ChangePinEffect
}

@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val repository: AuthRepository,
) : MviViewModel<ChangePinUiState, ChangePinEvent, ChangePinEffect>(ChangePinUiState()) {

    override fun onEvent(event: ChangePinEvent) {
        when (event) {
            is ChangePinEvent.OnCurrentPinChanged -> updateState { it.copy(currentPin = event.pin) }
            is ChangePinEvent.OnNewPinChanged -> {
                val isValid = AuthRules.validatePin(event.pin)
                val matches = isValid && event.pin == state.value.confirmPin
                updateState { it.copy(newPin = event.pin, isNewPinValid = isValid, doPinsMatch = matches) }
            }
            is ChangePinEvent.OnConfirmPinChanged -> {
                val matches = state.value.isNewPinValid && state.value.newPin == event.pin
                updateState { it.copy(confirmPin = event.pin, doPinsMatch = matches) }
            }
            ChangePinEvent.SubmitChangePin -> submit()
            ChangePinEvent.Retry -> updateState { it.copy(error = null, errorArg = "", isLoading = false) }
        }
    }

    private fun submit() {
        val current = state.value
        val ready = AuthRules.validatePin(current.currentPin) && current.isNewPinValid && current.doPinsMatch
        if (!ready || current.isLoading) return
        updateState { it.copy(isLoading = true, error = null, errorArg = "") }
        viewModelScope.launch {
            when (val result = repository.changePin(current.currentPin, current.newPin)) {
                is AppResult.Success -> {
                    updateState { it.copy(isLoading = false, isSuccess = true) }
                    sendEffect(ChangePinEffect.NavigateBack)
                }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    updateState { it.copy(isLoading = false, error = kind, errorArg = arg) }
                }
            }
        }
    }
}
