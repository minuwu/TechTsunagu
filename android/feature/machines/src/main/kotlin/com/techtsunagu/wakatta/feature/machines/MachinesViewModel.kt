package com.techtsunagu.wakatta.feature.machines

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

data class MachineWorkerChipState(
    val workerId: String,
    val displayName: String,
    val isReady: Boolean,
)

data class MachineItemUiState(
    val id: String,
    val code: String,
    val nameJa: String,
    val nameTranslated: String,
    val isReady: Boolean,
    val status: SkillStatus,
    val lockedWorkerCount: Int,
    val workerStatuses: List<MachineWorkerChipState> = emptyList(),
    val name: Map<String, String> = emptyMap(),
)

/** The Foreman's add / rename / archive dialog; null = closed. */
sealed interface MachineDialog {
    data object Add : MachineDialog
    data class Rename(val machine: MachineItemUiState) : MachineDialog
    data class Archive(val machine: MachineItemUiState) : MachineDialog
}

data class MachinesUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: AppError? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val machines: List<MachineItemUiState> = emptyList(),
    val dialog: MachineDialog? = null,
    val isSaving: Boolean = false,
    val saveError: AppError? = null,
)

sealed interface MachinesEvent {
    data object Retry : MachinesEvent
    data object Refresh : MachinesEvent
    data class MachineClicked(val machineId: String) : MachinesEvent
    data class TakeCheckClicked(val machineId: String) : MachinesEvent
    data class OpenDialog(val dialog: MachineDialog?) : MachinesEvent
    data class Add(val code: String, val nameJa: String, val nameEn: String) : MachinesEvent
    data class Rename(val id: String, val nameJa: String, val nameEn: String) : MachinesEvent
    data class Archive(val id: String) : MachinesEvent
}

sealed interface MachinesEffect {
    data class NavigateToMachine(val machineId: String) : MachinesEffect
    data class NavigateToCheck(val machineId: String) : MachinesEffect
}

@HiltViewModel
class MachinesViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    private val signOffRepository: SignOffRepository,
    private val workerRepository: WorkerRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<MachinesUiState, MachinesEvent, MachinesEffect>(MachinesUiState(isLoading = true)) {

    init {
        loadMachines()
    }

    override fun onEvent(event: MachinesEvent) {
        when (event) {
            is MachinesEvent.Retry -> loadMachines(isRefresh = false)
            is MachinesEvent.Refresh -> loadMachines(isRefresh = true)
            is MachinesEvent.MachineClicked -> sendEffect(MachinesEffect.NavigateToMachine(event.machineId))
            is MachinesEvent.TakeCheckClicked -> sendEffect(MachinesEffect.NavigateToCheck(event.machineId))
            is MachinesEvent.OpenDialog -> updateState { it.copy(dialog = event.dialog, saveError = null) }
            is MachinesEvent.Add -> save { machineRepository.addMachine(event.code, tri(event.nameJa, event.nameEn)) }
            is MachinesEvent.Rename -> save { machineRepository.renameMachine(event.id, tri(event.nameJa, event.nameEn)) }
            is MachinesEvent.Archive -> save { machineRepository.archiveMachine(event.id) }
        }
    }

    private fun tri(ja: String, en: String) = mapOf("ja" to ja.trim(), "en" to en.trim().ifEmpty { ja.trim() })

    /** Runs one Foreman change; on success the dialog closes and the list reloads. */
    private fun save(call: suspend () -> AppResult<*>) {
        if (state.value.isSaving) return
        viewModelScope.launch {
            updateState { it.copy(isSaving = true, saveError = null) }
            when (val r = call()) {
                is AppResult.Success -> {
                    updateState { it.copy(isSaving = false, dialog = null) }
                    loadMachines(isRefresh = true)
                }
                is AppResult.Failure -> updateState { it.copy(isSaving = false, saveError = r.error) }
            }
        }
    }

    fun loadMachines(isRefresh: Boolean = false) {
        viewModelScope.launch {
            updateState {
                if (isRefresh && it.machines.isNotEmpty()) {
                    it.copy(error = null)
                } else {
                    it.copy(isLoading = true, error = null)
                }
            }
            val session = sessionStore.session.value
            val isForeman = session?.user?.role == Role.FOREMAN
            val userId = session?.user?.id.orEmpty()
            val userLang = session?.user?.language ?: "ja"

            when (val result = machineRepository.getMachines()) {
                is AppResult.Failure -> updateState {
                    it.copy(
                        isLoading = false,
                        error = if (isRefresh && it.machines.isNotEmpty()) null else result.error
                    )
                }
                is AppResult.Success -> {
                    val machineList = result.value
                    if (machineList.isEmpty()) {
                        updateState { it.copy(isLoading = false, isEmpty = true, isForeman = isForeman, machines = emptyList()) }
                        return@launch
                    }

                    // A Foreman has no Skill Passport of their own (404), so only a Worker's list reads one.
                    val signOffs = if (isForeman) emptyList() else (signOffRepository.getSignOffs(userId) as? AppResult.Success)?.value.orEmpty()
                    // Only the Foreman's list shows every Worker; a Worker's list has no Worker chips.
                    val workerSignOffs = if (isForeman) loadWorkerSignOffs(workerRepository, signOffRepository) else emptyList()

                    val items = machineList.map { m ->
                        val signOff = signOffs.find { it.machineId == m.id }
                        val isReadyForUser = if (isForeman) m.isReady else (signOff != null && signOff.status != SkillStatus.EXPIRED)
                        val skillStatus = signOff?.status ?: if (m.isReady) SkillStatus.VALID else SkillStatus.NONE
                        val workerChips = workerSignOffs.map { w ->
                            MachineWorkerChipState(w.worker.id, w.worker.displayName, w.isReadyOn(m.id))
                        }

                        MachineItemUiState(
                            id = m.id,
                            code = m.code,
                            nameJa = m.name["ja"] ?: m.code,
                            nameTranslated = m.name[userLang] ?: m.name["en"] ?: m.name["ja"] ?: m.code,
                            isReady = isReadyForUser,
                            status = skillStatus,
                            lockedWorkerCount = workerChips.count { !it.isReady },
                            workerStatuses = workerChips,
                            name = m.name,
                        )
                    }

                    updateState {
                        it.copy(
                            isLoading = false,
                            isEmpty = false,
                            error = null,
                            isForeman = isForeman,
                            currentUserId = userId,
                            machines = items,
                        )
                    }
                }
            }
        }
    }
}
