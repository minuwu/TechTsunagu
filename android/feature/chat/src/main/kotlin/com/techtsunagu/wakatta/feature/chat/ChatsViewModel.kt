package com.techtsunagu.wakatta.feature.chat

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.User
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.chat.model.ThreadFilter
import com.techtsunagu.wakatta.feature.chat.model.ThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class ChatsUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val filter: ThreadFilter = ThreadFilter.ALL,
    val threads: List<ThreadUiModel> = emptyList(),
)

sealed interface ChatsEvent {
    data object Init : ChatsEvent
    data object Retry : ChatsEvent
    data object Refresh : ChatsEvent
    data class SetFilter(val filter: ThreadFilter) : ChatsEvent
}

sealed interface ChatsEffect {
    data class OpenThread(val threadId: String) : ChatsEffect
}

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val threadSources: Set<@JvmSuppressWildcards ThreadSource>,
    private val sessionStore: SessionStore,
) : MviViewModel<ChatsUiState, ChatsEvent, ChatsEffect>(ChatsUiState(isLoading = true)) {

    private var rawSummaries: List<ThreadSummary> = emptyList()
    private var loadJob: Job? = null

    init {
        loadThreads()
    }

    override fun onEvent(event: ChatsEvent) {
        when (event) {
            is ChatsEvent.Init -> loadThreads()
            is ChatsEvent.Retry -> {
                updateState { it.copy(isLoading = true, error = null) }
                loadThreads()
            }
            is ChatsEvent.Refresh -> {
                updateState { it.copy(isLoading = true) }
                loadThreads()
            }
            is ChatsEvent.SetFilter -> {
                updateState { it.copy(filter = event.filter) }
                applyFilter()
            }
        }
    }

    private fun loadThreads() {
        loadJob?.cancel()
        // One failing source gives an empty list instead of ending the merge with the others.
        val flows: List<Flow<List<ThreadSummary>>> =
            threadSources.map { source -> source.getThreads().catch { emit(emptyList()) } }

        loadJob = combine(flows) { arrays: Array<List<ThreadSummary>> ->
            arrays.flatMap { it }.distinctBy { it.id }
        }.onEach { summaries: List<ThreadSummary> ->
            rawSummaries = summaries
            applyFilter()
        }.launchIn(viewModelScope)
    }

    private fun applyFilter() {
        val currentFilter = state.value.filter
        val currentUser = sessionStore.session.value?.user
        val filtered = rawSummaries.filter { summary ->
            when (currentFilter) {
                ThreadFilter.ALL -> true
                ThreadFilter.UNREAD -> summary.unreadCount > 0
            }
        }.map { summary ->
            val resolvedTitle = resolveThreadTitle(summary.title, currentUser)
            ThreadUiModel(
                id = summary.id,
                title = resolvedTitle,
                lastMessage = summary.lastMessage ?: "",
                lastMessageAt = summary.updatedAt,
                unreadCount = summary.unreadCount,
                isVoice = summary.lastMessage?.contains("点検") == true || summary.lastMessage?.contains("金型") == true,
                worstStatus = null,
                isGroup = false,
                membersCount = 0,
            )
        }

        updateState {
            it.copy(
                isLoading = false,
                isEmpty = filtered.isEmpty(),
                error = null,
                threads = filtered,
            )
        }
    }

    private fun resolveThreadTitle(title: Tri, currentUser: User?): Tri {
        if (currentUser != null && currentUser.role == Role.WORKER) {
            val myName = currentUser.displayName.trim()
            val jaTitle = title["ja"]?.trim().orEmpty()
            val matchesMyName = myName.isNotEmpty() && (
                jaTitle.equals(myName, ignoreCase = true) ||
                    title.values.any { it.trim().equals(myName, ignoreCase = true) }
            )
            if (matchesMyName) {
                return FOREMAN_TITLE
            }
        }
        return title
    }

    companion object {
        private val FOREMAN_TITLE: Tri = mapOf(
            "ja" to "職長",
            "en" to "Foreman",
            "bn" to "ফোরম্যান",
        )
    }
}
