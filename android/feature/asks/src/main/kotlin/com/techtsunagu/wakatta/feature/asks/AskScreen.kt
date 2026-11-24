package com.techtsunagu.wakatta.feature.asks

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.rememberDictation
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.asks.data.AskRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AskUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    @StringRes val errorRes: Int? = null,
    val isForeman: Boolean = false,
    val workerId: String = "",
    val workerName: String = "",
    val currentInput: String = "",
    val language: String = "en",
    val isThinking: Boolean = false,
    val asks: List<AskDto> = emptyList(),
    val expandedOriginalIds: Set<String> = emptySet(),
    val currentlySpeakingAskId: String? = null,
)

sealed interface AskEvent {
    data class QueryChanged(val query: String) : AskEvent
    data object SendClick : AskEvent
    data class SuggestionClick(val query: String) : AskEvent
    data class ToggleOriginalClick(val askId: String) : AskEvent
    data class SpeakClick(val askId: String, val text: String, val lang: String) : AskEvent
    data class OpenNoticeClick(val noticeId: String) : AskEvent
    data class ForwardAskClick(val askId: String) : AskEvent
    data object Retry : AskEvent
}

sealed interface AskEffect {
    data class NavigateToNotice(val noticeId: String) : AskEffect
    data class ShowToast(@StringRes val messageRes: Int) : AskEffect
}

@HiltViewModel
class AskViewModel @Inject constructor(
    private val askRepository: AskRepository,
    private val outboxWriter: OutboxWriter,
    private val speaker: Speaker,
    private val sessionStore: SessionStore,
) : MviViewModel<AskUiState, AskEvent, AskEffect>(AskUiState(isLoading = true)) {

    private val reload = MutableStateFlow(0)

    /** The last request that failed: the same question sent again keeps its key, so it is not asked twice. */
    private var failed: AskCreateRequest? = null

    init {
        viewModelScope.launch {
            combine(sessionStore.session, reload) { session, _ -> session }.collectLatest { session ->
                val isForeman = session?.user?.role == Role.FOREMAN
                val workerId = session?.user?.id.orEmpty()
                updateState {
                    it.copy(
                        isLoading = true,
                        errorRes = null,
                        isForeman = isForeman,
                        workerId = workerId,
                        workerName = session?.user?.displayName.orEmpty(),
                        language = session?.user?.language ?: "en",
                    )
                }
                askRepository.getAsks(if (isForeman) null else workerId)
                    .catch { updateState { it.copy(isLoading = false, errorRes = R.string.asks_error_load) } }
                    .collect { asks ->
                        updateState { it.copy(isLoading = false, isEmpty = asks.isEmpty(), asks = asks) }
                    }
            }
        }
    }

    override fun onEvent(event: AskEvent) {
        when (event) {
            is AskEvent.QueryChanged -> updateState { it.copy(currentInput = event.query) }
            is AskEvent.SendClick -> send(state.value.currentInput.trim())
            is AskEvent.SuggestionClick -> send(event.query)
            is AskEvent.ToggleOriginalClick -> toggleOriginal(event.askId)
            is AskEvent.SpeakClick -> handleSpeak(event.askId, event.text, event.lang)
            is AskEvent.OpenNoticeClick -> sendEffect(AskEffect.NavigateToNotice(event.noticeId))
            is AskEvent.ForwardAskClick -> forwardToSenpai(event.askId)
            is AskEvent.Retry -> reload.update { it + 1 }
        }
    }

    private fun send(query: String) {
        if (query.isEmpty() || state.value.isThinking) return
        val request = failed?.takeIf { it.text == query } ?: AskCreateRequest(text = query)
        val typed = state.value.currentInput
        updateState { it.copy(currentInput = "", isThinking = true) }
        viewModelScope.launch {
            val res = askRepository.sendAsk(state.value.workerId, request)
            failed = request.takeIf { res is AppResult.Failure }
            updateState {
                // A question that was not sent goes back in the box.
                val text = if (res is AppResult.Failure && it.currentInput.isEmpty()) typed else it.currentInput
                it.copy(isThinking = false, currentInput = text)
            }
            if (res is AppResult.Failure) sendEffect(AskEffect.ShowToast(R.string.asks_error_failed_to_send))
        }
    }

    private fun toggleOriginal(askId: String) {
        val current = state.value.expandedOriginalIds.toMutableSet()
        if (current.contains(askId)) current.remove(askId) else current.add(askId)
        updateState { it.copy(expandedOriginalIds = current) }
    }

    private fun handleSpeak(askId: String, text: String, lang: String) {
        if (state.value.currentlySpeakingAskId == askId) {
            speaker.stop()
            updateState { it.copy(currentlySpeakingAskId = null) }
        } else {
            speaker.speak(text, lang)
            updateState { it.copy(currentlySpeakingAskId = askId) }
        }
    }

    private fun forwardToSenpai(askId: String) {
        viewModelScope.launch {
            val res = askRepository.forwardAsk(askId)
            if (res is AppResult.Failure) {
                val noPair = res.error is AppError.Conflict
                sendEffect(AskEffect.ShowToast(if (noPair) R.string.asks_error_no_active_pair else R.string.asks_error_failed_to_send))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speaker.stop()
    }
}

@Composable
fun AskScreen(
    onBackClick: () -> Unit = {},
    onNavigateToNotice: (String) -> Unit = {},
    viewModel: AskViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AskEffect.NavigateToNotice -> onNavigateToNotice(effect.noticeId)
                is AskEffect.ShowToast -> Toast.makeText(context, effect.messageRes, Toast.LENGTH_LONG).show()
            }
        }
    }

    AskContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@Composable
