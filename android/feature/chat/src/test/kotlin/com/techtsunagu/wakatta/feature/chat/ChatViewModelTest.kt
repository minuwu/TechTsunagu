package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.chat.data.ChatRepository
import com.techtsunagu.wakatta.feature.chat.data.FakeAudioRecorder
import com.techtsunagu.wakatta.feature.chat.data.FakeChatRepository
import com.techtsunagu.wakatta.feature.chat.data.FakeSpeaker
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private class TestSessionStore(session: Session?) : SessionStore {
        private val flow = MutableStateFlow(session)
        override val session: StateFlow<Session?> = flow
        override suspend fun save(session: Session) { flow.value = session }
        override suspend fun clear() { flow.value = null }
        override fun getCachedToken(): String? = flow.value?.token
    }

    /** Wraps the Fake repository; can fail calls and records what the ViewModel passed. */
    private class SpyRepository(private val inner: ChatRepository) : ChatRepository by inner {
        var failSend: AppError? = null
        var failAnswers: AppError? = null
        var failTranscribe: AppError? = null
        var transcript = "TRANSCRIPT"
        val sendKeys = mutableListOf<String>()
        val sendIsVoice = mutableListOf<Boolean>()
        val answerKeys = mutableListOf<String>()
        var activeCollectors = 0

        override fun getMessages(threadId: String): Flow<List<ChatMessageUiModel>> =
            inner.getMessages(threadId).onStart { activeCollectors++ }.onCompletion { activeCollectors-- }

        override suspend fun transcribe(audio: File?): AppResult<String> =
            failTranscribe?.let { AppResult.Failure(it) } ?: AppResult.Success(transcript)

        override suspend fun sendMessage(
            threadId: String,
            text: String,
            isVoice: Boolean,
            fromUserId: String,
            idempotencyKey: String,
        ): AppResult<ChatMessageUiModel> {
            sendKeys += idempotencyKey
            sendIsVoice += isVoice
            failSend?.let { return AppResult.Failure(it) }
            return inner.sendMessage(threadId, text, isVoice, fromUserId, idempotencyKey)
        }

        override suspend fun submitAnswers(
            messageId: String,
            userId: String,
            answers: List<Int>,
            idempotencyKey: String,
        ): AppResult<AnswerResultDto> {
            answerKeys += idempotencyKey
            failAnswers?.let { return AppResult.Failure(it) }
            return inner.submitAnswers(messageId, userId, answers, idempotencyKey)
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SpyRepository

    private val foreman = Session("t", user = User("u_tanaka", "Tanaka", Role.FOREMAN, "ja"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = SpyRepository(FakeChatRepository().apply { setLatency(0L) })
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(session: Session? = foreman, debug: Boolean = false) = ChatViewModel(
        chatRepository = repository,
        audioRecorder = FakeAudioRecorder(),
        speaker = FakeSpeaker(),
        sessionStore = TestSessionStore(session),
        appConfig = AppConfig(true, "", "", "1", 1, isDebug = debug),
    )

    private fun ChatViewModel.init(threadId: String = "t_line2") {
        onEvent(ChatEvent.Init(threadId))
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun initLoadsThreadTitleAndIdentityFromRepositoryAndSession() = runTest {
        val vm = viewModel()
        vm.init()

        val state = vm.state.value
        assertEquals("t_line2", state.threadId)
        assertEquals("Line 2 · プレス班", state.title)
        assertEquals("Line 2 · Press team", state.titleTri["en"])
        assertEquals(4, state.membersCount)
        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertNull(state.error)
        assertTrue(state.messages.isNotEmpty())
        assertEquals(Role.FOREMAN, state.currentRole)
        assertEquals("u_tanaka", state.currentUserId)
        assertTrue(state.messages.first { it.id == "m1" }.isMine)
    }

    @Test
    fun withoutSessionRoleIsUnknown() = runTest {
        val vm = viewModel(session = null)
        vm.init()
        assertEquals(Role.UNKNOWN, vm.state.value.currentRole)
        assertEquals("", vm.state.value.currentUserId)
    }

    @Test
    fun toggleRoleWorksOnlyInDebug() = runTest {
        val release = viewModel(debug = false)
        release.init()
        release.onEvent(ChatEvent.ToggleRole)
        assertEquals(Role.FOREMAN, release.state.value.currentRole)

        val debug = viewModel(debug = true)
        debug.init()
        assertTrue(debug.state.value.canSwitchRole)
        debug.onEvent(ChatEvent.ToggleRole)
        assertEquals(Role.WORKER, debug.state.value.currentRole)
        assertEquals("u_rahim", debug.state.value.currentUserId)
        debug.onEvent(ChatEvent.ToggleRole)
        assertEquals(Role.FOREMAN, debug.state.value.currentRole)
        assertEquals("u_tanaka", debug.state.value.currentUserId)
    }

    @Test
    fun retryAndRefreshKeepOneMessageCollector() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.Retry)
        vm.onEvent(ChatEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.activeCollectors)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun toggleShowOriginalFlipsFlag() = runTest {
        val vm = viewModel()
        vm.init()

        assertFalse(vm.state.value.messages.first { it.id == "m1" }.showOriginal)
        vm.onEvent(ChatEvent.ToggleShowOriginal("m1"))
        assertTrue(vm.state.value.messages.first { it.id == "m1" }.showOriginal)
        vm.onEvent(ChatEvent.ToggleShowOriginal("m1"))
        assertFalse(vm.state.value.messages.first { it.id == "m1" }.showOriginal)
    }

    @Test
    fun onInputTextChangedUpdatesComposerState() = runTest {
        val vm = viewModel()
        vm.onEvent(ChatEvent.OnInputTextChanged("Hello team"))
        assertEquals("Hello team", vm.state.value.inputText)
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)

        vm.onEvent(ChatEvent.OnInputTextChanged(""))
        assertEquals("", vm.state.value.inputText)
        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
    }

    @Test
    fun recordingFillsInputFromTranscriptAndStartsCountdown() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        assertEquals(ComposerStatus.RECORDING, vm.state.value.composerStatus)
        assertTrue(vm.state.value.isListening)

        vm.onEvent(ChatEvent.StopRecording)
        testDispatcher.scheduler.advanceTimeBy(150)

        assertEquals(ComposerStatus.COUNTDOWN, vm.state.value.composerStatus)
        assertFalse(vm.state.value.isListening)
        assertEquals("TRANSCRIPT", vm.state.value.inputText)

        vm.onEvent(ChatEvent.CancelCountdown)
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)
    }

    @Test
    fun transcribeFailureReportsErrorAndStaysIdle() = runTest {
        repository.failTranscribe = AppError.Offline
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        vm.onEvent(ChatEvent.StopRecording)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
        assertEquals("", vm.state.value.inputText)
        assertEquals(ChatEffect.ShowError(AppError.Offline), vm.effects.first())
    }

    @Test
    fun countdownTicksDoNotReEmitUiState() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.StartRecording)
        vm.onEvent(ChatEvent.StopRecording)
        testDispatcher.scheduler.advanceTimeBy(150)
        val before = vm.state.value

        testDispatcher.scheduler.advanceTimeBy(1_500)

        assertSame(before, vm.state.value)
        assertTrue(vm.countdown.value.progress < 1.0f)
    }

    @Test
    fun voiceFlagFollowsDictationNotTextContent() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        vm.onEvent(ChatEvent.StopRecording)
        testDispatcher.scheduler.advanceTimeBy(150)
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(ChatEvent.OnInputTextChanged("金型交換 " + "x".repeat(40)))
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(true, false), repository.sendIsVoice)
    }

    @Test
    fun sendNowDispatchesMessage() = runTest {
        val vm = viewModel()
        vm.init()
        val initialCount = vm.state.value.messages.size

        vm.onEvent(ChatEvent.OnInputTextChanged("Shift inspection completed"))
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.state.value.inputText)
        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
        assertEquals(initialCount + 1, vm.state.value.messages.size)
    }

    @Test
    fun failedSendKeepsTextShowsErrorAndRetryReusesTheKey() = runTest {
        repository.failSend = AppError.Offline
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.OnInputTextChanged("Shift inspection completed"))

        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Shift inspection completed", vm.state.value.inputText)
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)
        assertEquals(ChatEffect.ShowError(AppError.Offline), vm.effects.first())

        repository.failSend = null
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", vm.state.value.inputText)
        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
        assertEquals(2, repository.sendKeys.size)
        assertEquals(repository.sendKeys[0], repository.sendKeys[1])
    }

    @Test
    fun editedTextGetsANewKey() = runTest {
        repository.failSend = AppError.Timeout
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.OnInputTextChanged("first"))
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(ChatEvent.OnInputTextChanged("first, edited"))
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotEquals(repository.sendKeys[0], repository.sendKeys[1])
    }

    @Test
    fun workerCheckAnswerFlowGradesCorrectly() = runTest {
        val vm = viewModel(debug = true)
        vm.init()
        vm.onEvent(ChatEvent.ToggleRole)

        vm.onEvent(ChatEvent.SubmitAnswers("m1", listOf(0)))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(WakattaStatus.GREEN, vm.state.value.messages.first { it.id == "m1" }.workerAnswerStatus)
    }

    @Test
    fun failedAnswerSubmitReportsErrorAndRetryReusesKeyUnlessAnswersChange() = runTest {
        repository.failAnswers = AppError.Offline
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.SubmitAnswers("m1", listOf(0)))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ChatEffect.ShowError(AppError.Offline), vm.effects.first())

        vm.onEvent(ChatEvent.SubmitAnswers("m1", listOf(0)))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(repository.answerKeys[0], repository.answerKeys[1])

        vm.onEvent(ChatEvent.SubmitAnswers("m1", listOf(1)))
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotEquals(repository.answerKeys[1], repository.answerKeys[2])
    }

    @Test
    fun toggleWkFoldExpandsForForeman() = runTest {
        val vm = viewModel()
        vm.init()

        assertFalse(vm.state.value.messages.first { it.id == "m1" }.isWkFoldExpanded)
        vm.onEvent(ChatEvent.ToggleWkFold("m1"))
        assertTrue(vm.state.value.messages.first { it.id == "m1" }.isWkFoldExpanded)
    }

    @Test
    fun reexplainDraftComesFromRepositoryAndSubmitSendsWorkerLanguageText() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.OpenReexplain("m1", "u_siti"))
        testDispatcher.scheduler.advanceUntilIdle()
        val dialog = vm.state.value.reexplainDialog
        assertNotNull(dialog)
        assertEquals("Siti Rahma", dialog!!.workerName)
        assertEquals("油圧オイル", dialog.confusingTerm)

        vm.onEvent(
            ChatEvent.UpdateReexplainText("詳しい説明", dialog.explanation + (dialog.workerLang to "Detailed explanation")),
        )
        assertEquals("詳しい説明", vm.state.value.reexplainDialog?.easyJaText)
        assertEquals("Detailed explanation", vm.state.value.reexplainDialog?.explanation?.get(dialog.workerLang))

        vm.onEvent(ChatEvent.SubmitReexplain("m1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.state.value.reexplainDialog)
        assertTrue(vm.state.value.messages.any { it.kind == MessageKind.REEXPLAIN })
    }

    @Test
    fun reexplainWithoutFollowUpReportsErrorAndOpensNothing() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.OpenReexplain("m1", "u_nobody"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.state.value.reexplainDialog)
        assertEquals(ChatEffect.ShowError(AppError.NotFound), vm.effects.first())
    }

    @Test
    fun selectAndDismissGlossaryTerm() = runTest {
        val vm = viewModel()
        val term = GlossaryTerm("金型交換", "かながたこうかん", "ডাই পরিবর্তন", "Die change")
        vm.onEvent(ChatEvent.SelectGlossaryTerm(term))
        assertEquals(term, vm.state.value.selectedGlossaryTerm)

        vm.onEvent(ChatEvent.DismissGlossaryTerm)
        assertNull(vm.state.value.selectedGlossaryTerm)
    }

    @Test
    fun viewModelsHaveOnlyTheHiltConstructor() {
        assertEquals(1, ChatViewModel::class.java.constructors.size)
        assertEquals(1, ChatsViewModel::class.java.constructors.size)
    }
}
