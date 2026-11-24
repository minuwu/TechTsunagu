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
    /** The Shop Registration form (ADR 0017) is showing instead of sign-in. */
    val registering: Boolean = false,
    val shopName: String = "",
    val ownerName: String = "",
    val ownerPin: String = "",
    /** The code the owner wants (TECHTSUNAGU); empty means a random one. */
    val wantedCode: String = "",
    /** The "forgot the shop code" form is showing: shop name, own name and PIN. */
    val finding: Boolean = false,
    /** Set once the new shop exists; the screen shows it before going home. */
    val newShopCode: String? = null,
)

sealed interface SignInEvent {
    data class OnRoleChanged(val role: Role) : SignInEvent
    data class OnShopCodeChanged(val code: String) : SignInEvent
    data class OnUserSelected(val user: RosterUser) : SignInEvent
    data class OnPinChanged(val pin: String) : SignInEvent
    data object SubmitSignIn : SignInEvent
    data object Retry : SignInEvent
    /** Fills the form with a seeded demo account; the person still taps Sign in. */
    data class PrefillDemo(val account: DemoAccount) : SignInEvent
    data class ShowRegister(val show: Boolean) : SignInEvent
    data class OnShopNameChanged(val name: String) : SignInEvent
    data class OnOwnerNameChanged(val name: String) : SignInEvent
    data class OnOwnerPinChanged(val pin: String) : SignInEvent
    data object SubmitRegister : SignInEvent
    data object FinishRegister : SignInEvent
    data class OnWantedCodeChanged(val code: String) : SignInEvent
    data class ShowFind(val show: Boolean) : SignInEvent
    data object SubmitFind : SignInEvent
}

/** The seeded demo accounts (backend `seed_data/work.json`; fictional people, demo PINs). */
data class DemoAccount(val role: Role, val shopCode: String, val name: String, val pin: String)

val DEMO_ACCOUNTS = listOf(
    DemoAccount(Role.FOREMAN, "SASEBO01", "山口 隆", "246810"),
    DemoAccount(Role.WORKER, "SASEBO01", "Rahim Uddin", "2468"),
    DemoAccount(Role.WORKER, "SASEBO01", "Sadia Akter", "2468"),
    DemoAccount(Role.WORKER, "SASEBO01", "Tanvir Hasan", "2468"),
)

private const val OWNER_PIN_LENGTH = 6

val SignInUiState.canRegister: Boolean
    get() = shopName.isNotBlank() && ownerName.isNotBlank() && ownerPin.length == OWNER_PIN_LENGTH &&
        (wantedCode.isEmpty() || AuthRules.validateShopCode(wantedCode))

val SignInUiState.canFind: Boolean
    get() = shopName.isNotBlank() && ownerName.isNotBlank() && AuthRules.validatePin(ownerPin)

sealed interface SignInEffect {
    data object NavigateToHome : SignInEffect
    data object NavigateToChangePin : SignInEffect
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository,
) : MviViewModel<SignInUiState, SignInEvent, SignInEffect>(SignInUiState()) {

    private var rosterJob: Job? = null

    // The demo name to select once the roster arrives.
    private var demoName: String? = null

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
            is SignInEvent.PrefillDemo -> {
                val a = event.account
                demoName = a.name
                updateState {
                    it.copy(
                        role = a.role,
                        shopCode = a.shopCode,
                        isShopCodeValid = AuthRules.validateShopCode(a.shopCode),
                        pin = a.pin,
                        isPinValid = AuthRules.validatePin(a.pin),
                    )
                }
                loadRoster()
            }
            SignInEvent.SubmitSignIn -> submit()
            is SignInEvent.ShowRegister -> updateState { it.copy(registering = event.show, error = null, errorArg = "") }
            is SignInEvent.OnShopNameChanged -> updateState { it.copy(shopName = event.name.take(60)) }
            is SignInEvent.OnOwnerNameChanged -> updateState { it.copy(ownerName = event.name.take(40)) }
            is SignInEvent.OnOwnerPinChanged ->
                updateState { it.copy(ownerPin = event.pin.filter(Char::isDigit).take(OWNER_PIN_LENGTH)) }
            SignInEvent.SubmitRegister -> register()
            SignInEvent.FinishRegister -> viewModelScope.launch {
                updateState { it.copy(shopCode = it.newShopCode.orEmpty(), role = Role.FOREMAN) }
                repository.finishRegister()
                sendEffect(SignInEffect.NavigateToHome)
            }
            is SignInEvent.OnWantedCodeChanged ->
                updateState { it.copy(wantedCode = event.code.filter(Char::isLetterOrDigit).uppercase().take(12)) }
            is SignInEvent.ShowFind -> updateState { it.copy(finding = event.show, error = null, errorArg = "") }
            SignInEvent.SubmitFind -> find()
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
                    val pick = demoName?.let { name -> result.value.firstOrNull { u -> u.displayName == name } }
                    demoName = null
                    it.copy(
                        isRosterLoading = false,
                        roster = result.value,
                        isEmpty = result.value.isEmpty(),
                        selectedUser = pick,
                    )
                }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    updateState { it.copy(isRosterLoading = false, error = kind, errorArg = arg) }
                }
            }
        }
    }

    private fun register() {
        val s = state.value
        if (!s.canRegister || s.isLoading) return
        updateState { it.copy(isLoading = true, error = null, errorArg = "") }
        viewModelScope.launch {
            when (val result = repository.register(s.shopName.trim(), s.ownerName.trim(), s.ownerPin, s.wantedCode.ifEmpty { null })) {
                is AppResult.Success -> updateState { it.copy(isLoading = false, newShopCode = result.value, ownerPin = "") }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    updateState { it.copy(isLoading = false, error = kind, errorArg = arg) }
                }
            }
        }
    }

    private fun find() {
        val s = state.value
        if (!s.canFind || s.isLoading) return
        updateState { it.copy(isLoading = true, error = null, errorArg = "") }
        viewModelScope.launch {
            when (val result = repository.find(s.shopName.trim(), s.ownerName.trim(), s.ownerPin)) {
                is AppResult.Success -> {
                    val user = result.value.user
                    updateState {
                        it.copy(isLoading = false, isSuccess = true, ownerPin = "", shopCode = user.shopCode.orEmpty(), role = user.role)
                    }
                    sendEffect(if (result.value.mustChangePin) SignInEffect.NavigateToChangePin else SignInEffect.NavigateToHome)
                }
                is AppResult.Failure -> {
                    val (kind, arg) = result.error.toAuthError()
                    // a 401 here means no one has that shop name, name and PIN, not only a wrong PIN
                    val shown = if (kind == AuthErrorKind.WRONG_PIN) AuthErrorKind.NO_MATCH else kind
                    updateState { it.copy(isLoading = false, error = shown, errorArg = arg, ownerPin = "") }
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
