package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.home.data.FakeSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionStore: TestSessionStore
    private lateinit var settingsStore: FakeSettingsStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionStore = TestSessionStore(
            Session(
                token = "token",
                user = DemoSeed.demoWorker,
            )
        )
        settingsStore = FakeSettingsStore()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun workerInitialStateHasThreeSteps() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isForeman)
        assertEquals(3, state.totalSteps)
        assertEquals(0, state.step)
        assertFalse(state.isJoined)
    }

    @Test
    fun foremanInitialStateHasTwoSteps() = runTest(testDispatcher) {
        sessionStore.save(Session(token = "tok", user = DemoSeed.demoForeman))
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isForeman)
        assertEquals(2, state.totalSteps)
    }

    @Test
    fun workerStep0ScanJoinsTeam() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.ScanTeamQr)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isScanning)
        assertTrue(state.isJoined)
    }

    @Test
    fun workerStep1LanguageSelection() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.SelectLanguage("bn"))
        assertEquals("bn", viewModel.state.value.selectedLanguage)
    }

    @Test
    fun workerStep2CheckGrading() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        // Wrong answer (index 1: Gloves)
        viewModel.onEvent(OnboardingEvent.SelectCheckAnswer(1))
        assertFalse(viewModel.state.value.isAnswerCorrect)
        assertTrue(viewModel.state.value.isAnswerIncorrect)

        // Correct answer (index 0: Helmet)
        viewModel.onEvent(OnboardingEvent.SelectCheckAnswer(0))
        assertTrue(viewModel.state.value.isAnswerCorrect)
        assertFalse(viewModel.state.value.isAnswerIncorrect)
    }

    @Test
    fun foremanStep0CopyCode() = runTest(testDispatcher) {
        sessionStore.save(Session(token = "tok", user = DemoSeed.demoForeman))
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.CopyTeamCode)
        assertTrue(viewModel.state.value.codeCopied)
    }

    @Test
    fun foremanStep1GrantMic() = runTest(testDispatcher) {
        sessionStore.save(Session(token = "tok", user = DemoSeed.demoForeman))
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.GrantMicPermission)
        assertTrue(viewModel.state.value.micGranted)
    }

    @Test
    fun navigationNextAndPrev() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.Next)
        assertEquals(1, viewModel.state.value.step)

        viewModel.onEvent(OnboardingEvent.Prev)
        assertEquals(0, viewModel.state.value.step)
    }

    @Test
    fun skipOrFinishSetsOnboardingDoneAndEmitsEffect() = runTest(testDispatcher) {
        val viewModel = OnboardingViewModel(sessionStore, settingsStore)
        advanceUntilIdle()

        viewModel.onEvent(OnboardingEvent.Skip)
        advanceUntilIdle()

        assertTrue(settingsStore.isOnboardingDone.value)
        val effect = viewModel.effects.first()
        assertEquals(OnboardingEffect.FinishOnboarding, effect)
    }
}
