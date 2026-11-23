package com.techtsunagu.wakatta.feature.asks

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
import androidx.compose.material3.ButtonDefaults
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
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.feature.asks.data.AskRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AskUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val isForeman: Boolean = false,
    val workerId: String = "u_rahim",
    val workerName: String = "Rahim Uddin",
    val currentInput: String = "",
    val isRecording: Boolean = false,
    val isThinking: Boolean = false,
    val asks: List<AskDto> = emptyList(),
    val expandedOriginalIds: Set<String> = emptySet(),
    val currentlySpeakingAskId: String? = null,
)

sealed interface AskEvent {
    data class QueryChanged(val query: String) : AskEvent
    data object SendClick : AskEvent
    data class SuggestionClick(val query: String) : AskEvent
    data object MicClick : AskEvent
    data class ToggleOriginalClick(val askId: String) : AskEvent
    data class SpeakClick(val askId: String, val text: String, val lang: String) : AskEvent
    data class OpenNoticeClick(val noticeId: String) : AskEvent
    data class CannedReplyClick(val askId: String, val replyIndex: Int) : AskEvent
    data class ForwardAskClick(val askId: String) : AskEvent
    data object Retry : AskEvent
    data object Refresh : AskEvent
}

sealed interface AskEffect {
    data class NavigateToNotice(val noticeId: String) : AskEffect
    data class ShowToast(val message: String) : AskEffect
}

