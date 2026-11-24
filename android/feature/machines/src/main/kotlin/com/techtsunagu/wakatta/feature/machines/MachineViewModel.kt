package com.techtsunagu.wakatta.feature.machines

import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Shift
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.feature.machines.domain.MachineTaskSummary
import com.techtsunagu.wakatta.feature.machines.domain.ShiftAssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class MachineWorkerStatusUiState(
    val workerId: String,
    val displayName: String,
    val isCurrentUser: Boolean,
    val isReady: Boolean,
    val skillStatus: SkillStatus,
)

data class WorkerOptionUiState(
    val id: String,
    val displayName: String,
)

data class MachineUiState(
    val machineId: String = "",
    val code: String = "",
    val nameJa: String = "",
    val nameTranslated: String = "",
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: AppError? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val isReady: Boolean = false,
    val skillStatus: SkillStatus = SkillStatus.NONE,
    val workers: List<MachineWorkerStatusUiState> = emptyList(),
    val assignedWorkerId: String? = null,
    val assignedWorkerName: String? = null,
    val availableWorkers: List<WorkerOptionUiState> = emptyList(),
    val showAssignDialog: Boolean = false,
    val qrCodePayload: String = "",
    val tasks: List<MachineTaskSummary> = emptyList(),
    val currentVersion: String = "",
    @StringRes val qrPrintedToastRes: Int? = null,
)

sealed interface MachineEvent {
    data class Init(val machineId: String) : MachineEvent
    data object Refresh : MachineEvent
    data object Retry : MachineEvent
    data object OpenPanel : MachineEvent
    data object OpenNote : MachineEvent
    data object OpenAssignDialog : MachineEvent
    data object DismissAssignDialog : MachineEvent
    data class AssignWorker(val workerId: String?) : MachineEvent
    data object PrintQrClicked : MachineEvent
    data object DismissToast : MachineEvent
}

sealed interface MachineEffect {
    data class NavigateToPanel(val machineId: String) : MachineEffect
    data class NavigateToNote(val machineId: String) : MachineEffect
}

@HiltViewModel
class MachineViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    private val signOffRepository: SignOffRepository,
    private val shiftAssignmentRepository: ShiftAssignmentRepository,
    private val workerRepository: WorkerRepository,
    private val taskRepository: TaskRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<MachineUiState, MachineEvent, MachineEffect>(MachineUiState(isLoading = true)) {

    override fun onEvent(event: MachineEvent) {
        when (event) {
            is MachineEvent.Init -> {
                if (state.value.machineId != event.machineId) {
                    loadMachine(event.machineId)
                }
            }
            is MachineEvent.Refresh -> {
                if (state.value.machineId.isNotEmpty()) {
                    loadMachine(state.value.machineId, isRefresh = true)
                }
            }
            is MachineEvent.Retry -> {
                if (state.value.machineId.isNotEmpty()) {
                    loadMachine(state.value.machineId, isRefresh = false)
                }
            }
            is MachineEvent.OpenPanel -> sendEffect(MachineEffect.NavigateToPanel(state.value.machineId))
            is MachineEvent.OpenNote -> sendEffect(MachineEffect.NavigateToNote(state.value.machineId))
            is MachineEvent.OpenAssignDialog -> updateState { it.copy(showAssignDialog = true) }
            is MachineEvent.DismissAssignDialog -> updateState { it.copy(showAssignDialog = false) }
            is MachineEvent.AssignWorker -> updateShiftAssignment(event.workerId)
            is MachineEvent.PrintQrClicked -> {
                updateState { it.copy(qrPrintedToastRes = R.string.machines_qr_printed_toast) }
            }
            is MachineEvent.DismissToast -> updateState { it.copy(qrPrintedToastRes = null) }
        }
    }

    fun loadMachine(machineId: String, isRefresh: Boolean = false) {
        viewModelScope.launch {
            updateState {
                if (isRefresh && it.code.isNotEmpty()) {
                    it.copy(machineId = machineId, error = null)
                } else {
                    it.copy(machineId = machineId, isLoading = true, error = null)
                }
            }
            val session = sessionStore.session.value
            val isForeman = session?.user?.role == Role.FOREMAN
            val userId = session?.user?.id.orEmpty()
            val userLang = session?.user?.language ?: "ja"

            when (val res = machineRepository.getMachine(machineId)) {
                is AppResult.Failure -> updateState {
                    it.copy(
                        isLoading = false,
                        error = if (isRefresh && it.code.isNotEmpty()) null else res.error
                    )
                }
                is AppResult.Success -> {
                    val detail = res.value
                    val signOffs = (signOffRepository.getSignOffs(userId) as? AppResult.Success)?.value.orEmpty()
                    val userSkill = signOffs.find { it.machineId == machineId }?.status ?: SkillStatus.NONE
                    val userReady = isForeman || userSkill == SkillStatus.VALID || userSkill == SkillStatus.EXPIRING

                    val workerSignOffs = loadWorkerSignOffs(workerRepository, signOffRepository)
                    val assignment = (shiftAssignmentRepository.getAssignments() as? AppResult.Success)
                        ?.value?.find { it.machineId == machineId }
                    val tasks = (taskRepository.getTasks() as? AppResult.Success)?.value.orEmpty()
                        .filter { it.machineId == machineId }
                        .map { MachineTaskSummary(id = it.id, title = it.title, dueText = it.dueAt.orEmpty()) }

                    updateState {
                        it.copy(
                            isLoading = false,
                            isEmpty = false,
                            error = null,
                            machineId = detail.id,
                            code = detail.code,
                            nameJa = detail.name["ja"] ?: detail.code,
                            nameTranslated = detail.name[userLang] ?: detail.name["en"] ?: detail.name["ja"] ?: detail.code,
                            isForeman = isForeman,
                            currentUserId = userId,
                            isReady = userReady,
                            skillStatus = userSkill,
                            workers = workerSignOffs.map { w ->
                                MachineWorkerStatusUiState(
                                    workerId = w.worker.id,
                                    displayName = w.worker.displayName,
                                    isCurrentUser = w.worker.id == userId,
                                    isReady = w.isReadyOn(machineId),
                                    skillStatus = w.statusOn(machineId),
                                )
                            },
                            assignedWorkerId = assignment?.workerId,
                            assignedWorkerName = assignment?.workerName,
                            availableWorkers = workerSignOffs.map { w -> WorkerOptionUiState(w.worker.id, w.worker.displayName) },
                            qrCodePayload = "https://$STICKER_HOST/m/${detail.id}?s=${session?.user?.shopId.orEmpty()}",
                            tasks = tasks,
                            currentVersion = detail.currentVersionId.orEmpty(),
                        )
                    }
                }
            }
        }
    }

    private fun updateShiftAssignment(workerId: String?) {
        viewModelScope.launch {
            shiftAssignmentRepository.assign(
                machineId = state.value.machineId,
                shift = Shift.DAY,
                date = null, // the server takes today
                workerId = workerId
            )
            updateState {
                it.copy(
                    assignedWorkerId = workerId,
                    assignedWorkerName = it.availableWorkers.find { w -> w.id == workerId }?.displayName,
                    showAssignDialog = false
                )
            }
        }
    }

    private companion object {
        // shortcut: the sticker host belongs in ApiConfig (REQUESTED CHANGES), kept here until it is there.
        const val STICKER_HOST = "app.wakatta.com"
    }
}
