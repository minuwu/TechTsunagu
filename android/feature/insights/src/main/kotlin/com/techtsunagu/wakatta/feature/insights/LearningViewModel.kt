package com.techtsunagu.wakatta.feature.insights

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.insights.domain.LearningHistory
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class LearningUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val history: LearningHistory? = null,
    /** The Foreman sees English hints and the Worker name; a Worker sees only their own history. */
    val isForeman: Boolean = false,
) {
    val isEmpty: Boolean get() = history != null && history.entries.isEmpty()
}

sealed interface LearningEvent {
    /** [userId] is the Worker the Foreman opened; a Worker always gets their own history. */
    data class Init(val userId: String?) : LearningEvent
    data object Retry : LearningEvent
}

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val repository: LearningRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<LearningUiState, LearningEvent, Unit>(LearningUiState()) {

    private var loadJob: Job? = null
    private var requestedUserId: String? = null
    private var started = false

    override fun onEvent(event: LearningEvent) {
        when (event) {
            is LearningEvent.Init -> if (!started || event.userId != requestedUserId) {
                started = true
                requestedUserId = event.userId
                load()
            }
            LearningEvent.Retry -> load()
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            val user = sessionStore.session.value?.user
            if (user == null) {
                updateState { it.copy(isLoading = false, error = AppError.SessionExpired, history = null) }
                return@launch
            }
            val isForeman = user.role == Role.FOREMAN
            val target = if (isForeman) requestedUserId ?: user.id else user.id
            when (val r = repository.getHistory(target)) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = r.error, history = null) }
                is AppResult.Success -> updateState { it.copy(isLoading = false, history = r.value, isForeman = isForeman) }
            }
        }
    }
}
