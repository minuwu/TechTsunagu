package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.Passport
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.navigation.LearningKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.NoteKey
import com.techtsunagu.wakatta.core.navigation.OnboardingKey
import com.techtsunagu.wakatta.core.navigation.SkillsKey
import com.techtsunagu.wakatta.feature.home.data.FakeSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class TestSignOffRepository : SignOffRepository {
    override suspend fun getSignOffs(workerId: String): AppResult<List<SignOff>> = AppResult.Success(emptyList())
    override suspend fun getPassport(workerId: String): AppResult<Passport> = AppResult.Success(
        Passport(
            workerId = workerId,
            workerName = "Rahim Uddin",
            signOffs = listOf(
                SignOff(
                    machineId = "mc1",
                    machineName = mapOf("en" to "Lathe 1"),
                    passedAt = "2026-11-22T10:00:00Z",
                )
            )
        )
    )
}

internal class TestLanguageManager(initialLang: String = "ja") : LanguageManager {
    private val _currentLanguage = MutableStateFlow(initialLang)
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    override suspend fun setLanguage(lang: String) { _currentLanguage.value = lang }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var sessionStore: TestSessionStore
    private lateinit var settingsStore: FakeSettingsStore
    private lateinit var signOffRepository: SignOffRepository
    private lateinit var languageManager: LanguageManager

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
        signOffRepository = TestSignOffRepository()
        languageManager = TestLanguageManager("ja")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MeViewModel {
        return MeViewModel(
            sessionStore = sessionStore,
            settingsStore = settingsStore,
            signOffRepository = signOffRepository,
            machineRepository = TestMachineRepository(),
            boardSource = com.techtsunagu.wakatta.feature.home.data.FakeHomeBoardSource(),
            languageManager = languageManager,
        )
    }

    @Test
    fun loadDataPopulatesWorkerProfileAndPassport() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isForeman)
        assertEquals("Rahim Uddin", state.userName)
        assertEquals("ja", state.currentLanguage)
        assertTrue(state.passportMachines.isNotEmpty())
    }

    @Test
    fun toggleSettingsUpdatesStoreAndState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MeEvent.ToggleDarkMode(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isDarkMode)
        assertEquals(true, settingsStore.isDarkMode.value)

        viewModel.onEvent(MeEvent.ToggleLargeText(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isLargeText)
        assertTrue(settingsStore.isLargeText.value)

        viewModel.onEvent(MeEvent.ToggleSimulateOffline(true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSimulateOffline)
        assertTrue(settingsStore.isOfflineHintEnabled.value)

        viewModel.onEvent(MeEvent.ToggleNotifyMessages(false))
        advanceUntilIdle()
        assertFalse(viewModel.state.value.notifyMessages)
        assertFalse(settingsStore.isNotificationEnabled.value)
    }

    @Test
    fun selectLanguageUpdatesManagerAndEmitsApplyLocales() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MeEvent.SelectLanguage("bn"))
        advanceUntilIdle()

        assertEquals("bn", languageManager.currentLanguage.value)
        assertEquals("bn", viewModel.state.value.currentLanguage)
        assertFalse(viewModel.state.value.showLanguageDialog)

        val effect = viewModel.effects.first()
        assertTrue(effect is MeEffect.ApplyLocales)
        assertEquals("bn", (effect as MeEffect.ApplyLocales).languageTag)
    }

    @Test
    fun passportMachineClickEmitsCorrectNavKey() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MeEvent.PassportMachineClick("mc1", isSignedOff = true))
        val e1 = viewModel.effects.first()
        assertEquals(MachineKey("mc1"), (e1 as MeEffect.Navigate).key)

        viewModel.onEvent(MeEvent.PassportMachineClick("mc2", isSignedOff = false))
        val e2 = viewModel.effects.first()
        assertEquals(NoteKey("mc2"), (e2 as MeEffect.Navigate).key)
    }

    @Test
    fun actionsEmitNavKeys() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MeEvent.OpenSkillsMatrix)
        val e1 = viewModel.effects.first()
        assertEquals(SkillsKey, (e1 as MeEffect.Navigate).key)

        viewModel.onEvent(MeEvent.OpenMyLearning)
        val e2 = viewModel.effects.first()
        assertTrue((e2 as MeEffect.Navigate).key is LearningKey)

        viewModel.onEvent(MeEvent.ReplayOnboarding)
        val e3 = viewModel.effects.first()
        assertEquals(OnboardingKey, (e3 as MeEffect.Navigate).key)
    }

    @Test
    fun switchRoleDemoTogglesRoleInSession() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isForeman)

        viewModel.onEvent(MeEvent.SwitchRoleDemo)
        advanceUntilIdle()

        assertEquals(Role.FOREMAN, sessionStore.session.value?.user?.role)
        assertTrue(viewModel.state.value.isForeman)
    }

    @Test
    fun signOutClearsSessionAndNavigatesToSignIn() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(MeEvent.SignOut)
        advanceUntilIdle()

        assertNull(sessionStore.session.value)
        assertEquals(MeEffect.ApplyLocales(""), viewModel.effects.first())
    }
}
