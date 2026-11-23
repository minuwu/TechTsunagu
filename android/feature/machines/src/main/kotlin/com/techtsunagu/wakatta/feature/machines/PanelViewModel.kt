package com.techtsunagu.wakatta.feature.machines

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto
import com.techtsunagu.wakatta.feature.machines.domain.PanelRepository
import com.techtsunagu.wakatta.feature.machines.domain.PanelStateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

data class PanelWorkerRowUiState(
    val workerId: String,
    val displayName: String,
    val isReady: Boolean,
    val hasSignOff: Boolean,
    val signOffVersion: String?,
    val signOffAt: String?,
    val isPresent: Boolean,
)

/** A short message as a string resource plus one optional argument; the screen resolves it. */
data class PanelMessage(@StringRes val resId: Int, val arg: String? = null)

data class PanelUiState(
    val machineId: String = "",
    val machineName: String = "",
    val state: PanelState = PanelState.LOCKED,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isForeman: Boolean = false,
    val currentUserId: String = "",
    val isUserSignedOff: Boolean = false,
    val noteVersion: String = "v3",
    val signedOffAt: String? = null,
    val activeWorkerName: String? = null,
    val isCurrentUserPresent: Boolean = false,
    val activeOverride: PanelOverrideDto? = null,
    val perWorkerStatuses: List<PanelWorkerRowUiState> = emptyList(),
    val signedOffCount: Int = 0,
    val totalWorkerCount: Int = 0,
    val showOverrideSheet: Boolean = false,
    val showOverrideConfirmDialog: Boolean = false,
    val overrideWorkerId: String? = null,
    val overrideMinutes: Int = 60,
    val overrideReason: String = "",
    @StringRes val overrideReasonErrorRes: Int? = null,
    val toastMessage: PanelMessage? = null,
    val lastEtag: String? = null,
)

sealed interface PanelEvent {
    data class Init(val machineId: String) : PanelEvent
    data object Refresh : PanelEvent
    data object Retry : PanelEvent
    data object PollTick : PanelEvent
    data class RemindWorker(val workerId: String) : PanelEvent
    data object OpenNote : PanelEvent
    data object OpenNearMiss : PanelEvent
    data object OpenOverrideSheet : PanelEvent
    data object DismissOverrideSheet : PanelEvent
    data class OverrideWorkerSelected(val workerId: String?) : PanelEvent
    data class OverrideMinutesChanged(val minutes: Int) : PanelEvent
    data class OverrideReasonChanged(val reason: String) : PanelEvent
    data object RequestOverrideConfirm : PanelEvent
    data object ConfirmOverride : PanelEvent
    data object DismissOverrideConfirm : PanelEvent
    data object EndOverride : PanelEvent
    data object DismissToast : PanelEvent
}

sealed interface PanelEffect {
    data class NavigateToNote(val machineId: String) : PanelEffect
    data class NavigateToNearMiss(val machineId: String) : PanelEffect
}

/**
 * Safety rule: the Panel must never keep showing Ready from old data. After [MAX_FAILED_POLLS] failed polls in a
 * row, or one Offline or Timeout, or when the last good answer is older than [STALE_AFTER_MS], the state becomes
 * UNKNOWN until a poll succeeds again.
 */