fun AskContent(
    state: AskUiState,
    onEvent: (AskEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val title = if (state.isForeman) {
        stringResource(R.string.asks_foreman_asks_title)
    } else {
        stringResource(R.string.asks_ask_screen_title)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = title,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    LoadingState(message = stringResource(R.string.asks_loading))
                }
                state.error != null || state.errorRes != null -> {
                    val errorMsg = state.errorRes?.let { stringResource(it) } ?: state.error.orEmpty()
                    EmptyState(
                        title = stringResource(R.string.asks_error, errorMsg),
                        message = errorMsg,
                        actionLabel = stringResource(R.string.asks_retry),
                        onActionClick = { onEvent(AskEvent.Retry) },
                    )
                }
                state.isForeman -> {
                    ForemanAskInbox(state = state)
                }
                else -> {
                    WorkerAskChat(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerAskChat(
    state: AskUiState,
    onEvent: (AskEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.asks.size, state.isThinking) {
        if (state.asks.isNotEmpty()) {
            listState.animateScrollToItem(state.asks.size)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "welcome_card", contentType = "welcome") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.asks_welcome_msg, state.workerName.split(" ").first()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                    )
                }
            }

            items(
                items = state.asks,
                key = { it.id },
                contentType = { "ask_item" },
            ) { ask ->
                AskConversationItem(
                    ask = ask,
                    state = state,
                    onEvent = onEvent,
                )
            }

            if (state.isThinking) {
                item(key = "typing_indicator", contentType = "typing") {
                    Text(
                        text = stringResource(R.string.asks_typing),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        AskInputArea(
            currentInput = state.currentInput,
            language = state.language,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun AskConversationItem(
    ask: AskDto,
    state: AskUiState,
    onEvent: (AskEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorkerQuestionBubble(ask = ask)
        BotAnswerBubble(
            ask = ask,
            language = state.language,
            isOriginalExpanded = state.expandedOriginalIds.contains(ask.id),
            isSpeaking = state.currentlySpeakingAskId == ask.id,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun WorkerQuestionBubble(ask: AskDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (ask.kind == AskKind.VOICE) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.asks_mic_content_description),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = ask.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun BotAnswerBubble(
    ask: AskDto,
    language: String,
    isOriginalExpanded: Boolean,
    isSpeaking: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (ask.covered && ask.matchNotice != null) {
            NoticeCoveredCard(notice = ask.matchNotice, language = language, onEvent = onEvent)
        } else {
            SenpaiForwardedCard(
                ask = ask,
                language = language,
                isOriginalExpanded = isOriginalExpanded,
                isSpeaking = isSpeaking,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun NoticeCoveredCard(
    notice: com.techtsunagu.wakatta.feature.asks.model.NoticeMatchDto,
    language: String,
    onEvent: (AskEvent) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TriText(
                tri = notice.answer,
                lang = language,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
            if (notice.title.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = stringResource(R.string.asks_from_notice, pick(notice.title, language)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TriText(
                            tri = notice.snippet,
                            lang = language,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            TextButton(
                onClick = { onEvent(AskEvent.OpenNoticeClick(notice.id)) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_open_notice))
            }
        }
    }
}

@Composable
private fun SenpaiForwardedCard(
    ask: AskDto,
    language: String,
    isOriginalExpanded: Boolean,
    isSpeaking: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val noSenpai = ask.status == AskStatus.OPEN && ask.senpaiName == null
            Text(
                text = when {
                    noSenpai -> stringResource(R.string.asks_error_no_active_pair)
                    ask.senpaiName != null -> stringResource(R.string.asks_forwarding_to_senpai, ask.senpaiName)
                    else -> stringResource(R.string.asks_your_senpai)
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            if (ask.status == AskStatus.OPEN && !noSenpai) {
                OutlinedButton(
                    onClick = { onEvent(AskEvent.ForwardAskClick(ask.id)) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(text = stringResource(R.string.asks_forward))
                }
            }

            if (ask.status == AskStatus.FORWARDED) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = stringResource(R.string.asks_sent_to_senpai_waiting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else if (ask.status == AskStatus.REPLIED) {
                SenpaiReplySection(
                    ask = ask,
                    language = language,
                    isOriginalExpanded = isOriginalExpanded,
                    isSpeaking = isSpeaking,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun SenpaiReplySection(
    ask: AskDto,
    language: String,
    isOriginalExpanded: Boolean,
    isSpeaking: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
    val reply = ask.replyFor(language).orEmpty()
    val senpaiName = ask.senpaiEnName ?: ask.senpaiName ?: stringResource(R.string.asks_your_senpai)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Avatar(
                name = senpaiName,
                size = 28.dp,
                containerColor = Color(0xFFAD1457),
                contentColor = Color.White,
            )
            Text(
                text = senpaiName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = reply,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (reply != ask.replyJa) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.asks_translated_from_ja),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onEvent(
                        AskEvent.SpeakClick(
                            askId = ask.id,
                            text = reply,
                            lang = if (reply == ask.replyJa) "ja" else language,
                        ),
                    )
                },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(
                        if (isSpeaking) R.string.asks_stop_reading else R.string.asks_read_aloud,
                    ),
                    tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isOriginalExpanded && ask.replyJa != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = ask.replyJa,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }

        if (reply != ask.replyJa) {
            TextButton(
                onClick = { onEvent(AskEvent.ToggleOriginalClick(ask.id)) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(
                    text = stringResource(
                        if (isOriginalExpanded) R.string.asks_hide_original else R.string.asks_show_original,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AskInputArea(
    currentInput: String,
    language: String,
    onEvent: (AskEvent) -> Unit,
) {
    val dictation = rememberDictation(language, currentInput) { onEvent(AskEvent.QueryChanged(it)) }
    val isRecording = dictation.isListening
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        val suggestions = listOf(
            stringResource(R.string.asks_sug_garbage),
            stringResource(R.string.asks_sug_drill),
            stringResource(R.string.asks_sug_bus),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { sug ->
                WakattaChip(
                    text = sug,
                    selected = false,
                    onClick = { onEvent(AskEvent.SuggestionClick(sug)) },
                )
            }
        }

        dictation.errorRes?.let {
            Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = currentInput,
                onValueChange = { onEvent(AskEvent.QueryChanged(it)) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (isRecording) {
                            stringResource(R.string.asks_listening)
                        } else {
                            stringResource(R.string.asks_input_placeholder)
                        },
                        fontSize = 14.sp,
                    )
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
            )

            IconButton(
                onClick = dictation.toggle,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.asks_mic_content_description),
                    tint = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            IconButton(
                onClick = { onEvent(AskEvent.SendClick) },
                enabled = currentInput.isNotBlank() && !isRecording,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (currentInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.asks_send_content_description),
                    tint = if (currentInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ForemanAskInbox(state: AskUiState) {
    val forwarded = state.asks.filter { it.status == AskStatus.FORWARDED || it.status == AskStatus.OPEN }
    val replied = state.asks.filter { it.status == AskStatus.REPLIED }
    val answered = state.asks.filter { it.status == AskStatus.ANSWERED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "foreman_hint", contentType = "hint") {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.asks_foreman_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item(key = "forwarded_header", contentType = "header") {
            Text(
                text = stringResource(R.string.asks_forwarded_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (forwarded.isEmpty()) {
            item(key = "no_forwarded", contentType = "empty") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.asks_no_forwarded_asks),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        } else {
            items(
                items = forwarded,
                key = { "fwd_" + it.id },
                contentType = { "forwarded_item" },
            ) { ask ->
                ForemanForwardedAskCard(ask = ask)
            }
        }

        if (replied.isNotEmpty()) {
            item(key = "replied_header", contentType = "header") {
                Text(
                    text = stringResource(R.string.asks_replied_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(
                items = replied,
                key = { "rep_" + it.id },
                contentType = { "replied_item" },
            ) { ask ->
                ForemanRepliedAskCard(ask = ask)
            }
        }

        if (answered.isNotEmpty()) {
            item(key = "auto_answered_header", contentType = "header") {
                Text(
                    text = stringResource(R.string.asks_auto_answered_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(
                items = answered,
                key = { "ans_" + it.id },
                contentType = { "answered_item" },
            ) { ask ->
                ForemanAnsweredAskCard(ask = ask)
            }
        }
    }
}

@Composable
private fun ForemanForwardedAskCard(ask: AskDto) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Avatar(name = ask.workerName, size = 32.dp)
                Text(
                    text = ask.workerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = ask.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = ask.textJa ?: ask.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (ask.textJa != null) {
                Text(
                    text = ask.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(
                    if (ask.status == AskStatus.OPEN) R.string.asks_foreman_not_forwarded else R.string.asks_foreman_waiting,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ForemanRepliedAskCard(ask: AskDto) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name = ask.workerName, size = 28.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = ask.workerName, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = ask.createdAt, style = MaterialTheme.typography.labelSmall)
            }
            Text(text = ask.textJa ?: ask.text, style = MaterialTheme.typography.bodyMedium)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = ask.replyJa ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForemanAnsweredAskCard(ask: AskDto) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Avatar(name = ask.workerName, size = 28.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ask.textJa ?: ask.text, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = pick(ask.matchNotice?.answer, "ja"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
