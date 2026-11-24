package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.media.DictationErrorKind
import com.techtsunagu.wakatta.core.media.DictationEvent
import com.techtsunagu.wakatta.core.media.DictationSource
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
import com.techtsunagu.wakatta.feature.chat.data.FakeChatRepository
import com.techtsunagu.wakatta.feature.chat.data.FakeSpeaker
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        val sendAudios = mutableListOf<File?>()
        val sendTexts = mutableListOf<String?>()
        val answerKeys = mutableListOf<String>()
        var activeCollectors = 0

        override fun getMessages(threadId: String): Flow<List<ChatMessageUiModel>> =
            inner.getMessages(threadId).onStart { activeCollectors++ }.onCompletion { activeCollectors-- }

        override suspend fun transcribe(audio: File, language: String): AppResult<String> =
            failTranscribe?.let { AppResult.Failure(it) } ?: AppResult.Success(transcript)

        override suspend fun sendMessage(
            threadId: String,
            text: String?,
            audio: File?,
            isVoice: Boolean,
            fromUserId: String,
            idempotencyKey: String,
        ): AppResult<ChatMessageUiModel> {
            sendKeys += idempotencyKey
            sendIsVoice += isVoice
            sendAudios += audio
            sendTexts += text
            failSend?.let { return AppResult.Failure(it) }
            return inner.sendMessage(threadId, text, audio, isVoice, fromUserId, idempotencyKey)
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

    private class FakeDictationSource : DictationSource {
        var isAvailableResult = true
        // one listen round ends on Final or Error, as the device recognizer's does
        val events = kotlinx.coroutines.channels.Channel<DictationEvent>(kotlinx.coroutines.channels.Channel.UNLIMITED)
        var lastTag: String? = null
        var startCount = 0
        var stopCount = 0

        override fun isAvailable(): Boolean = isAvailableResult

        override fun start(languageTag: String): Flow<DictationEvent> {
            startCount++
            lastTag = languageTag
            return kotlinx.coroutines.flow.flow {
                for (e in events) {
                    emit(e)
                    if (e is DictationEvent.Final || e is DictationEvent.Error) break
                }
            }
        }

        override fun stop() {
            stopCount++
        }
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SpyRepository
    private lateinit var fakeDictation: FakeDictationSource

    private val foreman = Session("t", user = User("u_tanaka", "Tanaka", Role.FOREMAN, "ja"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = SpyRepository(FakeChatRepository().apply { setLatency(0L) })
        fakeDictation = FakeDictationSource()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        session: Session? = foreman,
        debug: Boolean = false,
        dictation: DictationSource = fakeDictation,
        recorder: com.techtsunagu.wakatta.core.media.VoiceRecorder = object : com.techtsunagu.wakatta.core.media.VoiceRecorder {
            override fun start() = false
            override fun stop(): java.io.File? = null
            override fun cancel() {}
        },
    ) = ChatViewModel(
        chatRepository = repository,
        dictation = dictation,
        recorder = recorder,
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
    fun anOpenTranslation_staysOpen_whenNewMessagesArrive() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.ToggleShowOriginal("m1"))

        vm.onEvent(ChatEvent.OnInputTextChanged("new one"))
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.messages.any { it.originalText == "new one" })
        assertTrue(vm.state.value.messages.first { it.id == "m1" }.showOriginal)
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
    fun dictationUnavailable_andNoMicForTheServer_showsTypeInsteadHint() = runTest {
        fakeDictation.isAvailableResult = false
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)
        assertEquals(ChatEffect.ShowHint(R.string.chat_dictation_type_instead), vm.effects.first())
    }

    @Test
    fun offline_voiceIsKept_andBecomesTextWhenBackOnline() = runTest {
        fakeDictation.isAvailableResult = false
        val audio = File.createTempFile("dictation", ".m4a").apply { writeBytes(ByteArray(64)) }
        val recorder = object : com.techtsunagu.wakatta.core.media.VoiceRecorder {
            override fun start() = true
            override fun stop(): File? = audio
            override fun cancel() {}
        }
        repository.failTranscribe = AppError.Offline
        val vm = viewModel(recorder = recorder)
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(ChatEvent.StopRecording)
        testDispatcher.scheduler.runCurrent()
        assertEquals(ChatEffect.ShowHint(R.string.chat_dictation_saved), vm.effects.first())
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)

        repository.failTranscribe = null // the network is back
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(repository.transcript, vm.state.value.inputText)
        assertEquals(ChatEffect.ShowHint(R.string.chat_dictation_recovered), vm.effects.first())
    }

    @Test
    fun dictationPartial_updatesInputTextLive_andSetsStatusRecording() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ComposerStatus.RECORDING, vm.state.value.composerStatus)
        assertTrue(vm.state.value.isListening)

        fakeDictation.events.send(DictationEvent.Partial("明日の朝"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("明日の朝", vm.state.value.inputText)
        assertEquals(ComposerStatus.RECORDING, vm.state.value.composerStatus)
        assertTrue(vm.state.value.isListening)
    }

    @Test
    fun dictationFinal_setsInputText_andStartsCountdown() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Final("明日の朝8時集合"))
        fakeDictation.events.send(DictationEvent.Error(DictationErrorKind.NO_SPEECH)) // the long pause
        testDispatcher.scheduler.advanceTimeBy(50)

        assertEquals("明日の朝8時集合", vm.state.value.inputText)
        assertEquals(ComposerStatus.COUNTDOWN, vm.state.value.composerStatus)
        assertFalse(vm.state.value.isListening)

        vm.onEvent(ChatEvent.CancelCountdown)
        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)
        assertEquals("明日の朝8時集合", vm.state.value.inputText)
    }

    @Test
    fun dictationError_noSpeech_returnsToIdle_showsHint_andKeepsTypedText() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.OnInputTextChanged("Existing typed note"))
        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Error(DictationErrorKind.NO_SPEECH))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
        assertEquals("Existing typed note", vm.state.value.inputText)
        assertFalse(vm.state.value.isListening)
        assertEquals(ChatEffect.ShowHint(R.string.chat_dictation_no_speech), vm.effects.first())
    }

    @Test
    fun dictationError_networkOrUnavailable_returnsToEditing_showsTypeInsteadHint_andKeepsTypedText() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.OnInputTextChanged("Draft to keep"))
        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Error(DictationErrorKind.NETWORK))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ComposerStatus.EDITING, vm.state.value.composerStatus)
        assertEquals("Draft to keep", vm.state.value.inputText)
        assertFalse(vm.state.value.isListening)
        assertEquals(ChatEffect.ShowHint(R.string.chat_dictation_type_instead), vm.effects.first())
    }

    @Test
    fun dictationStop_callsDictationStop() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(ChatEvent.StopRecording)

        assertEquals(1, fakeDictation.stopCount)
        assertFalse(vm.state.value.isListening)
    }

    @Test
    fun countdownTicksDoNotReEmitUiState() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Final("テスト"))
        fakeDictation.events.send(DictationEvent.Error(DictationErrorKind.NO_SPEECH)) // the long pause
        testDispatcher.scheduler.advanceUntilIdle()
        val before = vm.state.value

        testDispatcher.scheduler.advanceTimeBy(1_500)

        assertSame(before, vm.state.value)
        assertTrue(vm.countdown.value.progress < 1.0f)
    }

    @Test
    fun dictatedVoiceMessage_sendsTextWithIsVoiceTrueAndNoAudio() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Final("金型交換の件"))
        fakeDictation.events.send(DictationEvent.Error(DictationErrorKind.NO_SPEECH)) // the long pause
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.sendTexts.size)
        assertEquals("金型交換の件", repository.sendTexts[0])
        assertTrue(repository.sendIsVoice[0])
        assertNull(repository.sendAudios[0])
    }

    @Test
    fun dictationTag_matchesSessionUserLanguage() = runTest {
        val worker = Session("w", user = User("u_rahim", "Rahim", Role.WORKER, "bn"))
        val workerVm = viewModel(session = worker)
        workerVm.init()

        workerVm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("bn-BD", fakeDictation.lastTag)

        val foremanVm = viewModel(session = foreman)
        foremanVm.init()
        foremanVm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("ja-JP", fakeDictation.lastTag)
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
    fun editDraftUpdatesMessageContent() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.EditDraft("m1", "修正後のテキスト"))
        testDispatcher.scheduler.advanceUntilIdle()

        val updated = vm.state.value.messages.first { it.id == "m1" }
        assertEquals("修正後のテキスト", updated.originalText)
    }

    @Test
    fun askExplainAgainCallsRepositoryAndAddsMessage() = runTest {
        val vm = viewModel()
        vm.init()

        vm.onEvent(ChatEvent.AskExplainAgain("m1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.state.value.messages.any { it.kind == MessageKind.ASK })
    }

    @Test
    fun viewModelsHaveOnlyTheHiltConstructor() {
        assertEquals(1, ChatViewModel::class.java.constructors.size)
        assertEquals(1, ChatsViewModel::class.java.constructors.size)
    }

    @Test
    fun sendingWhileDictatingSendsOnce_evenWhenALateFinalArrives() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Partial("金型交換"))
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(ChatEvent.SendNow)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeDictation.events.send(DictationEvent.Final("金型交換の件です"))
        testDispatcher.scheduler.advanceTimeBy(10_000)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.sendTexts.size)
        assertEquals("", vm.state.value.inputText)
    }

    @Test
    fun cancelWhileRecordingStopsListening() = runTest {
        val vm = viewModel()
        vm.init()
        vm.onEvent(ChatEvent.StartRecording)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(ChatEvent.CancelCountdown)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeDictation.stopCount)
        assertEquals(ComposerStatus.IDLE, vm.state.value.composerStatus)
    }
}