@HiltViewModel
class PanelViewModel(
    private val panelRepository: PanelRepository,
    private val sessionStore: SessionStore,
    private val nowMs: () -> Long,
) : MviViewModel<PanelUiState, PanelEvent, PanelEffect>(PanelUiState(isLoading = true)) {

    @Inject
    constructor(
        panelRepository: PanelRepository,
        sessionStore: SessionStore,
    ) : this(panelRepository, sessionStore, { System.nanoTime() / 1_000_000L })

    private var failedPolls = 0
    private var lastFreshAtMs = 0L

    // The last state the server really told us; shown again when a 304 proves nothing changed while we were UNKNOWN.
    private var knownState = PanelState.LOCKED

    // One idempotency key per Panel Override action (rule R-5): kept until it succeeds or its inputs change.
    private var overrideKey: String? = null

    override fun onEvent(event: PanelEvent) {
        when (event) {
            is PanelEvent.Init -> {
                if (state.value.machineId != event.machineId) {
                    loadPanel(event.machineId)
                }
            }
            is PanelEvent.Refresh, is PanelEvent.Retry -> {
                if (state.value.machineId.isNotEmpty()) {
                    loadPanel(state.value.machineId)
                }
            }
            is PanelEvent.PollTick -> pollState()
            is PanelEvent.RemindWorker -> sendReminder(event.workerId)
            is PanelEvent.OpenNote -> sendEffect(PanelEffect.NavigateToNote(state.value.machineId))
            is PanelEvent.OpenNearMiss -> sendEffect(PanelEffect.NavigateToNearMiss(state.value.machineId))
            is PanelEvent.OpenOverrideSheet -> updateState { it.copy(showOverrideSheet = true) }
            is PanelEvent.DismissOverrideSheet -> {
                overrideKey = null
                updateState { it.copy(showOverrideSheet = false) }
            }
            is PanelEvent.OverrideWorkerSelected -> changeOverrideInput { it.copy(overrideWorkerId = event.workerId) }
            is PanelEvent.OverrideMinutesChanged -> changeOverrideInput { it.copy(overrideMinutes = event.minutes) }
            is PanelEvent.OverrideReasonChanged ->
                changeOverrideInput { it.copy(overrideReason = event.reason, overrideReasonErrorRes = null) }
            is PanelEvent.RequestOverrideConfirm -> validateAndConfirm()
            is PanelEvent.ConfirmOverride -> applyOverride()
            is PanelEvent.DismissOverrideConfirm -> updateState { it.copy(showOverrideConfirmDialog = false) }
            is PanelEvent.EndOverride -> revokeOverride()
            is PanelEvent.DismissToast -> updateState { it.copy(toastMessage = null) }
        }
    }

    // A changed request is a new user action, so it gets a new key.
    private fun changeOverrideInput(change: (PanelUiState) -> PanelUiState) {
        overrideKey = null
        updateState(change)
    }

    fun loadPanel(machineId: String) {
        viewModelScope.launch {
            updateState { it.copy(machineId = machineId, isLoading = true, error = null) }
            val session = sessionStore.session.value
            val isForeman = session?.user?.role == Role.FOREMAN
            val userId = session?.user?.id.orEmpty()

            when (val res = panelRepository.getPanelState(machineId)) {
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = res.error) }
                is AppResult.Success -> {
                    markFresh(res.value.panel.state)
                    showLoadedPanel(res.value, isForeman, userId)
                }
            }
        }
    }

    private fun showLoadedPanel(result: PanelStateResult, isForeman: Boolean, userId: String) {
        val p = result.panel
        val userRow = p.workerStatuses.find { it.workerId == userId }
        updateState {
            it.copy(
                isLoading = false,
                error = null,
                machineId = p.machineId,
                machineName = p.machineName["ja"] ?: p.machineId,
                state = p.state,
                isForeman = isForeman,
                currentUserId = userId,
                isUserSignedOff = userRow?.hasSignOff == true,
                noteVersion = userRow?.signOffVersion ?: "v3",
                signedOffAt = userRow?.signOffAt,
                activeWorkerName = p.activeWorkerName,
                isCurrentUserPresent = p.activeWorkerId == userId,
                activeOverride = p.override,
                perWorkerStatuses = p.workerStatuses.map { ws ->
                    PanelWorkerRowUiState(
                        workerId = ws.workerId,
                        displayName = ws.displayName,
                        isReady = ws.isReady,
                        hasSignOff = ws.hasSignOff,
                        signOffVersion = ws.signOffVersion,
                        signOffAt = ws.signOffAt,
                        isPresent = ws.isPresent,
                    )
                },
                signedOffCount = p.workerStatuses.count { ws -> ws.hasSignOff },
                totalWorkerCount = p.workerStatuses.size,
                lastEtag = result.etag,
            )
        }
    }

    private fun markFresh(realState: PanelState) {
        failedPolls = 0
        lastFreshAtMs = nowMs()
        knownState = realState
    }

    private fun markUnknown() {
        updateState { if (it.state == PanelState.UNKNOWN) it else it.copy(state = PanelState.UNKNOWN) }
    }

    private fun pollState() {
        val mid = state.value.machineId
        if (mid.isEmpty() || state.value.isLoading || state.value.error != null) return
        // Checked before the request: a poll that hangs must not keep an old Ready on screen.
        if (nowMs() - lastFreshAtMs > STALE_AFTER_MS) markUnknown()
        viewModelScope.launch {
            when (val res = panelRepository.getPanelState(mid, state.value.lastEtag)) {
                is AppResult.Success -> onPollSuccess(res.value)
                is AppResult.Failure -> onPollFailure(res.error)
            }
        }
    }

    private fun onPollSuccess(result: PanelStateResult) {
        if (result.isNotModified) {
            // 304: nothing changed since our last good answer, so that answer is current again.
            lastFreshAtMs = nowMs()
            failedPolls = 0
            updateState { if (it.state == PanelState.UNKNOWN) it.copy(state = knownState) else it }
            return
        }
        val p = result.panel
        markFresh(p.state)
        updateState {
            it.copy(
                state = p.state,
                activeWorkerName = p.activeWorkerName,
                activeOverride = p.override,
                lastEtag = result.etag,
            )
        }
    }

    private fun onPollFailure(error: AppError) {
        failedPolls++
        if (failedPolls >= MAX_FAILED_POLLS || error == AppError.Offline || error == AppError.Timeout) markUnknown()
    }

    private fun sendReminder(workerId: String) {
        val name = state.value.perWorkerStatuses.find { it.workerId == workerId }?.displayName ?: workerId
        updateState { it.copy(toastMessage = PanelMessage(R.string.machines_panel_reminded_toast, name)) }
    }

    private fun validateAndConfirm() {
        val reason = state.value.overrideReason.trim()
        if (reason.length < MIN_REASON || reason.length > MAX_REASON) {
            updateState { it.copy(overrideReasonErrorRes = R.string.machines_panel_override_reason_error) }
            return
        }
        updateState { it.copy(showOverrideConfirmDialog = true) }
    }

    private fun applyOverride() {
        val s = state.value
        val key = overrideKey ?: UUID.randomUUID().toString().also { overrideKey = it }
        viewModelScope.launch {
            updateState { it.copy(showOverrideConfirmDialog = false) }
            val res = panelRepository.requestOverride(
                machineId = s.machineId,
                workerId = s.overrideWorkerId,
                minutes = s.overrideMinutes,
                reason = s.overrideReason.trim(),
                idempotencyKey = key,
            )
            when (res) {
                is AppResult.Success -> onOverrideApplied(res.value)
                // The sheet stays open, so the Foreman can try again with the same key.
                is AppResult.Failure -> updateState {
                    it.copy(toastMessage = PanelMessage(res.error.machinesMessageRes()))
                }
            }
        }
    }

    private fun onOverrideApplied(override: PanelOverrideDto) {
        overrideKey = null
        markFresh(PanelState.OVERRIDE)
        updateState {
            it.copy(
                showOverrideSheet = false,
                state = PanelState.OVERRIDE,
                activeOverride = override,
                overrideReason = "",
                toastMessage = PanelMessage(R.string.machines_panel_override_authorized),
            )
        }
    }

    private fun revokeOverride() {
        val ov = state.value.activeOverride ?: return
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val res = panelRepository.revokeOverride(state.value.machineId, ov.id)
            if (res is AppResult.Failure) {
                updateState { it.copy(isLoading = false, toastMessage = PanelMessage(res.error.machinesMessageRes())) }
            } else {
                loadPanel(state.value.machineId)
            }
        }
    }

    private companion object {
        const val MAX_FAILED_POLLS = 2
        const val STALE_AFTER_MS = 10_000L
        const val MIN_REASON = 3
        const val MAX_REASON = 200
    }
}
