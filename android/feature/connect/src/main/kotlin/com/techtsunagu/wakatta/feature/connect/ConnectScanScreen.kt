package com.techtsunagu.wakatta.feature.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.QrCamera
import com.techtsunagu.wakatta.core.media.QrPayload
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.QrScanner
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.ContactResolveDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class ConnectScanUiState(
    val isLoading: Boolean = true,
    val isBusy: Boolean = false,
    val code: String = "",
    val error: ConnectError? = null,
    val samples: List<ContactResolveDto> = emptyList(),
    val pending: ContactResolveDto? = null,
)

internal sealed interface ConnectScanEvent {
    data class SetCode(val code: String) : ConnectScanEvent
    data object ReadCode : ConnectScanEvent
    data class PickSample(val token: String) : ConnectScanEvent
    data object Confirm : ConnectScanEvent
    data object Cancel : ConnectScanEvent
}

internal sealed interface ConnectScanEffect {
    /** Connected (or already connected): open that Contact Thread. */
    data class Opened(val threadId: String) : ConnectScanEffect
}

@HiltViewModel
internal class ConnectScanViewModel @Inject constructor(
    private val contacts: ContactRepository,
    private val scanner: QrScanner,
) : MviViewModel<ConnectScanUiState, ConnectScanEvent, ConnectScanEffect>(ConnectScanUiState()) {

    init {
        viewModelScope.launch {
            val samples = contacts.getSamplePeople()
            updateState { it.copy(isLoading = false, samples = samples) }
        }
        viewModelScope.launch { scanner.scanResults().collect { resolveRaw(it) } }
    }

    override fun onEvent(event: ConnectScanEvent) {
        when (event) {
            is ConnectScanEvent.SetCode -> updateState { it.copy(code = event.code, error = null) }
            ConnectScanEvent.ReadCode -> resolveRaw(state.value.code)
            is ConnectScanEvent.PickSample -> resolveToken(event.token)
            ConnectScanEvent.Confirm -> confirm()
            ConnectScanEvent.Cancel -> updateState { it.copy(pending = null) }
        }
    }

    /** Accepts `wk:<token>` from a scan, or a bare token typed by hand. */
    private fun resolveRaw(raw: String) {
        val payload = QrPayload.parse(raw) ?: QrPayload.parse("wk:${raw.trim()}")
        val token = (payload as? QrPayload.MyQr)?.token
        if (token == null) updateState { it.copy(error = ConnectError.BAD_CODE) } else resolveToken(token)
    }

    private fun resolveToken(token: String) {
        if (state.value.isBusy || state.value.pending != null) return
        updateState { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            when (val res = contacts.resolve(token)) {
                is AppResult.Success -> updateState { it.copy(isBusy = false, pending = res.value) }
                is AppResult.Failure -> updateState { it.copy(isBusy = false, error = res.error.toConnectError()) }
            }
        }
    }

    private fun confirm() {
        val pending = state.value.pending ?: return
        updateState { it.copy(isBusy = true) }
        viewModelScope.launch {
            when (val res = contacts.addContact(pending.token)) {
                is AppResult.Success -> {
                    updateState { it.copy(isBusy = false, pending = null, code = "") }
                    sendEffect(ConnectScanEffect.Opened(res.value.id))
                }
                is AppResult.Failure -> updateState {
                    it.copy(isBusy = false, pending = null, error = res.error.toConnectError())
                }
            }
        }
    }
}

@Composable
internal fun ConnectScanScreen(
    onBackClick: () -> Unit = {},
    onOpenThread: (String) -> Unit = {},
    viewModel: ConnectScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ConnectScanEffect.Opened -> onOpenThread(effect.threadId)
            }
        }
    }
    ConnectScanContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick)
}

@Composable
internal fun ConnectScanContent(
    state: ConnectScanUiState,
    onEvent: (ConnectScanEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { WakattaTopBar(title = stringResource(R.string.connect_scan_screen_title), onBackClick = onBackClick) },
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingState(message = stringResource(R.string.connect_loading), modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else {
            ScanBody(state, onEvent, Modifier.padding(innerPadding))
        }
    }
    state.pending?.let { ConfirmDialog(it, state.isBusy, onEvent) }
}

@Composable
private fun ScanBody(state: ConnectScanUiState, onEvent: (ConnectScanEvent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.connect_scan_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        QrCamera(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        OutlinedTextField(
            value = state.code,
            onValueChange = { onEvent(ConnectScanEvent.SetCode(it)) },
            label = { Text(stringResource(R.string.connect_scan_code_label)) },
            singleLine = true,
            isError = state.error != null,
            modifier = Modifier.fillMaxWidth(),
        )
        state.error?.let { ErrorLine(it) }
        Button(
            onClick = { onEvent(ConnectScanEvent.ReadCode) },
            enabled = state.code.isNotBlank() && !state.isBusy,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
        ) { Text(stringResource(R.string.connect_scan_read)) }
        Samples(state, onEvent)
    }
}

@Composable
private fun ErrorLine(error: ConnectError) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.connect_icon_warning),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(stringResource(error.message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Samples(state: ConnectScanUiState, onEvent: (ConnectScanEvent) -> Unit) {
    if (state.samples.isEmpty()) return
    Text(
        text = stringResource(R.string.connect_scan_samples),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.samples.forEach { person ->
            WakattaChip(
                selected = false,
                enabled = !state.isBusy,
                onClick = { onEvent(ConnectScanEvent.PickSample(person.token)) },
                leadingIcon = { Avatar(name = person.name, size = 24.dp) },
                label = { Text(person.name) },
            )
        }
    }
}

@Composable
private fun ConfirmDialog(person: ContactResolveDto, busy: Boolean, onEvent: (ConnectScanEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(ConnectScanEvent.Cancel) },
        icon = { Avatar(name = person.name, size = 48.dp) },
        title = { Text(stringResource(R.string.connect_scan_confirm_title, person.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.connect_scan_confirm_lang, languageName(person.language)))
                Text(stringResource(R.string.connect_scan_confirm_note))
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(ConnectScanEvent.Confirm) }, enabled = !busy) {
                Text(stringResource(R.string.connect_scan_connect))
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(ConnectScanEvent.Cancel) }) { Text(stringResource(R.string.connect_cancel)) } },
    )
}
