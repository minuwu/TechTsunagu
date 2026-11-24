package com.techtsunagu.wakatta.feature.machines

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.feature.machines.domain.NoteRepository
import com.techtsunagu.wakatta.feature.machines.domain.NoteStepDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteVersionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

enum class NotePhase {
    READ,
    QUIZ,
    PASS,
    FAIL,
    CONFLICT
}

data class NoteUiState(
    val machineId: String = "",
    val machineName: String = "",
    val phase: NotePhase = NotePhase.READ,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val userLanguage: String = "ja",
    val isSignedOff: Boolean = false,
    val currentVersion: NoteVersionDto? = null,
    val availableVersions: List<NoteVersionDto> = emptyList(),
    // Quiz state
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val answersList: MutableList<Int> = mutableListOf(),
    val wrongQuestionIndices: List<Int> = emptyList(),
    val wrongSteps: List<NoteStepDto> = emptyList(),
    val isSpeaking: Boolean = false,
)

sealed interface NoteEvent {
    data class Init(val machineId: String) : NoteEvent
    data object Refresh : NoteEvent
    data object Retry : NoteEvent
    data class SelectVersion(val versionId: String) : NoteEvent
    data object StartCheck : NoteEvent
    data class SelectOption(val optionIndex: Int) : NoteEvent
    data object NextQuestion : NoteEvent
    data object RetryCheck : NoteEvent
    data object ReturnToRead : NoteEvent
    data class SpeakText(val text: String) : NoteEvent
    data object StopSpeaking : NoteEvent
    data object ReloadAfterConflict : NoteEvent
}

sealed interface NoteEffect {
    data object CheckPassed : NoteEffect

