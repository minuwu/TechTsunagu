package com.techtsunagu.wakatta.feature.insights

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsData
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val data: AnalyticsData? = null,
    val selectedPoint: Int = 0,
    val selectedTerm: String? = null,
    val selectedWorkerId: String? = null,
    /** Term whose "add to glossary" call is running. */
    val pinningTerm: String? = null,
    val pinError: AppError? = null,
) {
    val isEmpty: Boolean get() = data != null && data.sent == 0 && data.terms.isEmpty()
}

sealed interface AnalyticsEvent {
    data object Retry : AnalyticsEvent
    data class SelectPoint(val index: Int) : AnalyticsEvent
    data class SelectCell(val term: String, val workerId: String) : AnalyticsEvent
    data class PinTerm(val term: String) : AnalyticsEvent
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: AnalyticsRepository,
) : MviViewModel<AnalyticsUiState, AnalyticsEvent, Unit>(AnalyticsUiState()) {

    private var loadJob: Job? = null
    private var pinJob: Job? = null

    init {
        load()
    }

    override fun onEvent(event: AnalyticsEvent) {
        when (event) {
            AnalyticsEvent.Retry -> load()
            is AnalyticsEvent.SelectPoint -> updateState { it.copy(selectedPoint = event.index) }
            is AnalyticsEvent.SelectCell ->
                updateState { it.copy(selectedTerm = event.term, selectedWorkerId = event.workerId) }
            is AnalyticsEvent.PinTerm -> pin(event.term)
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null, pinError = null) }
            when (val r = repository.getAnalytics()) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = r.error, data = null) }
                is AppResult.Success -> updateState {
                    it.copy(isLoading = false, data = r.value, selectedPoint = r.value.trend.lastIndex.coerceAtLeast(0))
                }
            }
        }
    }

    private fun pin(term: String) {
        val stat = state.value.data?.terms?.firstOrNull { it.term == term } ?: return
        if (state.value.pinningTerm != null) return
        pinJob?.cancel()
        pinJob = viewModelScope.launch {
            updateState { it.copy(pinningTerm = term, pinError = null) }
            when (val r = repository.pinTerm(term, stat.termEn)) {
                is AppResult.Failure -> updateState { it.copy(pinningTerm = null, pinError = r.error) }
                is AppResult.Success -> updateState { s ->
                    s.copy(
                        pinningTerm = null,
                        data = s.data?.copy(terms = s.data.terms.map { t -> if (t.term == term) t.copy(inGlossary = true) else t }),
                    )
                }
            }
        }
    }
}
