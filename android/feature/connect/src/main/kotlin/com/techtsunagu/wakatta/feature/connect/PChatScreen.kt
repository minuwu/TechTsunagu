package com.techtsunagu.wakatta.feature.connect

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.connect.domain.ContactMessageDto
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadIds
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val POLL_MS = 5_000L

internal data class PChatUiState(
    val threadId: String = "",
    val lang: String = "en",
    val isLoading: Boolean = true,
    val error: ConnectError? = null,
    val title: String = "",
    val messages: List<ContactMessageDto> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val confirmBlock: Boolean = false,
    /** The message waiting for "Delete?" (long-press), or null. */
    val confirmDelete: String? = null,
    val confirmClear: Boolean = false,
)

internal sealed interface PChatEvent {
    /** [lang] is the app language: it decides which side of a translation is shown first. */
    data class Init(val threadId: String, val lang: String) : PChatEvent
    data object Retry : PChatEvent
    /** Sent by the screen every [POLL_MS] while it is shown (R7): new messages appear without leaving. */
    data object Poll : PChatEvent
    data class SetInput(val text: String) : PChatEvent
    data object Send : PChatEvent
    data object AskBlock : PChatEvent
    data object ConfirmBlock : PChatEvent
    data object DismissBlock : PChatEvent
    data class AskDelete(val messageId: String) : PChatEvent
    data object ConfirmDelete : PChatEvent
    data object AskClear : PChatEvent
    data object ConfirmClear : PChatEvent
    data object DismissDelete : PChatEvent
}

internal sealed interface PChatEffect {
    data object Blocked : PChatEffect
    data class Failed(val error: ConnectError) : PChatEffect
}