    /** The Check answers did not reach the server; the Worker can send them again with the same key. */
    data object SubmitFailed : NoteEffect
}

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val checkRepository: CheckRepository,
    private val machineRepository: MachineRepository,
    private val signOffRepository: SignOffRepository,
    private val speaker: Speaker,
    private val sessionStore: SessionStore,
) : MviViewModel<NoteUiState, NoteEvent, NoteEffect>(NoteUiState(isLoading = true)) {

    // One idempotency key per Check attempt (rule R-5): kept until the server answers, reused when the send is retried.
    private var checkKey: String? = null

    override fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.Init -> {
                if (state.value.machineId != event.machineId) {
                    loadNote(event.machineId)
                }
            }
            is NoteEvent.Refresh, is NoteEvent.Retry -> {
                if (state.value.machineId.isNotEmpty()) {
                    loadNote(state.value.machineId)
                }
            }
            is NoteEvent.SelectVersion -> switchVersion(event.versionId)
            is NoteEvent.StartCheck -> startQuiz()
            is NoteEvent.SelectOption -> handleOptionSelect(event.optionIndex)
            is NoteEvent.NextQuestion -> handleNextQuestion()
            is NoteEvent.RetryCheck -> startQuiz()
            is NoteEvent.ReturnToRead -> updateState { it.copy(phase = NotePhase.READ) }
            is NoteEvent.SpeakText -> speak(event.text)
            is NoteEvent.StopSpeaking -> stopSpeaking()
            is NoteEvent.ReloadAfterConflict -> loadNote(state.value.machineId)
        }
    }

    fun loadNote(machineId: String) {
        viewModelScope.launch {
            updateState { it.copy(machineId = machineId, isLoading = true, error = null, phase = NotePhase.READ) }
            val session = sessionStore.session.value
            val isForeman = session?.user?.role == Role.FOREMAN
            val userId = session?.user?.id.orEmpty()
            val userLang = session?.user?.language ?: "ja"

            when (val res = noteRepository.getVersions(machineId)) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = res.error) }
                is AppResult.Success -> {
                    val versions = res.value
                    val machineName = (machineRepository.getMachine(machineId) as? AppResult.Success)
                        ?.value?.name?.get("ja") ?: machineId
                    val signOffs = (signOffRepository.getSignOffs(userId) as? AppResult.Success)?.value.orEmpty()
                    val isSigned = signOffs.any {
                        it.machineId == machineId && (it.status == SkillStatus.VALID || it.status == SkillStatus.EXPIRING)
                    }

                    updateState {
                        it.copy(
                            isLoading = false,
                            error = null,
                            machineId = machineId,
                            machineName = machineName,
                            isForeman = isForeman,
                            currentUserId = userId,
                            userLanguage = userLang,
                            isSignedOff = isSigned,
                            currentVersion = versions.firstOrNull(),
                            availableVersions = versions,
                            phase = NotePhase.READ,
                        )
                    }
                }
            }
        }
    }

    private fun switchVersion(versionId: String) {
        val selected = state.value.availableVersions.find { it.id == versionId || it.version == versionId } ?: return
        updateState { it.copy(currentVersion = selected) }
    }

    private fun startQuiz() {
        val qCount = state.value.currentVersion?.questions?.size ?: 0
        if (qCount == 0) return
        checkKey = null // a new attempt is a new user action
        updateState {
            it.copy(
                phase = NotePhase.QUIZ,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                answersList = mutableListOf(),
                wrongQuestionIndices = emptyList(),
                wrongSteps = emptyList()
            )
        }
    }

    // The Worker is never told right or wrong here: only the server grades (rule R-3), at the end.
    private fun handleOptionSelect(optionIndex: Int) {
        if (state.value.isAnswerSubmitted) return
        state.value.currentVersion?.questions?.getOrNull(state.value.currentQuestionIndex) ?: return
        val answers = state.value.answersList.toMutableList().apply { add(optionIndex) }
        updateState { it.copy(selectedOptionIndex = optionIndex, isAnswerSubmitted = true, answersList = answers) }
    }

    private fun handleNextQuestion() {
        val currentQIndex = state.value.currentQuestionIndex
        val totalQuestions = state.value.currentVersion?.questions?.size ?: 0

        if (currentQIndex < totalQuestions - 1) {
            updateState {
                it.copy(
                    currentQuestionIndex = currentQIndex + 1,
                    selectedOptionIndex = null,
                        isAnswerSubmitted = false
                )
            }
        } else {
            // Final question answered: submit check
            submitCheck()
        }
    }

    private fun submitCheck() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            // A Note Check targets the Note Version being read, not the Machine.
            val targetId = state.value.currentVersion?.id ?: state.value.machineId
            val lang = state.value.userLanguage
            val answers = state.value.answersList
            val idempotencyKey = checkKey ?: UUID.randomUUID().toString().also { checkKey = it }

            val res = checkRepository.submitCheck(
                targetKind = CheckTargetKind.NOTE,
                targetId = targetId,
                language = lang,
                answers = answers,
                idempotencyKey = idempotencyKey
            )

            when (res) {
                is AppResult.Success -> {
                    checkKey = null
                    val result = res.value
                    if (result.passed) {
                        updateState { it.copy(isLoading = false, phase = NotePhase.PASS, isSignedOff = true) }
                        sendEffect(NoteEffect.CheckPassed)
                    } else {
                        showFailed(result.wrongQuestions)
                    }
                }
                is AppResult.Failure -> {
                    if (res.error is AppError.Conflict) {
                        checkKey = null
                        updateState { it.copy(isLoading = false, phase = NotePhase.CONFLICT) }
                    } else {
                        // Never sign the Worker off locally: stay on the last question; "see result" sends again.
                        updateState { it.copy(isLoading = false) }
                        sendEffect(NoteEffect.SubmitFailed)
                    }
                }
            }
        }
    }

    /** The server names the wrong questions; the Worker is sent back to the steps they belong to. */
    private fun showFailed(wrongIds: List<String>) {
        val version = state.value.currentVersion
        val wrong = version?.questions.orEmpty().withIndex().filter { it.value.id in wrongIds }
        val steps = wrong.mapNotNull { (_, q) -> version?.steps?.find { it.index == q.stepIndex } }.distinct()
        updateState {
            it.copy(isLoading = false, phase = NotePhase.FAIL, wrongQuestionIndices = wrong.map { w -> w.index }, wrongSteps = steps)
        }
    }

    private fun speak(text: String) {
        speaker.speak(text, state.value.userLanguage)
        updateState { it.copy(isSpeaking = true) }
    }

    private fun stopSpeaking() {
        speaker.stop()
        updateState { it.copy(isSpeaking = false) }
    }
}
