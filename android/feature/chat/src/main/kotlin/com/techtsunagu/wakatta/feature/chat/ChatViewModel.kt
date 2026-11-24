package com.techtsunagu.wakatta.feature.chat

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.media.DictationAccumulator
import com.techtsunagu.wakatta.core.media.DictationErrorKind
import com.techtsunagu.wakatta.core.media.DictationEvent
import com.techtsunagu.wakatta.core.media.DictationSource
import com.techtsunagu.wakatta.core.media.dictationTag
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.feature.chat.data.ChatRepository
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import com.techtsunagu.wakatta.feature.chat.model.CountdownState
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDraft
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDialogState
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ChatUiState(
    val threadId: String = "",
    val title: String = "",
    val titleTri: Tri = emptyMap(),
    val membersCount: Int = 0,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: AppError? = null,
    val currentRole: Role = Role.UNKNOWN,
    val currentUserId: String = "",
    val canSwitchRole: Boolean = false,
    val messages: List<ChatMessageUiModel> = emptyList(),
    /** Fixed when the Thread opens: the divider sits above it and the list opens there (null: open at the bottom). */
    val firstUnreadId: String? = null,
    val unreadAtOpen: Int = 0,
    val composerStatus: ComposerStatus = ComposerStatus.IDLE,
    val inputText: String = "",
    val isListening: Boolean = false,
    val hasPendingAudio: Boolean = false,
    val selectedGlossaryTerm: GlossaryTerm? = null,
    val reexplainDialog: ReexplainDialogState? = null,
    val glossaryTerms: List<GlossaryTerm> = emptyList(),
)

sealed interface ChatEvent {
    data class Init(val threadId: String) : ChatEvent
    data object Retry : ChatEvent
    data object Refresh : ChatEvent
    data object ToggleRole : ChatEvent
    data class ToggleShowOriginal(val messageId: String) : ChatEvent
    data class PlayVoice(val text: String, val lang: String) : ChatEvent
    data class OnInputTextChanged(val text: String) : ChatEvent
    data object StartRecording : ChatEvent
    data object StopRecording : ChatEvent
    data object CancelCountdown : ChatEvent
    data object SendNow : ChatEvent
    data class SubmitAnswers(val messageId: String, val answers: List<Int>) : ChatEvent
    data class ToggleWkFold(val messageId: String) : ChatEvent
    data class NudgeWorker(val messageId: String, val workerId: String) : ChatEvent
    data class OpenReexplain(val messageId: String, val workerId: String) : ChatEvent

    /** [explanation] holds the text in the Worker's own language (and any other languages already filled). */
    data class UpdateReexplainText(val easyJa: String, val explanation: Tri) : ChatEvent
    data class SubmitReexplain(val messageId: String) : ChatEvent
    data object DismissReexplain : ChatEvent
    data class SelectGlossaryTerm(val term: GlossaryTerm) : ChatEvent
    data object DismissGlossaryTerm : ChatEvent
    data class AskExplainAgain(val messageId: String) : ChatEvent
    data class EditDraft(val messageId: String, val textJa: String) : ChatEvent
}

sealed interface ChatEffect {
    /** The screen turns the error into text from string resources. */
    data class ShowError(val error: AppError) : ChatEffect
    data class ShowHint(@StringRes val messageRes: Int) : ChatEffect
}

