package com.techtsunagu.wakatta.feature.connect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.QrMatrix
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.QrEncoder
import com.techtsunagu.wakatta.core.model.ShareFile
import com.techtsunagu.wakatta.feature.connect.domain.ContactDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import com.techtsunagu.wakatta.feature.connect.domain.TalkThreadDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

internal enum class ConnectDialog { NONE, NOTICE, NEW_TALK }

internal data class ConnectUiState(
    val isLoading: Boolean = true,
    val error: ConnectError? = null,
    val qrSide: Int = 0,
    val qrModules: List<Boolean> = emptyList(),
    val contacts: List<ContactDto> = emptyList(),
    val talks: List<TalkThreadDto> = emptyList(),
    val noticeAcknowledged: Boolean = false,
    val dialog: ConnectDialog = ConnectDialog.NONE,
    val talkName: String = "",
    val nameInvalid: Boolean = false,
)

internal sealed interface ConnectEvent {
    data object Load : ConnectEvent
    data object Rotate : ConnectEvent
    data object Share : ConnectEvent
    data object NewTalk : ConnectEvent
    data object AcknowledgeNotice : ConnectEvent
    data class SetTalkName(val name: String) : ConnectEvent
    data object CreateTalk : ConnectEvent
    data object DismissDialog : ConnectEvent
}

internal sealed interface ConnectEffect {
    data object Rotated : ConnectEffect
    data class Failed(val error: ConnectError) : ConnectEffect
    data class OpenThread(val threadId: String) : ConnectEffect
}

