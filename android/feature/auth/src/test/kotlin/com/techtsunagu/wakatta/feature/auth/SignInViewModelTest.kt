package com.techtsunagu.wakatta.feature.auth

import app.cash.turbine.test
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.User
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = ScriptedAuthRepository()
    private lateinit var viewModel: SignInViewModel

    private val worker = RosterUser("12", "Rahim", "worker")

    private fun session(mustChangePin: Boolean) = Session(
        token = "t",
        user = User(id = "12", displayName = "Rahim", role = Role.WORKER, language = "bn"),
        mustChangePin = mustChangePin,
    )

    private fun fillAndSelect() {
        viewModel.onEvent(SignInEvent.OnShopCodeChanged("SASEBO01"))
        viewModel.onEvent(SignInEvent.OnUserSelected(worker))
        viewModel.onEvent(SignInEvent.OnPinChanged("9137"))
    }

    @Before
    fun setUp() {
        repository.rosterResult = AppResult.Success(listOf(worker))
        viewModel = SignInViewModel(repository)
    }

    @Test
    fun initialStateIsDefault() {
        val state = viewModel.state.value
        assertEquals(Role.WORKER, state.role)
        assertEquals("", state.shopCode)
        assertFalse(state.isShopCodeValid)
        assertTrue(state.roster.isEmpty())
        assertNull(state.selectedUser)
        assertFalse(state.isPinValid)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun validShopCodeLoadsRoster() {
        viewModel.onEvent(SignInEvent.OnShopCodeChanged("sasebo01"))
        val state = viewModel.state.value
        assertEquals("SASEBO01", state.shopCode)
        assertTrue(state.isShopCodeValid)
        assertEquals(listOf(worker), state.roster)
    }

    @Test
    fun invalidShopCodeLoadsNothing() {
        viewModel.onEvent(SignInEvent.OnShopCodeChanged("123"))
        val state = viewModel.state.value
        assertFalse(state.isShopCodeValid)
        assertTrue(state.roster.isEmpty())
    }

    @Test
    fun changingRoleReloadsRosterForThatRole() {
        viewModel.onEvent(SignInEvent.OnShopCodeChanged("SASEBO01"))
        viewModel.onEvent(SignInEvent.OnRoleChanged(Role.FOREMAN))
        assertEquals(Role.FOREMAN, repository.lastRosterRole)
        assertEquals(Role.FOREMAN, viewModel.state.value.role)
    }

    @Test
    fun unknownShopCodeShowsError() {
        repository.rosterResult = AppResult.Failure(AppError.NotFound)
        viewModel.onEvent(SignInEvent.OnShopCodeChanged("SASEBO01"))
        assertEquals(AuthErrorKind.UNKNOWN_SHOP, viewModel.state.value.error)
    }

    @Test
    fun wrongPinShowsErrorAndClearsPin() {
        repository.signInResult = AppResult.Failure(AppError.WrongPin)
        fillAndSelect()
        viewModel.onEvent(SignInEvent.SubmitSignIn)
        val state = viewModel.state.value
        assertEquals(AuthErrorKind.WRONG_PIN, state.error)
        assertEquals("", state.pin)
        assertFalse(state.isLoading)
    }

    @Test
    fun pinLockShowsUnlockTimeInShopTime() {
        repository.signInResult = AppResult.Failure(AppError.PinLocked("2026-12-22T10:15:00Z"))
        fillAndSelect()
        viewModel.onEvent(SignInEvent.SubmitSignIn)
        assertEquals(AuthErrorKind.LOCKED, viewModel.state.value.error)
        assertEquals("19:15", viewModel.state.value.errorArg)
    }

    @Test
    fun rateLimitShowsSeconds() {
        repository.signInResult = AppResult.Failure(AppError.RateLimited(60))
        fillAndSelect()
        viewModel.onEvent(SignInEvent.SubmitSignIn)
        assertEquals(AuthErrorKind.RATE_LIMITED, viewModel.state.value.error)
        assertEquals("60", viewModel.state.value.errorArg)
    }

    @Test
    fun mustChangePinRoutesToChangePin() = runTest {
        repository.signInResult = AppResult.Success(session(mustChangePin = true))
        fillAndSelect()
        viewModel.effects.test {
            viewModel.onEvent(SignInEvent.SubmitSignIn)
            assertEquals(SignInEffect.NavigateToChangePin, awaitItem())
        }
        assertTrue(viewModel.state.value.isSuccess)
    }

    @Test
    fun normalSignInRoutesHome() = runTest {
        repository.signInResult = AppResult.Success(session(mustChangePin = false))
        fillAndSelect()
        viewModel.effects.test {
            viewModel.onEvent(SignInEvent.SubmitSignIn)
            assertEquals(SignInEffect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun demoChipFillsTheFormButDoesNotSignIn() {
        val foreman = RosterUser("1", "山口 隆", "foreman")
        repository.rosterResult = AppResult.Success(listOf(foreman))
        viewModel.onEvent(SignInEvent.PrefillDemo(DEMO_ACCOUNTS.first()))
        val state = viewModel.state.value
        assertEquals(Role.FOREMAN, state.role)
        assertEquals("SASEBO01", state.shopCode)
        assertEquals(foreman, state.selectedUser)
        assertTrue(state.isPinValid)
        assertFalse(state.isSuccess)
    }

    @Test
    fun registeringShowsTheNewShopCodeThenGoesHome() = runTest {
        viewModel.onEvent(SignInEvent.ShowRegister(true))
        viewModel.onEvent(SignInEvent.OnShopNameChanged("Minato Works"))
        viewModel.onEvent(SignInEvent.OnOwnerNameChanged("森 太郎"))
        viewModel.onEvent(SignInEvent.OnOwnerPinChanged("73a9215"))
        assertEquals("739215", viewModel.state.value.ownerPin)

        viewModel.onEvent(SignInEvent.SubmitRegister)
        assertEquals("NEWSHOP1", viewModel.state.value.newShopCode)

        viewModel.effects.test {
            viewModel.onEvent(SignInEvent.FinishRegister)
            assertEquals(SignInEffect.NavigateToHome, awaitItem())
        }
    }
}
