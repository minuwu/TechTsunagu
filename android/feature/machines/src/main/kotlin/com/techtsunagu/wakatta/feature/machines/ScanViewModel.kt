package com.techtsunagu.wakatta.feature.machines

import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.media.QrPayload
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.QrScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ScanMachineChipUiState(
    val id: String,
    val code: String,
    val name: String,
)

data class ScanUiState(
    val isLoading: Boolean = false,
    val isTorchOn: Boolean = false,
    val error: AppError? = null,
    val manualCodeInput: String = "",
    val availableMachines: List<ScanMachineChipUiState> = emptyList(),
    val scannedMachineId: String? = null,
)

sealed interface ScanEvent {
    data object Retry : ScanEvent
    data object Refresh : ScanEvent
    data object ToggleTorch : ScanEvent
    data class ManualCodeChanged(val code: String) : ScanEvent
    data object SubmitManualCode : ScanEvent
    data class SimulateScan(val machineId: String) : ScanEvent
    data class QrCodeDetected(val rawPayload: String) : ScanEvent
    data object ClearError : ScanEvent
}

sealed interface ScanEffect {
    data class NavigateToPanel(val machineId: String) : ScanEffect
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    private val qrScanner: QrScanner,
) : MviViewModel<ScanUiState, ScanEvent, ScanEffect>(ScanUiState()) {

    init {
        loadMachines()
        listenToQrScanner()
    }

    override fun onEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.Retry, is ScanEvent.Refresh -> loadMachines()
            is ScanEvent.ToggleTorch -> updateState { it.copy(isTorchOn = !it.isTorchOn) }
            is ScanEvent.ManualCodeChanged -> updateState { it.copy(manualCodeInput = event.code, error = null) }
            is ScanEvent.SubmitManualCode -> handleManualCodeSubmit()
            is ScanEvent.SimulateScan -> handleScanMachine(event.machineId)
            is ScanEvent.QrCodeDetected -> handleRawQr(event.rawPayload)
            is ScanEvent.ClearError -> updateState { it.copy(error = null) }
        }
    }

    private fun loadMachines() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            when (val res = machineRepository.getMachines()) {
                is AppResult.Success -> {
                    val chips = res.value.map {
                        ScanMachineChipUiState(
                            id = it.id,
                            code = it.code,
                            name = it.name["en"] ?: it.name["ja"] ?: it.code
                        )
                    }
                    updateState { it.copy(isLoading = false, availableMachines = chips) }
                }
                is AppResult.Failure -> updateState { it.copy(isLoading = false, error = res.error) }
            }
        }
    }

    private fun listenToQrScanner() {
        viewModelScope.launch {
            qrScanner.scanResults()
                .catch { /* ignore scanner error in background */ }
                .collect { rawPayload ->
                    handleRawQr(rawPayload)
                }
        }
    }

    private fun handleRawQr(raw: String) {
        val payload = QrPayload.parse(raw)
        when (payload) {
            is QrPayload.MachineSticker -> handleScanMachine(payload.machineId)
            else -> {
                val machine = state.value.availableMachines.find {
                    it.id.equals(raw.trim(), ignoreCase = true) || it.code.equals(raw.trim(), ignoreCase = true)
                }
                if (machine != null) {
                    handleScanMachine(machine.id)
                } else {
                    updateState { it.copy(error = AppError.Validation()) }
                }
            }
        }
    }

    private fun handleManualCodeSubmit() {
        val input = state.value.manualCodeInput.trim()
        if (input.isEmpty()) return

        val machine = state.value.availableMachines.find {
            it.code.equals(input, ignoreCase = true) || it.id.equals(input, ignoreCase = true)
        }
        if (machine != null) {
            handleScanMachine(machine.id)
        } else {
            updateState { it.copy(error = AppError.NotFound) }
        }
    }

    private fun handleScanMachine(machineId: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            machineRepository.scan(machineId)
            updateState { it.copy(isLoading = false, scannedMachineId = machineId) }
            sendEffect(ScanEffect.NavigateToPanel(machineId))
        }
    }
}
