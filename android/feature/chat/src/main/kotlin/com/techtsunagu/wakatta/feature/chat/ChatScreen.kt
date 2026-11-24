package com.techtsunagu.wakatta.feature.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.feature.chat.model.ComposerStatus
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    threadId: String,
    onBackClick: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val countdown by viewModel.countdown.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(threadId) {
        viewModel.onEvent(ChatEvent.Init(threadId))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChatEffect.ShowError -> snackbarHostState.showSnackbar(context.getString(effect.error.chatMessageRes()))
                is ChatEffect.ShowHint -> snackbarHostState.showSnackbar(context.getString(effect.messageRes))
            }
        }
    }

    ChatContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
        countdownSeconds = { countdown.seconds },
        countdownProgress = { countdown.progress },
    )
}

@Composable
fun ChatContent(
    state: ChatUiState,
    onEvent: (ChatEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    countdownSeconds: () -> Int = { 0 },
    countdownProgress: () -> Float = { 0f },
    onMicClick: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WakattaTopBar(
                title = if (state.title.isNotEmpty()) state.title else stringResource(R.string.chat_screen_title),
                onBackClick = onBackClick,
                actions = {
                    // Debug-only role switch (the repository offers it only in the Fake path).
                    if (state.canSwitchRole) RoleSwitch(state.currentRole, onEvent)
                },
            )
        },
        bottomBar = {
            ChatComposer(
                status = state.composerStatus,
                inputText = state.inputText,
                countdownSeconds = countdownSeconds,
                countdownProgress = countdownProgress,
                hasPendingAudio = state.hasPendingAudio,
                onMicClick = onMicClick,
                snackbarHostState = snackbarHostState,
                onEvent = onEvent,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    LoadingState(message = stringResource(R.string.chat_loading))
                }
                state.error != null -> {
                    ErrorState(
                        message = stringResource(state.error.chatMessageRes()),
                        onRetry = { onEvent(ChatEvent.Retry) },
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.chat_screen_title),
                        message = stringResource(R.string.chat_empty),
                        actionLabel = stringResource(R.string.chat_retry),
                        onActionClick = { onEvent(ChatEvent.Refresh) },
                    )
                }
                else -> {
                    val listState = rememberLazyListState()
                    val unreadIndex = state.messages.indexOfFirst { it.id == state.firstUnreadId }
                    // Messenger-style open: the "new messages" bar mid-screen, or the newest message against the composer
                    var placed by remember(state.threadId) { mutableStateOf(false) }
                    LaunchedEffect(state.threadId, state.messages.isNotEmpty()) {
                        if (placed || state.messages.isEmpty()) return@LaunchedEffect
                        placed = true
                        if (unreadIndex >= 0) {
                            listState.scrollToItem(unreadIndex) // the divider is the item at this index
                            listState.scrollBy(-listState.layoutInfo.viewportSize.height / 2f)
                        } else {
                            listState.scrollToItem(state.messages.lastIndex)
                            listState.scrollBy(100_000f) // clamps at the end: a tall last bubble still sits on the composer
                        }
                    }
                    // a new message follows the bottom only when the reader was already there (or sent it)
                    val last = state.messages.lastOrNull()
                    LaunchedEffect(last?.id) {
                        val info = listState.layoutInfo
                        if (!placed || last == null || info.totalItemsCount == 0) return@LaunchedEffect
                        val nearBottom = (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - 3
                        if (last.isMine || nearBottom) listState.animateScrollToItem(info.totalItemsCount - 1)
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = state.messages,
                            key = { it.id },
                            contentType = { it.kind.name },
                        ) { message ->
                            if (message.id == state.firstUnreadId) UnreadDivider(state.unreadAtOpen)
                            ChatMessageBubble(
                                message = message,
                                isForeman = state.currentRole == Role.FOREMAN,
                                glossaryTerms = state.glossaryTerms,
                                onEvent = onEvent,
                            )
                        }
                    }
                }
            }
        }
    }

    // Glossary Popup
    if (state.selectedGlossaryTerm != null) {
        GlossaryDialog(
            term = state.selectedGlossaryTerm,
            onDismiss = { onEvent(ChatEvent.DismissGlossaryTerm) },
        )
    }

    // Foreman Re-explanation Dialog
    if (state.reexplainDialog != null) {
        ReexplainDialog(
            dialogState = state.reexplainDialog,
            onEvent = onEvent,
        )
    }
}

@Composable
internal fun GlossaryDialog(
    term: GlossaryTerm,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.chat_glossary_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${term.ja} (${term.reading})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "= ${term.en} / ${term.bn}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.chat_glossary_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.chat_close))
            }
        },
    )
}

@Composable
private fun RoleSwitch(role: Role, onEvent: (ChatEvent) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onEvent(ChatEvent.ToggleRole) },
    ) {
        Text(
            text = stringResource(if (role == Role.FOREMAN) R.string.chat_role_foreman else R.string.chat_role_worker),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}


/** The thin "N new messages" bar above the first message the reader has not seen (Messenger, Slack, Stream). */
@Composable
private fun UnreadDivider(count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        Text(
            pluralStringResource(R.plurals.chat_unread_divider, count, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    }
}
