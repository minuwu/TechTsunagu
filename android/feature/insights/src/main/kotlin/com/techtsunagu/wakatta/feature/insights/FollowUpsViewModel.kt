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
) {
    val isEmpty: Boolean get() = data != null && data.items.isEmpty()
}

sealed interface FollowUpsEvent {
    data object Retry : FollowUpsEvent
    data class SelectFilter(val status: FollowUpStatus) : FollowUpsEvent
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
