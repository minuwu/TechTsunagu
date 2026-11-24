package com.techtsunagu.wakatta.feature.auth

import app.cash.turbine.test
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class ChangePinViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = ScriptedAuthRepository()
    private val viewModel = ChangePinViewModel(repository)

    private fun fill() {
        viewModel.onEvent(ChangePinEvent.OnCurrentPinChanged("9137"))
        viewModel.onEvent(ChangePinEvent.OnNewPinChanged("1357"))
        viewModel.onEvent(ChangePinEvent.OnConfirmPinChanged("1357"))
    }

    @Test
    fun successGoesBack() = runTest {
        fill()
        viewModel.effects.test {
            viewModel.onEvent(ChangePinEvent.SubmitChangePin)
            assertEquals(ChangePinEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun wrongCurrentPinShowsError() {
        repository.changePinResult = AppResult.Failure(AppError.WrongPin)
        fill()
        viewModel.onEvent(ChangePinEvent.SubmitChangePin)
        assertEquals(AuthErrorKind.WRONG_PIN, viewModel.state.value.error)
    }

    @Test
    fun rejectedNewPinShowsError() {
        repository.changePinResult = AppResult.Failure(AppError.Validation(listOf("newPin")))
        fill()
        viewModel.onEvent(ChangePinEvent.SubmitChangePin)
        assertEquals(AuthErrorKind.PIN_REJECTED, viewModel.state.value.error)
    }

    @Test
    fun mismatchingConfirmationDoesNotSubmit() {
        viewModel.onEvent(ChangePinEvent.OnCurrentPinChanged("9137"))
        viewModel.onEvent(ChangePinEvent.OnNewPinChanged("1357"))
        viewModel.onEvent(ChangePinEvent.OnConfirmPinChanged("1358"))
        viewModel.onEvent(ChangePinEvent.SubmitChangePin)
        assertFalse(viewModel.state.value.isSuccess)
    }
}