@HiltViewModel
internal class PChatViewModel @Inject constructor(
    private val contacts: ContactRepository,
    private val threads: PersonalThreadRepository,
) : MviViewModel<PChatUiState, PChatEvent, PChatEffect>(PChatUiState()) {

    override fun onEvent(event: PChatEvent) {
        when (event) {
            is PChatEvent.Init -> {
                updateState { it.copy(threadId = event.threadId, lang = event.lang) }
                load()
            }
            PChatEvent.Retry -> load()
            PChatEvent.Poll -> poll()
            is PChatEvent.SetInput -> updateState { it.copy(input = event.text) }
            PChatEvent.Send -> send()
            PChatEvent.AskBlock -> updateState { it.copy(confirmBlock = true) }
            PChatEvent.DismissBlock -> updateState { it.copy(confirmBlock = false) }
            PChatEvent.ConfirmBlock -> block()
            is PChatEvent.AskDelete -> updateState { it.copy(confirmDelete = event.messageId) }
            PChatEvent.AskClear -> updateState { it.copy(confirmClear = true) }
            PChatEvent.DismissDelete -> updateState { it.copy(confirmDelete = null, confirmClear = false) }
            PChatEvent.ConfirmDelete -> delete()
            PChatEvent.ConfirmClear -> clear()
        }
    }

    private fun load() {
        val id = state.value.threadId
        updateState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val contact = contacts.getContact(id)
            val messages = threads.getMessages(id)
            val failure = contact.errorOrNull() ?: messages.errorOrNull()
            updateState {
                if (failure != null) {
                    it.copy(isLoading = false, error = failure.toConnectError())
                } else {
                    it.copy(isLoading = false, title = contact.getOrNull()?.name.orEmpty(), messages = messages.getOrNull().orEmpty())
                }
            }
        }
    }

    /** A quiet refresh: no spinner, and a failed poll changes nothing (the next one tries again). */
    private fun poll() {
        val s = state.value
        if (s.threadId.isEmpty() || s.isLoading || s.error != null || s.isSending) return
        viewModelScope.launch {
            val messages = threads.getMessages(s.threadId).getOrNull() ?: return@launch
            if (messages != state.value.messages) updateState { it.copy(messages = messages) }
        }
    }

    private fun send() {
        val s = state.value
        if (s.isSending || s.input.isBlank()) return
        updateState { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val res = threads.sendMessage(s.threadId, s.input, s.lang)) {
                is AppResult.Success -> {
                    val messages = threads.getMessages(s.threadId).getOrNull() ?: state.value.messages
                    updateState { it.copy(isSending = false, input = "", messages = messages) }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isSending = false) }
                    sendEffect(PChatEffect.Failed(res.error.toConnectError()))
                }
            }
        }
    }

    private fun delete() {
        val id = state.value.confirmDelete ?: return
        updateState { it.copy(confirmDelete = null) }
        viewModelScope.launch {
            when (val res = threads.deleteMessage(state.value.threadId, id)) {
                is AppResult.Success -> updateState { s -> s.copy(messages = s.messages.filterNot { it.id == id }) }
                is AppResult.Failure -> sendEffect(PChatEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun clear() {
        updateState { it.copy(confirmClear = false) }
        viewModelScope.launch {
            when (val res = threads.clearMessages(state.value.threadId)) {
                is AppResult.Success -> updateState { it.copy(messages = emptyList()) }
                is AppResult.Failure -> sendEffect(PChatEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun block() {
        updateState { it.copy(confirmBlock = false) }
        viewModelScope.launch {
            when (val res = contacts.block(state.value.threadId)) {
                is AppResult.Success -> sendEffect(PChatEffect.Blocked)
                is AppResult.Failure -> sendEffect(PChatEffect.Failed(res.error.toConnectError()))
            }
        }
    }
}

/** Opens the Talk Thread or the Contact Thread screen for [threadId] (the id prefix tells which). */
@Composable
internal fun PChatScreen(threadId: String, onBackClick: () -> Unit = {}) {
    if (PersonalThreadIds.isTalk(threadId)) {
        TalkScreen(threadId = threadId, onBackClick = onBackClick)
    } else {
        ContactChatScreen(threadId = threadId, onBackClick = onBackClick)
    }
}

@Composable
internal fun ContactChatScreen(
    threadId: String,
    onBackClick: () -> Unit,
    viewModel: PChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lang = appLanguage()

    LaunchedEffect(threadId) { viewModel.onEvent(PChatEvent.Init(threadId, lang)) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(threadId) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                delay(POLL_MS)
                viewModel.onEvent(PChatEvent.Poll)
            }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PChatEffect.Blocked -> onBackClick()
                is PChatEffect.Failed -> snackbarHostState.showSnackbar(context.getString(effect.error.message))
            }
        }
    }
    PChatContent(state, viewModel::onEvent, onBackClick = onBackClick, snackbarHostState = snackbarHostState)
}

@Composable
internal fun PChatContent(
    state: PChatUiState,
    onEvent: (PChatEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = state.title.ifEmpty { stringResource(R.string.connect_pchat_screen_title) },
                onBackClick = onBackClick,
                actions = {
                    if (state.messages.isNotEmpty()) {
                        IconButton(onClick = { onEvent(PChatEvent.AskClear) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.connect_clear_chat))
                        }
                    }
                    IconButton(onClick = { onEvent(PChatEvent.AskBlock) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Block, contentDescription = stringResource(R.string.connect_block))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.isLoading -> LoadingState(message = stringResource(R.string.connect_loading), modifier = Modifier.fillMaxSize())
                    state.error != null -> ErrorState(
                        message = stringResource(state.error.message),
                        onRetry = { onEvent(PChatEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    state.messages.isEmpty() -> EmptyState(
                        message = stringResource(R.string.connect_chat_empty),
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> Messages(state.messages, state.lang) { onEvent(PChatEvent.AskDelete(it)) }
                }
            }
            if (state.error == null) MessageInput(state, onEvent)
        }
    }
    if (state.confirmBlock) BlockDialog(state.title, onEvent)
    if (state.confirmDelete != null || state.confirmClear) DeleteDialog(all = state.confirmClear, onEvent = onEvent)
}

@Composable
private fun DeleteDialog(all: Boolean, onEvent: (PChatEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(PChatEvent.DismissDelete) },
        title = { Text(stringResource(if (all) R.string.connect_clear_title else R.string.connect_msg_delete_title)) },
        text = { Text(stringResource(if (all) R.string.connect_clear_body else R.string.connect_msg_delete_body)) },
        confirmButton = {
            TextButton(onClick = { onEvent(if (all) PChatEvent.ConfirmClear else PChatEvent.ConfirmDelete) }) {
                Text(stringResource(R.string.connect_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(PChatEvent.DismissDelete) }) { Text(stringResource(R.string.connect_cancel)) } },
    )
}

@Composable
private fun Messages(messages: List<ContactMessageDto>, lang: String, onLongPress: (String) -> Unit = {}) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(messages, key = { it.id }, contentType = { if (it.fromMe) "mine" else "theirs" }) { message ->
            Bubble(message, lang, Modifier.combinedClickable(onClick = {}, onLongClick = { onLongPress(message.id) }))
        }
    }
}

@Composable
private fun Bubble(message: ContactMessageDto, lang: String, modifier: Modifier = Modifier) {
    val japanese = lang == "ja"
    val alt = (if (japanese) message.en else message.ja).ifBlank { null }
    val shown = (if (japanese) message.ja else message.en).ifBlank { alt.orEmpty() }
    val note = alt ?: stringResource(R.string.connect_no_translation)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (message.fromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.8f).then(modifier),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(shown, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = stringResource(if (message.fromMe) R.string.connect_they_see else R.string.connect_original, note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MessageInput(state: PChatUiState, onEvent: (PChatEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = state.input,
            onValueChange = { onEvent(PChatEvent.SetInput(it)) },
            placeholder = { Text(stringResource(R.string.connect_message_hint)) },
            modifier = Modifier.weight(1f),
            maxLines = 4,
        )
        IconButton(
            onClick = { onEvent(PChatEvent.Send) },
            enabled = state.input.isNotBlank() && !state.isSending,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.connect_send),
                tint = if (state.input.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Unspecified,
            )
        }
    }
}

@Composable
private fun BlockDialog(name: String, onEvent: (PChatEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(PChatEvent.DismissBlock) },
        title = { Text(stringResource(R.string.connect_block_title)) },
        text = { Text(stringResource(R.string.connect_block_body, name)) },
        confirmButton = {
            TextButton(onClick = { onEvent(PChatEvent.ConfirmBlock) }) {
                Text(stringResource(R.string.connect_block), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { onEvent(PChatEvent.DismissBlock) }) { Text(stringResource(R.string.connect_cancel)) } },
    )
}