@HiltViewModel
internal class ConnectViewModel @Inject constructor(
    private val contacts: ContactRepository,
    private val threads: PersonalThreadRepository,
    private val qrEncoder: QrEncoder,
    private val shareFile: ShareFile,
) : MviViewModel<ConnectUiState, ConnectEvent, ConnectEffect>(ConnectUiState()) {

    private var loadJob: Job? = null

    init {
        // Lists change when a chat is blocked or a talk is deleted on another screen.
        viewModelScope.launch { threads.getThreads().drop(1).collect { refreshLists() } }
    }

    override fun onEvent(event: ConnectEvent) {
        when (event) {
            ConnectEvent.Load -> load()
            ConnectEvent.Rotate -> rotate()
            ConnectEvent.Share -> share()
            ConnectEvent.NewTalk -> openNewTalk()
            ConnectEvent.AcknowledgeNotice -> acknowledgeNotice()
            is ConnectEvent.SetTalkName -> updateState { it.copy(talkName = event.name, nameInvalid = false) }
            ConnectEvent.CreateTalk -> createTalk()
            ConnectEvent.DismissDialog -> updateState { it.copy(dialog = ConnectDialog.NONE, nameInvalid = false) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        updateState { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            val qr = contacts.getMyQr()
            val list = contacts.getContacts()
            val talks = threads.getTalks()
            val failure = listOf(qr, list, talks).firstNotNullOfOrNull { it.errorOrNull() }
            if (failure != null) {
                updateState { it.copy(isLoading = false, error = failure.toConnectError()) }
                return@launch
            }
            val matrix = qr.getOrNull()?.let { encode(it.token) }
            val acknowledged = threads.isNoticeAcknowledged()
            updateState {
                it.copy(
                    isLoading = false,
                    qrSide = matrix?.width ?: 0,
                    qrModules = matrix?.data?.toList().orEmpty(),
                    contacts = list.getOrNull().orEmpty(),
                    talks = talks.getOrNull().orEmpty(),
                    noticeAcknowledged = acknowledged,
                )
            }
        }
    }

    private fun refreshLists() {
        viewModelScope.launch {
            val list = contacts.getContacts().getOrNull() ?: return@launch
            val talks = threads.getTalks().getOrNull() ?: return@launch
            updateState { it.copy(contacts = list, talks = talks) }
        }
    }

    // zxing returns the module grid (with its quiet zone) when asked for a 1x1 size.
    private fun encode(token: String): QrMatrix? = qrEncoder.encode("wk:$token", 1, 1) as? QrMatrix

    private fun rotate() {
        viewModelScope.launch {
            when (val res = contacts.rotateMyQr()) {
                is AppResult.Success -> {
                    val matrix = encode(res.value.token)
                    updateState { it.copy(qrSide = matrix?.width ?: 0, qrModules = matrix?.data?.toList().orEmpty()) }
                    sendEffect(ConnectEffect.Rotated)
                }
                is AppResult.Failure -> sendEffect(ConnectEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun share() {
        viewModelScope.launch {
            when (val res = contacts.createInviteFile()) {
                is AppResult.Success -> shareFile.share(res.value, "image/png")
                is AppResult.Failure -> sendEffect(ConnectEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun openNewTalk() {
        val dialog = if (state.value.noticeAcknowledged) ConnectDialog.NEW_TALK else ConnectDialog.NOTICE
        updateState { it.copy(dialog = dialog, talkName = "", nameInvalid = false) }
    }

    private fun acknowledgeNotice() {
        viewModelScope.launch {
            threads.acknowledgeNotice()
            updateState { it.copy(noticeAcknowledged = true, dialog = ConnectDialog.NEW_TALK) }
        }
    }

    private fun createTalk() {
        val name = state.value.talkName
        if (!isValidTalkName(name)) {
            updateState { it.copy(nameInvalid = true) }
            return
        }
        viewModelScope.launch {
            when (val res = threads.createTalk(name)) {
                is AppResult.Success -> {
                    updateState { it.copy(dialog = ConnectDialog.NONE) }
                    sendEffect(ConnectEffect.OpenThread(res.value.id))
                }
                is AppResult.Failure -> sendEffect(ConnectEffect.Failed(res.error.toConnectError()))
            }
        }
    }
}

@Composable
internal fun ConnectScreen(
    onScanClick: () -> Unit = {},
    onOpenThread: (String) -> Unit = {},
    viewModel: ConnectViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.onEvent(ConnectEvent.Load) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                ConnectEffect.Rotated -> snackbarHostState.showSnackbar(context.getString(R.string.connect_rotated))
                is ConnectEffect.Failed -> snackbarHostState.showSnackbar(context.getString(effect.error.message))
                is ConnectEffect.OpenThread -> onOpenThread(effect.threadId)
            }
        }
    }

    ConnectContent(
        state = state,
        onEvent = viewModel::onEvent,
        onScanClick = onScanClick,
        onOpenThread = onOpenThread,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun ConnectContent(
    state: ConnectUiState,
    onEvent: (ConnectEvent) -> Unit,
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit = {},
    onOpenThread: (String) -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.connect_screen_title),
                actions = {
                    IconButton(onClick = onScanClick, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = stringResource(R.string.connect_scan))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> LoadingState(message = stringResource(R.string.connect_loading), modifier = Modifier.fillMaxSize())
                state.error != null -> ErrorState(
                    message = stringResource(state.error.message),
                    onRetry = { onEvent(ConnectEvent.Load) },
                    modifier = Modifier.fillMaxSize(),
                )
                else -> ConnectBody(state, onEvent, onScanClick, onOpenThread)
            }
        }
    }
    ConnectDialogs(state, onEvent)
}

@Composable
private fun ConnectBody(
    state: ConnectUiState,
    onEvent: (ConnectEvent) -> Unit,
    onScanClick: () -> Unit,
    onOpenThread: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "my_qr", contentType = "qr") { MyQrCard(state, onEvent, onScanClick) }
        item(key = "contacts_header", contentType = "header") { SectionTitle(R.string.connect_contacts_title) }
        if (state.contacts.isEmpty()) {
            item(key = "contacts_empty", contentType = "empty") { EmptyLine(R.string.connect_contacts_empty) }
        }
        items(state.contacts, key = { it.id }, contentType = { "contact" }) { contact ->
            WakattaListItem(
                headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    val time = formatContactTime(contact.lastMessage)
                    Text(time ?: languageName(contact.language), maxLines = 1)
                },
                leadingContent = { Avatar(name = contact.name) },
                onClick = { onOpenThread(contact.id) },
                modifier = Modifier.defaultMinSize(minHeight = 56.dp),
            )
        }
        item(key = "talks_header", contentType = "header") { TalksHeader { onEvent(ConnectEvent.NewTalk) } }
        if (state.talks.isEmpty()) {
            item(key = "talks_empty", contentType = "empty") { EmptyLine(R.string.connect_talks_empty) }
        }
        items(state.talks, key = { it.id }, contentType = { "talk" }) { talk ->
            WakattaListItem(
                headlineContent = { Text(talk.title, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.Mic, contentDescription = null) },
                onClick = { onOpenThread(talk.id) },
                modifier = Modifier.defaultMinSize(minHeight = 56.dp),
            )
        }
        item(key = "bottom", contentType = "spacer") { Spacer(Modifier.size(24.dp)) }
    }
}

@Composable
private fun MyQrCard(state: ConnectUiState, onEvent: (ConnectEvent) -> Unit, onScanClick: () -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.connect_my_qr_title), style = MaterialTheme.typography.titleMedium)
            QrCode(state.qrSide, state.qrModules)
            Text(
                text = stringResource(R.string.connect_my_qr_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onScanClick, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.connect_scan))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(
                    onClick = { onEvent(ConnectEvent.Share) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.connect_share))
                }
                OutlinedButton(
                    onClick = { onEvent(ConnectEvent.Rotate) },
                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.connect_rotate))
                }
            }
        }
    }
}

