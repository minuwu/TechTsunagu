package com.techtsunagu.wakatta.feature.insights

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpsData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class FollowUpsUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val data: FollowUpsData? = null,
    val filter: FollowUpStatus = FollowUpStatus.OPEN,
    /** Follow-ups being marked clarified (their button waits). */
    val clarifying: Set<String> = emptySet(),
    /** A failed "Clarified" tap; the list stays. */
    val clarifyError: AppError? = null,
) {
    val isEmpty: Boolean get() = data != null && data.items.isEmpty()
}

sealed interface FollowUpsEvent {
    data object Retry : FollowUpsEvent
    data class SelectFilter(val status: FollowUpStatus) : FollowUpsEvent
    /** The Foreman explained the term (M10). */
    data class Clarify(val followUpId: String) : FollowUpsEvent
}

@HiltViewModel
class FollowUpsViewModel @Inject constructor(
    private val repository: FollowUpRepository,
) : MviViewModel<FollowUpsUiState, FollowUpsEvent, Unit>(FollowUpsUiState()) {

    private var loadJob: Job? = null

    init {
        load()
    }

    override fun onEvent(event: FollowUpsEvent) {
        when (event) {
            FollowUpsEvent.Retry -> load()
            is FollowUpsEvent.SelectFilter -> updateState { it.copy(filter = event.status) }
            is FollowUpsEvent.Clarify -> clarify(event.followUpId)
        }
    }

    private fun clarify(id: String) {
        if (id in state.value.clarifying) return
        updateState { it.copy(clarifying = it.clarifying + id, clarifyError = null) }
        viewModelScope.launch {
            val r = repository.clarify(id)
            updateState { s ->
                val done = s.copy(clarifying = s.clarifying - id)
                when (r) {
                    is AppResult.Success -> done.copy(
                        data = s.data?.copy(
                            items = s.data.items.map { if (it.id == id) it.copy(status = FollowUpStatus.CLARIFIED) else it },
                        ),
                    )
                    is AppResult.Failure -> done.copy(clarifyError = r.error.takeUnless { it is AppError.Conflict })
                }
            }
            // 409: the Worker already understood it; the list shows where it is now
            if (r is AppResult.Failure && r.error is AppError.Conflict) load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val r = repository.getFollowUps()) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = r.error, data = null) }
                is AppResult.Success -> updateState { it.copy(isLoading = false, data = r.value) }
            }
        }
    }
}
