package com.techtsunagu.wakatta.feature.insights

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrix
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class SkillsUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val matrix: SkillMatrix? = null,
    /** Language used to pick Machine names: the signed-in user's own. */
    val language: String = "ja",
    val selectedWorkerId: String? = null,
    val selectedMachineId: String? = null,
) {
    val isEmpty: Boolean get() = matrix != null && (matrix.rows.isEmpty() || matrix.machines.isEmpty())
}

sealed interface SkillsEvent {
    data object Retry : SkillsEvent
    data class SelectCell(val workerId: String, val machineId: String) : SkillsEvent
    data object DismissCell : SkillsEvent
}

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val repository: SkillMatrixRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<SkillsUiState, SkillsEvent, Unit>(SkillsUiState()) {

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onEvent(event: SkillsEvent) {
        when (event) {
            SkillsEvent.Retry -> load()
            is SkillsEvent.SelectCell ->
                updateState { it.copy(selectedWorkerId = event.workerId, selectedMachineId = event.machineId) }
            SkillsEvent.DismissCell -> updateState { it.copy(selectedWorkerId = null, selectedMachineId = null) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null, selectedWorkerId = null, selectedMachineId = null) }
            val language = sessionStore.session.value?.user?.language ?: "ja"
            when (val r = repository.getMatrix()) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = r.error, matrix = null) }
                is AppResult.Success -> updateState { it.copy(isLoading = false, matrix = r.value, language = language) }
            }
        }
    }
}
