package com.techtsunagu.wakatta.feature.connect

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.DictationEvent
import com.techtsunagu.wakatta.core.media.DictationSource
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.feature.connect.domain.EntryAction
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import com.techtsunagu.wakatta.feature.connect.domain.TalkEntryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal enum class TalkDialog { NONE, RENAME, DELETE }

internal data class TalkUiState(
    val threadId: String = "",
    val lang: String = "en",
    val isLoading: Boolean = true,
    val error: ConnectError? = null,
    val title: String = "",
    val entries: List<TalkEntryDto> = emptyList(),
    val isListening: Boolean = false,
    val dialog: TalkDialog = TalkDialog.NONE,
    val nameDraft: String = "",
    val nameInvalid: Boolean = false,
)

internal sealed interface TalkEvent {
    data class Init(val threadId: String, val lang: String) : TalkEvent
    data object Retry : TalkEvent
    data object Listen : TalkEvent
    data object StopListening : TalkEvent
    data class Understand(val entryId: String, val text: String) : TalkEvent
    data class ShowMeaning(val entryId: String) : TalkEvent
    data class Reply(val entryId: String, val index: Int) : TalkEvent
    data class Confirm(val entryId: String, val understood: Boolean) : TalkEvent
    data class RetryReply(val entryId: String) : TalkEvent

    /** Reads aloud what they said, or (with [reply]) the owner's chosen reply. */
    data class Say(val entryId: String, val reply: Boolean) : TalkEvent
    data object OpenRename : TalkEvent
    data object OpenDelete : TalkEvent
    data class SetName(val name: String) : TalkEvent
    data object SaveName : TalkEvent
    data object ConfirmDelete : TalkEvent
    data object DismissDialog : TalkEvent
}

internal sealed interface TalkEffect {
    data object Deleted : TalkEffect
    data class Failed(val error: ConnectError) : TalkEffect
}