@HiltViewModel
class AskViewModel @Inject constructor(
    private val askRepository: AskRepository,
    private val audioRecorder: AudioRecorder,
    private val outboxWriter: OutboxWriter,
    private val speaker: Speaker,
    private val sessionStore: SessionStore,
) : MviViewModel<AskUiState, AskEvent, AskEffect>(AskUiState(isLoading = true)) {

    init {
        observeSessionAndAsks()
    }

    private fun observeSessionAndAsks() {
        viewModelScope.launch {
            sessionStore.session.collectLatest { session ->
                val isForeman = session?.user?.role == Role.FOREMAN
                val workerId = session?.user?.id ?: "u_rahim"
                val workerName = session?.user?.displayName ?: "Rahim Uddin"

                updateState {
                    it.copy(
                        isForeman = isForeman,
                        workerId = workerId,
                        workerName = workerName,
                    )
                }

                val filterId = if (isForeman) null else workerId
                askRepository.getAsks(filterId).collectLatest { asksList ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isEmpty = asksList.isEmpty(),
                            asks = asksList,
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(event: AskEvent) {
        when (event) {
            is AskEvent.QueryChanged -> updateState { it.copy(currentInput = event.query) }
            is AskEvent.SendClick -> sendCurrentInput()
            is AskEvent.SuggestionClick -> sendSpecificQuery(event.query)
            is AskEvent.MicClick -> toggleMicRecording()
            is AskEvent.ToggleOriginalClick -> toggleOriginal(event.askId)
            is AskEvent.SpeakClick -> handleSpeak(event.askId, event.text, event.lang)
            is AskEvent.OpenNoticeClick -> sendEffect(AskEffect.NavigateToNotice(event.noticeId))
            is AskEvent.CannedReplyClick -> sendCannedReply(event.askId, event.replyIndex)
            is AskEvent.ForwardAskClick -> forwardToSenpai(event.askId)
            is AskEvent.Retry -> updateState { it.copy(isLoading = false, error = null) }
            is AskEvent.Refresh -> updateState { it.copy(isLoading = false) }
        }
    }

    private fun sendCurrentInput() {
        val query = state.value.currentInput.trim()
        if (query.isEmpty()) return
        updateState { it.copy(currentInput = "", isThinking = true) }
        viewModelScope.launch {
            when (val res = askRepository.sendAsk(state.value.workerId, AskCreateRequest(text = query))) {
                is AppResult.Success -> updateState { it.copy(isThinking = false) }
                is AppResult.Failure -> updateState { it.copy(isThinking = false, error = "Failed to send ask") }
            }
        }
    }

    private fun sendSpecificQuery(query: String) {
        updateState { it.copy(isThinking = true) }
        viewModelScope.launch {
            when (val res = askRepository.sendAsk(state.value.workerId, AskCreateRequest(text = query))) {
                is AppResult.Success -> updateState { it.copy(isThinking = false) }
                is AppResult.Failure -> updateState { it.copy(isThinking = false, error = "Failed to send ask") }
            }
        }
    }

    private fun toggleMicRecording() {
        if (state.value.isRecording) {
            updateState { it.copy(isRecording = false, isThinking = true) }
            viewModelScope.launch {
                val recording = audioRecorder.stop()
                // The text is only the mock stand-in for the transcript; the live repository sends the recording.
                val voiceQuery = "Where can I buy a bus pass?"
                askRepository.sendAsk(
                    state.value.workerId,
                    AskCreateRequest(text = voiceQuery, isVoice = true, audioFile = recording),
                )
                updateState { it.copy(isThinking = false) }
            }
        } else {
            updateState { it.copy(isRecording = true) }
            viewModelScope.launch {
                audioRecorder.start().collectLatest { }
            }
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

    private fun sendCannedReply(askId: String, replyIndex: Int) {
        val (ja, en, bn) = when (replyIndex) {
            0 -> Triple(
                "いい質問ですね。明日の昼休みに、いっしょに調べましょう。",
                "Good question. Let us look it up together at lunch tomorrow.",
                "ভালো প্রশ্ন। আসুন আগামীকাল দুপুরের খাবারের সময় একসাথে খোঁজ করি。",
            )
            1 -> Triple(
                "市役所の窓口で聞くといいですよ。私もついて行きます。",
                "It is best to ask at the city office. I will go with you.",
                "সিটি অফিসের কাউন্টারে জিজ্ঞাসা করাই ভালো। আমিও আপনার সাথে যাব।",
            )
            else -> Triple(
                "今度の土曜日に、いっしょに行きましょう。",
                "Let us go together this Saturday.",
                "আসুন এই শনিবার একসাথে যাই।",
            )
        }
        viewModelScope.launch {
            askRepository.replyAsk(askId, ja, en, bn)
        }
    }

    private fun forwardToSenpai(askId: String) {
        viewModelScope.launch {
            when (val res = askRepository.forwardAsk(askId)) {
                is AppResult.Success -> { }
                is AppResult.Failure -> updateState { it.copy(error = "No active Senpai pair") }
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
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AskEffect.NavigateToNotice -> onNavigateToNotice(effect.noticeId)
                is AskEffect.ShowToast -> { }
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
                state.error != null -> {
                    EmptyState(
                        title = stringResource(R.string.asks_error, state.error),
                        message = state.error,
                        actionLabel = stringResource(R.string.asks_retry),
                        onActionClick = { onEvent(AskEvent.Retry) },
                    )
                }
                state.isForeman -> {
                    ForemanAskInbox(
                        state = state,
                        onEvent = onEvent,
                    )
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
            isRecording = state.isRecording,
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
            NoticeCoveredCard(notice = ask.matchNotice, onEvent = onEvent)
        } else {
            SenpaiForwardedCard(
                ask = ask,
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
    onEvent: (AskEvent) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TriText(
                tri = notice.answer,
                lang = "en",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(R.string.asks_from_notice, notice.title["ja"] ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TriText(
                        tri = notice.snippet,
                        lang = "en",
                        style = MaterialTheme.typography.bodySmall,
                    )
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
    isOriginalExpanded: Boolean,
    isSpeaking: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
    val senpaiName = ask.senpaiName ?: "佐藤 美咲"
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.asks_forwarding_to_senpai, senpaiName),
                style = MaterialTheme.typography.bodyMedium,
            )

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
            } else if (ask.status == AskStatus.REPLIED && ask.replyEn != null) {
                SenpaiReplySection(
                    ask = ask,
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
    isOriginalExpanded: Boolean,
    isSpeaking: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Avatar(
                name = ask.senpaiName ?: "佐",
                size = 28.dp,
                containerColor = Color(0xFFAD1457),
                contentColor = Color.White,
            )
            Text(
                text = ask.senpaiEnName ?: ask.senpaiName ?: "Sato Misaki",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = ask.replyEn ?: "",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    onEvent(
                        AskEvent.SpeakClick(
                            askId = ask.id,
                            text = ask.replyEn ?: "",
                            lang = "en-US",
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

@Composable
private fun AskInputArea(
    currentInput: String,
    isRecording: Boolean,
    onEvent: (AskEvent) -> Unit,
) {
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
                onClick = { onEvent(AskEvent.MicClick) },
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
                enabled = currentInput.isNotBlank(),
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
private fun ForemanAskInbox(
    state: AskUiState,
    onEvent: (AskEvent) -> Unit,
) {
    val forwarded = state.asks.filter { it.status == AskStatus.FORWARDED }
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
                ForemanForwardedAskCard(ask = ask, onEvent = onEvent)
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
private fun ForemanForwardedAskCard(
    ask: AskDto,
    onEvent: (AskEvent) -> Unit,
) {
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
            Text(
                text = ask.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.asks_canned_replies_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val cannedList = listOf(
                "いい質問ですね。明日の昼休みに、いっしょに調べましょう。",
                "市役所の窓口で聞くといいですよ。私もついて行きます。",
                "今度の土曜日に、いっしょに行きましょう。",
            )

            cannedList.forEachIndexed { index, replyJa ->
                OutlinedButton(
                    onClick = { onEvent(AskEvent.CannedReplyClick(ask.id, index)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp),
                ) {
                    Text(text = replyJa, fontSize = 13.sp)
                }
            }
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
                    text = ask.matchNotice?.title?.get("ja") ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
