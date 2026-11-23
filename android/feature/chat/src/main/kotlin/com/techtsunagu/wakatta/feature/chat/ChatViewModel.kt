package com.techtsunagu.wakatta.feature.chat

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.GlossaryTerm
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
    val composerStatus: ComposerStatus = ComposerStatus.IDLE,
    val inputText: String = "",
    val isListening: Boolean = false,
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
    data class AnswerQuestion(val messageId: String, val questionIndex: Int, val answerOptionIndex: Int) : ChatEvent
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
}

sealed interface ChatEffect {
    /** The screen turns the error into text from string resources. */
    data class ShowError(val error: AppError) : ChatEffect
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val audioRecorder: AudioRecorder,
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
    private val pendingAnswersMap = mutableMapOf<String, MutableMap<Int, Int>>()

    // One idempotency key per user action (rule R-5): kept until the call succeeds, reused on every retry.
    private var sendKey: String? = null
    private val answerKeys = mutableMapOf<String, Pair<List<Int>, String>>()

    // True while inputText came from dictation, so the sent message is a voice message.
    private var inputIsVoice = false

    init {
        loadGlossary()
        sessionStore.session
            .onEach { session -> setIdentity(session?.user?.id.orEmpty(), session?.user?.role ?: Role.UNKNOWN) }
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
            is ChatEvent.CancelCountdown -> {
                cancelCountdownTimer()
                updateState { it.copy(composerStatus = ComposerStatus.EDITING) }
            }
            is ChatEvent.SendNow -> {
                cancelCountdownTimer()
                doSend()
            }
            is ChatEvent.AnswerQuestion -> answerQuestion(event)
            is ChatEvent.SubmitAnswers -> submitAnswers(event.messageId, event.answers)
            is ChatEvent.NudgeWorker -> launchReported { chatRepository.nudge(event.messageId, event.workerId) }
            is ChatEvent.AskExplainAgain ->
                launchReported { chatRepository.askExplainAgain(event.messageId, state.value.currentUserId) }
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
                    val mapped = msgList.map { it.copy(isMine = it.fromUserId == myId) }
                    updateState {
                        it.copy(
                            threadId = threadId,
                            isLoading = false,
                            isEmpty = mapped.isEmpty(),
                            error = null,
                            messages = mapped,
                        )
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

    private fun onInputChanged(text: String) {
        cancelCountdownTimer()
        sendKey = null // a different text is a different message
        inputIsVoice = false
        updateState {
            it.copy(
                inputText = text,
                composerStatus = if (text.isNotEmpty()) ComposerStatus.EDITING else ComposerStatus.IDLE,
            )
        }
    }

    private fun startRecording() {
        cancelCountdownTimer()
        updateState { it.copy(composerStatus = ComposerStatus.RECORDING, isListening = true) }
        audioRecorder.start().launchIn(viewModelScope)
    }

    private fun stopRecording() {
        viewModelScope.launch {
            val audio = audioRecorder.stop()
            when (val result = chatRepository.transcribe(audio)) {
                is AppResult.Success -> {
                    sendKey = null
                    inputIsVoice = true
                    _countdown.value = CountdownState()
                    updateState {
                        it.copy(inputText = result.value, composerStatus = ComposerStatus.COUNTDOWN, isListening = false)
                    }
                    startCountdownTimer()
                }
                is AppResult.Failure -> {
                    updateState { it.copy(composerStatus = ComposerStatus.IDLE, isListening = false) }
                    sendEffect(ChatEffect.ShowError(result.error))
                }
            }
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
        val current = state.value
        val text = current.inputText.trim()
        if (text.isEmpty() || current.composerStatus == ComposerStatus.SENDING) return
        val key = sendKey ?: UUID.randomUUID().toString().also { sendKey = it }

        updateState { it.copy(composerStatus = ComposerStatus.SENDING) }
        viewModelScope.launch {
            val result = chatRepository.sendMessage(
                threadId = current.threadId,
                text = text,
                isVoice = inputIsVoice,
                fromUserId = current.currentUserId,
                idempotencyKey = key,
            )
            when (result) {
                is AppResult.Success -> {
                    sendKey = null
                    inputIsVoice = false
                    updateState { it.copy(inputText = "", composerStatus = ComposerStatus.IDLE) }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(composerStatus = ComposerStatus.EDITING) }
                    sendEffect(ChatEffect.ShowError(result.error))
                }
            }
        }
    }

    private fun answerQuestion(event: ChatEvent.AnswerQuestion) {
        val questionMap = pendingAnswersMap.getOrPut(event.messageId) { mutableMapOf() }
        questionMap[event.questionIndex] = event.answerOptionIndex

        val msg = state.value.messages.find { it.id == event.messageId }
        val totalQuestions = msg?.workerQuestions?.size ?: 1
        if (questionMap.size >= totalQuestions) {
            submitAnswers(event.messageId, (0 until totalQuestions).map { questionMap[it] ?: -1 })
        }
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
                pendingAnswersMap.remove(messageId)
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