/** A Talk Thread is private to its owner and never notifies anyone. */
@HiltViewModel
internal class TalkViewModel @Inject constructor(
    private val threads: PersonalThreadRepository,
    private val speaker: Speaker,
    private val dictation: DictationSource,
) : MviViewModel<TalkUiState, TalkEvent, TalkEffect>(TalkUiState()) {

    private var dictationJob: Job? = null

    // Nobody can repeat what the other person said: words that could not be saved go again at the next Listen.
    private val unsaved = mutableListOf<String>()
    private val saving = Mutex()

    override fun onEvent(event: TalkEvent) {
        when (event) {
            is TalkEvent.Init -> {
                updateState { it.copy(threadId = event.threadId, lang = event.lang) }
                load()
            }
            TalkEvent.Retry -> load()
            TalkEvent.Listen -> listen()
            TalkEvent.StopListening -> stopListening()
            is TalkEvent.Understand -> patch(event.entryId, EntryAction.Understand(event.text))
            is TalkEvent.ShowMeaning -> patch(event.entryId, EntryAction.ShowMeaning)
            is TalkEvent.Reply -> {
                val replyJa = state.value.entries.find { it.id == event.entryId }
                    ?.replyOptions?.getOrNull(event.index)?.ja.orEmpty()
                patch(event.entryId, EntryAction.Reply(event.index, replyJa))
            }
            is TalkEvent.Confirm -> patch(event.entryId, EntryAction.Confirm(event.understood))
            is TalkEvent.RetryReply -> patch(event.entryId, EntryAction.RetryReply)
            is TalkEvent.Say -> say(event)
            TalkEvent.OpenRename -> updateState { it.copy(dialog = TalkDialog.RENAME, nameDraft = it.title, nameInvalid = false) }
            TalkEvent.OpenDelete -> updateState { it.copy(dialog = TalkDialog.DELETE) }
            is TalkEvent.SetName -> updateState { it.copy(nameDraft = event.name, nameInvalid = false) }
            TalkEvent.SaveName -> rename()
            TalkEvent.ConfirmDelete -> delete()
            TalkEvent.DismissDialog -> updateState { it.copy(dialog = TalkDialog.NONE, nameInvalid = false) }
        }
    }

    private fun load() {
        val id = state.value.threadId
        updateState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val talk = threads.getTalk(id)
            val entries = threads.getEntries(id)
            val failure = talk.errorOrNull() ?: entries.errorOrNull()
            updateState {
                if (failure != null) {
                    it.copy(isLoading = false, error = failure.toConnectError())
                } else {
                    it.copy(isLoading = false, title = talk.getOrNull()?.title.orEmpty(), entries = entries.getOrNull().orEmpty())
                }
            }
        }
    }

    private fun listen() {
        if (state.value.isListening) {
            stopListening()
            return
        }
        if (unsaved.isNotEmpty()) saveWords()
        if (!dictation.isAvailable()) {
            sendEffect(TalkEffect.Failed(ConnectError.DICTATION_UNAVAILABLE))
            return
        }

        updateState { it.copy(isListening = true) }
        dictationJob?.cancel()
        dictationJob = viewModelScope.launch {
            dictation.start("ja-JP").collect { event ->
                when (event) {
                    is DictationEvent.Listening -> updateState { it.copy(isListening = true) }
                    is DictationEvent.Partial -> Unit
                    is DictationEvent.Final -> {
                        updateState { it.copy(isListening = false) }
                        dictation.stop()
                        if (event.text.isNotBlank()) {
                            unsaved += event.text
                            saveWords()
                        }
                    }
                    is DictationEvent.Error -> {
                        updateState { it.copy(isListening = false) }
                        dictation.stop()
                        sendEffect(TalkEffect.Failed(event.kind.toConnectError()))
                    }
                }
            }
            updateState { it.copy(isListening = false) }
        }
    }

    /** Not part of [dictationJob]: listening again must not cut a save short. In the order the words were spoken. */
    private fun saveWords() {
        viewModelScope.launch {
            saving.withLock {
                while (unsaved.isNotEmpty()) {
                    when (val res = threads.addEntry(state.value.threadId, unsaved.first())) {
                        is AppResult.Success -> {
                            unsaved.removeAt(0)
                            updateState { it.copy(entries = it.entries + res.value) }
                        }
                        is AppResult.Failure -> {
                            sendEffect(TalkEffect.Failed(res.error.toConnectError()))
                            if (res.error.canPass()) return@withLock
                            unsaved.removeAt(0)
                        }
                    }
                }
            }
        }
    }

    /** True for an error that a later try can get past; the others would fail the same way each time. */
    private fun AppError.canPass() = when (this) {
        AppError.Offline, AppError.Timeout, AppError.Server, AppError.Busy, AppError.SessionExpired, is AppError.RateLimited -> true
        else -> false
    }

    private fun stopListening() {
        // The job stays: the recognizer still answers with the words spoken before the stop.
        dictation.stop()
        updateState { it.copy(isListening = false) }
    }

    private fun patch(entryId: String, action: EntryAction) {
        viewModelScope.launch {
            when (val res = threads.updateEntry(state.value.threadId, entryId, action)) {
                is AppResult.Success -> updateState { s ->
                    s.copy(entries = s.entries.map { if (it.id == entryId) res.value else it })
                }
                is AppResult.Failure -> sendEffect(TalkEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun say(event: TalkEvent.Say) {
        val entry = state.value.entries.firstOrNull { it.id == event.entryId } ?: return
        val text = if (event.reply) entry.replyJa else entry.said
        if (text == null) return
        if (speaker.isAvailable(SPEECH_LANG)) speaker.speak(text, SPEECH_LANG) else sendEffect(TalkEffect.Failed(ConnectError.NO_SPEECH))
    }

    private fun rename() {
        val name = state.value.nameDraft
        if (!isValidTalkName(name)) {
            updateState { it.copy(nameInvalid = true) }
            return
        }
        viewModelScope.launch {
            when (val res = threads.renameTalk(state.value.threadId, name)) {
                is AppResult.Success -> updateState { it.copy(title = res.value.title, dialog = TalkDialog.NONE) }
                is AppResult.Failure -> sendEffect(TalkEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    private fun delete() {
        updateState { it.copy(dialog = TalkDialog.NONE) }
        viewModelScope.launch {
            when (val res = threads.deleteTalk(state.value.threadId)) {
                is AppResult.Success -> sendEffect(TalkEffect.Deleted)
                is AppResult.Failure -> sendEffect(TalkEffect.Failed(res.error.toConnectError()))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        dictationJob?.cancel()
        dictation.stop()
    }

    private companion object {
        const val SPEECH_LANG = "ja"
    }
}

@Composable
internal fun TalkScreen(
    threadId: String,
    onBackClick: () -> Unit,
    viewModel: TalkViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lang = appLanguage()
    var permissionDeniedCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedCount = 0
            viewModel.onEvent(TalkEvent.Listen)
        } else {
            permissionDeniedCount++
            if (permissionDeniedCount == 1) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.connect_mic_rationale))
                }
            }
        }
    }

    val onMicClick: () -> Unit = {
        if (state.isListening) {
            viewModel.onEvent(TalkEvent.StopListening)
        } else if (permissionDeniedCount >= 2) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.connect_dictation_unavailable))
            }
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.onEvent(TalkEvent.Listen)
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    LaunchedEffect(threadId) { viewModel.onEvent(TalkEvent.Init(threadId, lang)) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                TalkEffect.Deleted -> onBackClick()
                is TalkEffect.Failed -> snackbarHostState.showSnackbar(context.getString(effect.error.message))
            }
        }
    }
    TalkContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        onListenClick = onMicClick,
    )
}