private const val ASK_TERM_MAX = 100

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val dictation: DictationSource,
    private val recorder: com.techtsunagu.wakatta.core.media.VoiceRecorder,
    private val speaker: Speaker,
    private val sessionStore: SessionStore,
    appConfig: AppConfig,
) : MviViewModel<ChatUiState, ChatEvent, ChatEffect>(
    ChatUiState(
        isLoading = true,
        canSwitchRole = appConfig.isDebug && chatRepository.debugUserIds.isNotEmpty(),
    ),
) {

    // The Auto-send Countdown ticks ten times a second: keep it out of ChatUiState so the message list is not re-emitted.
    private val _countdown = MutableStateFlow(CountdownState())
    val countdown: StateFlow<CountdownState> = _countdown.asStateFlow()

    private var countdownJob: Job? = null
    private var messagesJob: Job? = null
    private var dictationJob: Job? = null

    // One idempotency key per user action (rule R-5): kept until the call succeeds, reused on every retry.
    private var sendKey: String? = null
    private val answerKeys = mutableMapOf<String, Pair<List<Int>, String>>()

    // True while inputText came from dictation, so the sent message is a voice message.
    private var inputIsVoice = false

    /** Optimistic bubbles shown at once; each leaves when the poll returns its id or its send fails. */
    private var pending = listOf<ChatMessageUiModel>()
    private var openUnread: Int? = 0 // null once the first page has placed the divider
    private var server = listOf<ChatMessageUiModel>()

    private fun shown(): List<ChatMessageUiModel> {
        pending = pending.filter { p -> server.none { it.id == p.id } }
        return server + pending
    }
    private var currentUserLang: String = ""
    private val accumulator = DictationAccumulator()

    init {
        loadGlossary()
        sessionStore.session
            .onEach { session ->
                currentUserLang = session?.user?.language.orEmpty()
                setIdentity(session?.user?.id.orEmpty(), session?.user?.role ?: Role.UNKNOWN)
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Init -> {
                updateState { it.copy(threadId = event.threadId, isLoading = true, error = null) }
                loadThread(event.threadId)
            }
            is ChatEvent.Retry -> {
                updateState { it.copy(isLoading = true, error = null) }
                loadThread(state.value.threadId)
            }
            is ChatEvent.Refresh -> {
                updateState { it.copy(isLoading = true) }
                loadThread(state.value.threadId)
            }
            is ChatEvent.ToggleRole -> toggleRole()
            is ChatEvent.ToggleShowOriginal -> updateMessage(event.messageId) { it.copy(showOriginal = !it.showOriginal) }
            is ChatEvent.ToggleWkFold -> updateMessage(event.messageId) { it.copy(isWkFoldExpanded = !it.isWkFoldExpanded) }
            is ChatEvent.PlayVoice -> speaker.speak(event.text, event.lang)
            is ChatEvent.SelectGlossaryTerm -> updateState { it.copy(selectedGlossaryTerm = event.term) }
            is ChatEvent.DismissGlossaryTerm -> updateState { it.copy(selectedGlossaryTerm = null) }
            is ChatEvent.OnInputTextChanged -> onInputChanged(event.text)
            is ChatEvent.StartRecording -> startRecording()
            is ChatEvent.StopRecording -> stopRecording()
            is ChatEvent.CancelCountdown -> cancelCountdown()
            is ChatEvent.SendNow -> {
                cancelCountdownTimer()
                doSend()
            }
            is ChatEvent.SubmitAnswers -> submitAnswers(event.messageId, event.answers)
            is ChatEvent.NudgeWorker -> launchReported { chatRepository.nudge(event.messageId, event.workerId) }
            is ChatEvent.AskExplainAgain -> {
                val text = state.value.messages.find { it.id == event.messageId }?.originalText.orEmpty()
                // The Term in the message; without one the Foreman sees the message itself. The server takes 100 characters.
                val termJa = state.value.glossaryTerms.map { it.ja }.filter { it.isNotBlank() && it in text }
                    .maxByOrNull { it.length } ?: text.take(ASK_TERM_MAX)
                launchReported { chatRepository.askExplainAgain(event.messageId, state.value.currentUserId, termJa) }
            }
            is ChatEvent.EditDraft -> {
                viewModelScope.launch {
                    val result = chatRepository.editDraft(event.messageId, event.textJa)
                    when (result) {
                        is AppResult.Success -> updateMessage(event.messageId) { result.value }
                        is AppResult.Failure -> {
                            report(result)
                            if ((result.error as? AppError.Conflict)?.code == "ALREADY_SENT") {
                                loadThread(state.value.threadId)
                            }
                        }
                    }
                }
            }
            is ChatEvent.OpenReexplain -> openReexplain(event.messageId, event.workerId)
            is ChatEvent.UpdateReexplainText -> updateState { s ->
                s.copy(
                    reexplainDialog = s.reexplainDialog?.copy(
                        easyJaText = event.easyJa,
                        explanation = event.explanation,
                    ),
                )
            }
            is ChatEvent.SubmitReexplain -> submitReexplain(event.messageId)
            is ChatEvent.DismissReexplain -> updateState { it.copy(reexplainDialog = null) }
        }
    }

    private fun setIdentity(userId: String, role: Role) {
        updateState { s ->
            s.copy(
                currentUserId = userId,
                currentRole = role,
                messages = s.messages.map { it.copy(isMine = it.fromUserId == userId) },
            )
        }
    }

    // Debug only: acts as the other demo user. The ids come from the repository, which is empty outside the Fake.
    private fun toggleRole() {
        if (!state.value.canSwitchRole) return
        val newRole = if (state.value.currentRole == Role.FOREMAN) Role.WORKER else Role.FOREMAN
        val newUserId = chatRepository.debugUserIds[newRole] ?: return
        setIdentity(newUserId, newRole)
    }

    private fun updateMessage(messageId: String, change: (ChatMessageUiModel) -> ChatMessageUiModel) {
        updateState { s -> s.copy(messages = s.messages.map { if (it.id == messageId) change(it) else it }) }
    }

    private fun loadThread(threadId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            loadTitle(threadId)
            chatRepository.getMessages(threadId)
                .catch { updateState { it.copy(isLoading = false, error = AppError.Unknown()) } }
                .collect { msgList ->
                    val myId = state.value.currentUserId
                    server = msgList.map { it.copy(isMine = it.fromUserId == myId) }
                    val all = shown()
                    // first emission only: the oldest of the last `unread` messages from other people
                    val n = openUnread
                    openUnread = null
                    val anchor = if (n == null || n <= 0) null else server.filter { !it.isMine }.takeLast(n).firstOrNull()?.id
                    updateState {
                        it.copy(
                            threadId = threadId,
                            isLoading = false,
                            isEmpty = all.isEmpty(),
                            error = null,
                            messages = all,
                        ).let { s -> if (anchor != null) s.copy(firstUnreadId = anchor, unreadAtOpen = n ?: 0) else s }
                    }
                }
        }
    }

    private suspend fun loadTitle(threadId: String) {
        val thread = chatRepository.getThreads().firstOrNull()?.firstOrNull { it.id == threadId } ?: return
        val titleTri = buildMap {
            put("ja", thread.titleJa)
            thread.titleEn?.let { put("en", it) }
        }
        openUnread = thread.unread
        updateState { it.copy(title = thread.titleJa, titleTri = titleTri, membersCount = thread.memberIds.size) }
    }

    private fun loadGlossary() {
        viewModelScope.launch {
            val res = chatRepository.getTerms()
            if (res is AppResult.Success) {
                updateState { it.copy(glossaryTerms = res.value) }
            }
        }
    }

    private fun currentDictationTag(): String {
        val lang = if (state.value.currentRole == Role.FOREMAN) {
            currentUserLang.ifEmpty { "ja" }
        } else {
            currentUserLang.ifEmpty { "ja" }
        }
        return dictationTag(lang)
    }

    private fun onInputChanged(text: String) {
        cancelCountdownTimer()
        dictationJob?.cancel()
        dictationJob = null
        sendKey = null // a different text is a different message
        inputIsVoice = false
        accumulator.reset(text)
        updateState {
            it.copy(
                inputText = text,
                hasPendingAudio = false,
                composerStatus = if (text.isNotEmpty()) ComposerStatus.EDITING else ComposerStatus.IDLE,
            )
        }
    }

    private fun startRecording() {
        cancelCountdownTimer()
        dictationJob?.cancel()
        dictationJob = null
        sendKey = null

        if (serverDictation || !dictation.isAvailable()) {
            startServerRecording()
            return
        }

        accumulator.reset(state.value.inputText)
        updateState {
            it.copy(
                composerStatus = ComposerStatus.RECORDING,
                isListening = true,
                hasPendingAudio = false,
            )
        }

        val tag = currentDictationTag()
        dictationJob = viewModelScope.launch {
            dictation.start(tag).collect { event ->
                when (event) {
                    is DictationEvent.Listening -> {
                        updateState { it.copy(isListening = true, composerStatus = ComposerStatus.RECORDING) }
                    }
                    is DictationEvent.Partial -> {
                        val text = accumulator.onPartial(event.text)
                        updateState {
                            it.copy(
                                inputText = text,
                                composerStatus = ComposerStatus.RECORDING,
                                isListening = true,
                            )
                        }
                    }
                    is DictationEvent.Final -> {
                        val text = accumulator.onFinal(event.text)
                        inputIsVoice = true
                        updateState {
                            it.copy(
                                inputText = text,
                                composerStatus = ComposerStatus.COUNTDOWN,
                                isListening = false,
                            )
                        }
                        startCountdownTimer()
                    }
                    is DictationEvent.Error -> {
                        handleDictationError(event.kind)
                    }
                }
            }
        }
    }

    private fun handleDictationError(kind: DictationErrorKind) {
        dictationJob?.cancel()
        dictationJob = null
        when (kind) {
            DictationErrorKind.NO_SPEECH -> {
                updateState {
                    it.copy(
                        composerStatus = ComposerStatus.IDLE,
                        isListening = false,
                    )
                }
                sendEffect(ChatEffect.ShowHint(R.string.chat_dictation_no_speech))
            }
            DictationErrorKind.NETWORK, DictationErrorKind.UNAVAILABLE, DictationErrorKind.BUSY, DictationErrorKind.OTHER -> {
                // the phone's recognizer cannot do it: record and let the server transcribe, from now on
                serverDictation = true
                startServerRecording()
            }
            DictationErrorKind.NO_PERMISSION -> {
                updateState {
                    it.copy(
                        composerStatus = ComposerStatus.EDITING,
                        isListening = false,
                    )
                }
                sendEffect(ChatEffect.ShowHint(R.string.chat_dictation_type_instead))
            }
        }
    }

    private fun stopRecording() {
        if (!serverRecording) {
            dictation.stop()
            updateState { it.copy(isListening = false) }
            return
        }
        serverRecording = false
        val file = recorder.stop()
        if (file == null) {
            updateState { it.copy(composerStatus = ComposerStatus.IDLE, isListening = false) }
            sendEffect(ChatEffect.ShowHint(R.string.chat_dictation_no_speech))
            return
        }
        updateState { it.copy(composerStatus = ComposerStatus.SENDING, isListening = false) }
        dictationJob = viewModelScope.launch {
            when (val r = chatRepository.transcribe(file, currentDictationTag().substringBefore('-'))) {
                is AppResult.Success -> {
                    val text = accumulator.onFinal(r.value)
                    inputIsVoice = true
                    updateState { it.copy(inputText = text, composerStatus = ComposerStatus.COUNTDOWN) }
                    startCountdownTimer()
                }
                is AppResult.Failure -> {
                    updateState { it.copy(composerStatus = ComposerStatus.EDITING) }
                    sendEffect(ChatEffect.ShowHint(R.string.chat_dictation_type_instead))
                }
            }
        }
    }

    /** Set once the phone's recognizer failed: later taps go straight to the server. */
    private var serverDictation = false
    private var serverRecording = false

    private fun startServerRecording() {
        accumulator.reset(state.value.inputText)
        if (!recorder.start()) {
            updateState { it.copy(composerStatus = ComposerStatus.EDITING, isListening = false) }
            sendEffect(ChatEffect.ShowHint(R.string.chat_dictation_type_instead))
            return
        }
        serverRecording = true
        updateState { it.copy(composerStatus = ComposerStatus.RECORDING, isListening = true, hasPendingAudio = false) }
    }

    private fun cancelCountdown() {
        cancelCountdownTimer()
        dictationJob?.cancel()
        dictationJob = null
        if (state.value.composerStatus == ComposerStatus.COUNTDOWN) {
            updateState { it.copy(composerStatus = ComposerStatus.EDITING) }
        } else {
            updateState { it.copy(composerStatus = ComposerStatus.IDLE, hasPendingAudio = false) }
        }
    }

    private fun startCountdownTimer() {
        cancelCountdownTimer()
        countdownJob = viewModelScope.launch {
            val totalSteps = 40
            for (step in totalSteps downTo 0) {
                _countdown.value = CountdownState(
                    seconds = (step + 9) / 10,
                    progress = step.toFloat() / totalSteps.toFloat(),
                )
                delay(100)
            }
            doSend()
        }
    }

    private fun cancelCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun doSend() {
        cancelCountdownTimer()
        val current = state.value
        val textToSend = current.inputText.trim()
        if (textToSend.isEmpty()) return

        val isVoice = inputIsVoice
        val key = sendKey ?: UUID.randomUUID().toString()
        val isForeman = current.currentRole == Role.FOREMAN
        val local = ChatMessageUiModel(
            id = "local-$key",
            threadId = current.threadId,
            fromUserId = current.currentUserId,
            fromUserName = "",
            isMine = true,
            kind = MessageKind.TEXT,
            text = mapOf("ja" to textToSend),
            originalText = textToSend,
            translatedText = textToSend,
            time = java.time.LocalTime.now().toString().take(5),
            stage = if (isForeman) 1 else 5, // the Foreman's pill shows "Translating…" until the server answers
            isVoice = isVoice,
            hasCheck = isForeman,
        )
        // Optimistic: the bubble shows and the composer frees at once; the AI draft and send run behind it.
        pending = pending + local
        sendKey = null
        inputIsVoice = false
        accumulator.clear()
        updateState { it.copy(inputText = "", composerStatus = ComposerStatus.IDLE, hasPendingAudio = false, isEmpty = false, messages = shown()) }
        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                threadId = current.threadId,
                text = textToSend,
                audio = null,
                isVoice = isVoice,
                fromUserId = current.currentUserId,
                idempotencyKey = key,
            )
            pending = when (result) {
                // the real message stands in until the poll brings it (same id, so no duplicate)
                is AppResult.Success -> pending.map { if (it.id == local.id) result.value.copy(isMine = true) else it }
                is AppResult.Failure -> pending - local
            }
            updateState { it.copy(messages = shown()) }
            if (result is AppResult.Failure) {
                // give the words back, same key so a retry cannot post twice
                if (state.value.inputText.isEmpty()) {
                    sendKey = key
                    inputIsVoice = isVoice
                    accumulator.reset(textToSend)
                    updateState { it.copy(inputText = textToSend, composerStatus = ComposerStatus.EDITING) }
                }
                sendEffect(ChatEffect.ShowError(result.error))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dictationJob?.cancel()
        recorder.cancel()
    }

    private fun submitAnswers(messageId: String, answers: List<Int>) {
        // Same answers reuse the key; changed answers are a new action.
        val key = answerKeys[messageId]?.takeIf { it.first == answers }?.second
            ?: UUID.randomUUID().toString().also { answerKeys[messageId] = answers to it }
        viewModelScope.launch {
            val result = chatRepository.submitAnswers(
                messageId = messageId,
                userId = state.value.currentUserId,
                answers = answers,
                idempotencyKey = key,
            )
            if (result.isSuccess) {
                answerKeys.remove(messageId)
            } else {
                report(result)
            }
        }
    }

    private fun openReexplain(messageId: String, workerId: String) {
        viewModelScope.launch {
            when (val result = chatRepository.getReexplainDraft(messageId, workerId)) {
                is AppResult.Success ->
                    updateState { it.copy(reexplainDialog = result.value.toDialog(messageId, workerId)) }
                is AppResult.Failure -> sendEffect(ChatEffect.ShowError(result.error))
            }
        }
    }

    private fun submitReexplain(messageId: String) {
        val dialog = state.value.reexplainDialog ?: return
        viewModelScope.launch {
            val request = ReexplainRequest(
                userId = dialog.workerId,
                termJa = dialog.confusingTerm,
                easyJa = dialog.easyJaText,
                explanation = dialog.explanation,
                newQuestionPrompt = dialog.newQuestionPrompt,
                newQuestionOptions = dialog.newQuestionOptions,
                newQuestionCorrectIndex = dialog.correctOptionIndex,
            )
            val result = chatRepository.reexplain(messageId, request)
            if (result.isSuccess) updateState { it.copy(reexplainDialog = null) } else report(result)
        }
    }

    private fun launchReported(call: suspend () -> AppResult<*>) {
        viewModelScope.launch { report(call()) }
    }

    private fun report(result: AppResult<*>) {
        (result as? AppResult.Failure)?.let { sendEffect(ChatEffect.ShowError(it.error)) }
    }

    private fun ReexplainDraft.toDialog(messageId: String, workerId: String) = ReexplainDialogState(
        messageId = messageId,
        workerId = workerId,
        workerName = workerName,
        workerLang = workerLang,
        confusingTerm = termJa,
        easyJaText = easyJa,
        explanation = explanation,
        newQuestionPrompt = newQuestionPrompt,
        newQuestionOptions = newQuestionOptions,
        correctOptionIndex = newQuestionCorrectIndex,
    )
}