/** Draws the module grid from `QrEncoder` on a white tile so it scans in dark theme too. */
@Composable
internal fun QrCode(side: Int, modules: List<Boolean>, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.connect_my_qr_desc)
    Canvas(
        modifier = modifier
            .size(220.dp)
            .background(Color.White)
            .semantics { contentDescription = description },
    ) {
        if (side <= 0) return@Canvas
        val cell = size.width / side
        modules.forEachIndexed { index, dark ->
            if (dark) {
                drawRect(Color.Black, Offset((index % side) * cell, (index / side) * cell), Size(cell, cell))
            }
        }
    }
}

@Composable
private fun SectionTitle(res: Int) {
    Text(stringResource(res), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyLine(res: Int) {
    Text(
        text = stringResource(res),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TalksHeader(onNewTalk: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.connect_talks_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onNewTalk, modifier = Modifier.defaultMinSize(minHeight = 48.dp)) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.connect_new_talk))
        }
    }
}

@Composable
private fun ConnectDialogs(state: ConnectUiState, onEvent: (ConnectEvent) -> Unit) {
    when (state.dialog) {
        ConnectDialog.NONE -> Unit
        ConnectDialog.NOTICE -> AlertDialog(
            onDismissRequest = { onEvent(ConnectEvent.DismissDialog) },
            title = { Text(stringResource(R.string.connect_privacy_notice_title)) },
            text = { Text(stringResource(R.string.connect_privacy_notice_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ConnectEvent.AcknowledgeNotice) }) {
                    Text(stringResource(R.string.connect_privacy_notice_dismiss))
                }
            },
        )
        ConnectDialog.NEW_TALK -> AlertDialog(
            onDismissRequest = { onEvent(ConnectEvent.DismissDialog) },
            title = { Text(stringResource(R.string.connect_new_talk)) },
            text = { TalkNameField(state.talkName, state.nameInvalid) { onEvent(ConnectEvent.SetTalkName(it)) } },
            confirmButton = { TextButton(onClick = { onEvent(ConnectEvent.CreateTalk) }) { Text(stringResource(R.string.connect_create)) } },
            dismissButton = { TextButton(onClick = { onEvent(ConnectEvent.DismissDialog) }) { Text(stringResource(R.string.connect_cancel)) } },
        )
    }
}

@Composable
internal fun TalkNameField(value: String, invalid: Boolean, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.connect_name_label)) },
        isError = invalid,
        supportingText = if (invalid) {
            { Text(stringResource(R.string.connect_err_name_required)) }
        } else {
            null
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