@Composable
internal fun TalkContent(
    state: TalkUiState,
    onEvent: (TalkEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onListenClick: (() -> Unit)? = null,
) {
    val handleListen = onListenClick ?: { onEvent(TalkEvent.Listen) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = state.title.ifEmpty { stringResource(R.string.connect_pchat_screen_title) },
                onBackClick = onBackClick,
                actions = { TalkActions(onEvent) },
            )
        },
        bottomBar = { if (state.error == null && !state.isLoading) ListenBar(state.isListening, handleListen) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> LoadingState(message = stringResource(R.string.connect_loading), modifier = Modifier.fillMaxSize())
                state.error != null -> ErrorState(
                    message = stringResource(state.error.message),
                    onRetry = { onEvent(TalkEvent.Retry) },
                    modifier = Modifier.fillMaxSize(),
                )
                state.entries.isEmpty() -> EmptyState(
                    message = stringResource(R.string.connect_talk_empty),
                    modifier = Modifier.fillMaxSize(),
                )
                else -> Entries(state, onEvent)
            }
        }
    }
    TalkDialogs(state, onEvent)
}

@Composable
private fun TalkActions(onEvent: (TalkEvent) -> Unit) {
    IconButton(onClick = { onEvent(TalkEvent.OpenRename) }, modifier = Modifier.size(48.dp)) {
        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.connect_rename))
    }
    IconButton(onClick = { onEvent(TalkEvent.OpenDelete) }, modifier = Modifier.size(48.dp)) {
        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.connect_delete))
    }
}

@Composable
private fun Entries(state: TalkUiState, onEvent: (TalkEvent) -> Unit) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.entries.size) { if (state.entries.isNotEmpty()) listState.animateScrollToItem(state.entries.lastIndex) }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.entries, key = { it.id }, contentType = { "entry" }) { ExchangeCard(it, state.lang, onEvent) }
    }
}

@Composable
private fun ListenBar(listening: Boolean, onListen: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Button(onClick = onListen, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)) {
            if (listening) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.Mic, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(if (listening) R.string.connect_listening else R.string.connect_listen))
        }
    }
}

@Composable
private fun TalkDialogs(state: TalkUiState, onEvent: (TalkEvent) -> Unit) {
    when (state.dialog) {
        TalkDialog.NONE -> Unit
        TalkDialog.RENAME -> AlertDialog(
            onDismissRequest = { onEvent(TalkEvent.DismissDialog) },
            title = { Text(stringResource(R.string.connect_rename)) },
            text = { TalkNameField(state.nameDraft, state.nameInvalid) { onEvent(TalkEvent.SetName(it)) } },
            confirmButton = { TextButton(onClick = { onEvent(TalkEvent.SaveName) }) { Text(stringResource(R.string.connect_save)) } },
            dismissButton = { TextButton(onClick = { onEvent(TalkEvent.DismissDialog) }) { Text(stringResource(R.string.connect_cancel)) } },
        )
        TalkDialog.DELETE -> AlertDialog(
            onDismissRequest = { onEvent(TalkEvent.DismissDialog) },
            title = { Text(stringResource(R.string.connect_delete_title)) },
            text = { Text(stringResource(R.string.connect_delete_body, state.title)) },
            confirmButton = {
                TextButton(onClick = { onEvent(TalkEvent.ConfirmDelete) }) {
                    Text(stringResource(R.string.connect_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onEvent(TalkEvent.DismissDialog) }) { Text(stringResource(R.string.connect_cancel)) } },
        )
    }
}
