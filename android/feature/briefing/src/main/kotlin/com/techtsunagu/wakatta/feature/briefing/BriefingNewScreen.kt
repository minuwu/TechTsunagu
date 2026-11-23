package com.techtsunagu.wakatta.feature.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.briefing.data.BriefingRepository
import com.techtsunagu.wakatta.feature.briefing.data.userMessage
import com.techtsunagu.wakatta.feature.briefing.model.BriefingDraft
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForForeman
import com.techtsunagu.wakatta.feature.briefing.model.BriefingPublishRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class BriefingNewPhase {
    IDLE,
    RECORDING,
    TRANSLATING,
    READY,
}

data class BriefingNewUiState(
    val phase: BriefingNewPhase = BriefingNewPhase.IDLE,
    val pastBriefings: List<BriefingForForeman> = emptyList(),
    val draft: BriefingDraft? = null,
    val recordedText: String = "",
    val recordingSeconds: Int = 0,
    val isLoading: Boolean = false,
    val isPublishing: Boolean = false,
    val showPublishConfirm: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true,
)

sealed interface BriefingNewEvent {
    data object Init : BriefingNewEvent
    data object StartRecording : BriefingNewEvent
    data object StopRecording : BriefingNewEvent
    data class DictateInstead(val text: String) : BriefingNewEvent
    data object RedoDraft : BriefingNewEvent
    data object RequestPublish : BriefingNewEvent
    data object ConfirmPublish : BriefingNewEvent
    data object DismissPublishConfirm : BriefingNewEvent
    data object Retry : BriefingNewEvent
    data object Refresh : BriefingNewEvent
}

sealed interface BriefingNewEffect {
    data class NavigateToBoard(val id: String) : BriefingNewEffect
}

@HiltViewModel
class BriefingNewViewModel @Inject constructor(
    private val repository: BriefingRepository,
    private val audioRecorder: AudioRecorder,
) : MviViewModel<BriefingNewUiState, BriefingNewEvent, BriefingNewEffect>(BriefingNewUiState()) {

    private var recordingJob: Job? = null
    private var typingJob: Job? = null

    private val cannedDictation = "おはようございます。今日の安全の重点は、プレスでの手の挟まれ防止です。作業の前に、必ず安全カバーが閉まっているか確認してください。変更が一つあります。2号プレスの金型交換は午後に行います。午前中は使えません。"

    init {
        loadPastBriefings()
    }

    private fun loadPastBriefings() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val result = repository.getBriefings()) {
                is AppResult.Success -> {
                    updateState { it.copy(isLoading = false, pastBriefings = result.value) }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isLoading = false, error = result.error.userMessage()) }
                }
            }
        }
    }

    override fun onEvent(event: BriefingNewEvent) {
        when (event) {
            is BriefingNewEvent.Init -> loadPastBriefings()
            is BriefingNewEvent.Refresh -> loadPastBriefings()
            is BriefingNewEvent.Retry -> {
                updateState { it.copy(error = null) }
                loadPastBriefings()
            }
            is BriefingNewEvent.StartRecording -> startRecording()
            is BriefingNewEvent.StopRecording -> stopRecording()
            is BriefingNewEvent.DictateInstead -> {
                updateState { it.copy(recordedText = event.text) }
                processDictation(event.text)
            }
            is BriefingNewEvent.RedoDraft -> {
                updateState { it.copy(phase = BriefingNewPhase.IDLE, draft = null, recordedText = "") }
            }
            is BriefingNewEvent.RequestPublish -> {
                updateState { it.copy(showPublishConfirm = true) }
            }
            is BriefingNewEvent.DismissPublishConfirm -> {
                updateState { it.copy(showPublishConfirm = false) }
            }
            is BriefingNewEvent.ConfirmPublish -> publishDraft()
        }
    }

    private fun startRecording() {
        updateState {
            it.copy(
                phase = BriefingNewPhase.RECORDING,
                recordedText = "",
                recordingSeconds = 0,
                error = null,
            )
        }
        recordingJob = audioRecorder.start()
            .onEach { (_, elapsed) ->
                updateState { it.copy(recordingSeconds = elapsed) }
            }
            .launchIn(viewModelScope)

        // Type out canned speech preview for visual demo
        typingJob = viewModelScope.launch {
            var index = 0
            while (index < cannedDictation.length) {
                delay(70)
                index = (index + 4).coerceAtMost(cannedDictation.length)
                val chunk = cannedDictation.substring(0, index)
                updateState { it.copy(recordedText = chunk) }
            }
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        typingJob?.cancel()
        typingJob = null

        val spokenText = state.value.recordedText.ifBlank { cannedDictation }
        updateState { it.copy(phase = BriefingNewPhase.TRANSLATING) }

        viewModelScope.launch {
            audioRecorder.stop()
            processDictation(spokenText)
        }
    }

    private fun processDictation(text: String) {
        viewModelScope.launch {
            updateState { it.copy(phase = BriefingNewPhase.TRANSLATING, error = null) }
            val key = UUID.randomUUID().toString()
            when (val result = repository.createDraft(text = text, audioFile = null, idempotencyKey = key)) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(
                            phase = BriefingNewPhase.READY,
                            draft = result.value,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            phase = BriefingNewPhase.IDLE,
                            error = result.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun publishDraft() {
        val currentDraft = state.value.draft ?: return
        if (!state.value.isOnline) {
            updateState {
                it.copy(
                    showPublishConfirm = false,
                    error = "Cannot publish while offline. Check connection.",
                )
            }
            return
        }

        updateState { it.copy(isPublishing = true, showPublishConfirm = false, error = null) }
        viewModelScope.launch {
            val request = BriefingPublishRequest(
                id = currentDraft.id,
                text = currentDraft.text,
                focusText = currentDraft.focusText,
                changeText = currentDraft.changeText,
                question = currentDraft.question,
            )
            when (val result = repository.publishBriefing(request)) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(
                            isPublishing = false,
                            phase = BriefingNewPhase.IDLE,
                            draft = null,
                            recordedText = "",
                        )
                    }
                    sendEffect(BriefingNewEffect.NavigateToBoard(result.value.id))
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isPublishing = false,
                            error = result.error.userMessage(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BriefingNewScreen(
    onBackClick: () -> Unit = {},
    onNavigateToBoard: (String) -> Unit = {},
    viewModel: BriefingNewViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BriefingNewEffect.NavigateToBoard -> onNavigateToBoard(effect.id)
            }
        }
    }

    BriefingNewContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onNavigateToBoard = onNavigateToBoard,
    )
}

@Composable
fun BriefingNewContent(
    state: BriefingNewUiState,
    onEvent: (BriefingNewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToBoard: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.briefing_new_screen_title),
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
                    LoadingState(message = stringResource(R.string.briefing_loading))
                }
                state.error != null && state.phase == BriefingNewPhase.IDLE && state.pastBriefings.isEmpty() -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(BriefingNewEvent.Retry) },
                        retryLabel = stringResource(R.string.briefing_retry),
                    )
                }
                state.phase == BriefingNewPhase.TRANSLATING -> {
                    TranslatingSection(modifier = Modifier.padding(16.dp))
                }
                state.phase == BriefingNewPhase.READY && state.draft != null -> {
                    DraftReviewSection(
                        draft = state.draft,
                        isPublishing = state.isPublishing,
                        onRedo = { onEvent(BriefingNewEvent.RedoDraft) },
                        onPublish = { onEvent(BriefingNewEvent.RequestPublish) },
                        modifier = Modifier.padding(16.dp),
                    )
                }
                else -> {
                    // Phase: IDLE or RECORDING
                    RecordAndPastSection(
                        state = state,
                        onEvent = onEvent,
                        onNavigateToBoard = onNavigateToBoard,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            if (state.showPublishConfirm) {
                AlertDialog(
                    onDismissRequest = { onEvent(BriefingNewEvent.DismissPublishConfirm) },
                    title = { Text(stringResource(R.string.briefing_new_confirm_publish_title)) },
                    text = { Text(stringResource(R.string.briefing_new_confirm_publish_message)) },
                    confirmButton = {
                        TextButton(
                            onClick = { onEvent(BriefingNewEvent.ConfirmPublish) },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(stringResource(R.string.briefing_new_confirm_publish_action))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { onEvent(BriefingNewEvent.DismissPublishConfirm) },
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(stringResource(R.string.briefing_new_cancel))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun RecordAndPastSection(
    state: BriefingNewUiState,
    onEvent: (BriefingNewEvent) -> Unit,
    onNavigateToBoard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRecording = state.phase == BriefingNewPhase.RECORDING

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "record_card", contentType = "record_card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (isRecording) {
                            stringResource(R.string.briefing_new_mic_recording_prompt)
                        } else {
                            stringResource(R.string.briefing_new_mic_idle_prompt)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    val micBgColor = if (isRecording) {
                        StatusColors.Red
                    } else {
                        MaterialTheme.colorScheme.primary
                    }

                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable {
                                if (isRecording) {
                                    onEvent(BriefingNewEvent.StopRecording)
                                } else {
                                    onEvent(BriefingNewEvent.StartRecording)
                                }
                            },
                        shape = CircleShape,
                        color = micBgColor,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) {
                                    stringResource(R.string.briefing_new_mic_action_stop)
                                } else {
                                    stringResource(R.string.briefing_new_mic_action_record)
                                },
                                tint = Color.White,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                    }

                    if (isRecording) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = String.format("%02d:%02d", state.recordingSeconds / 60, state.recordingSeconds % 60),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusColors.Red,
                        )
                        if (state.recordedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = state.recordedText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!isRecording && state.pastBriefings.isNotEmpty()) {
            item(key = "past_title", contentType = "header") {
                Text(
                    text = stringResource(R.string.briefing_new_past_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            items(
                items = state.pastBriefings,
                key = { it.id },
                contentType = { "briefing_item" },
            ) { briefing ->
                WakattaCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToBoard(briefing.id) },
                ) {
                    WakattaListItem(
                        headlineContent = {
                            Text(
                                text = pick(briefing.focusText, "ja"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = briefing.createdAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TranslatingSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.briefing_new_translating_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DraftReviewSection(
    draft: BriefingDraft,
    isPublishing: Boolean,
    onRedo: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "spoken_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_new_spoken_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item(key = "spoken_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = draft.transcriptJa,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        item(key = "english_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_new_english_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item(key = "focus_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = stringResource(R.string.briefing_new_tag_safety_focus),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pick(draft.focusText, "en"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        item(key = "change_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = stringResource(R.string.briefing_new_tag_one_change),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pick(draft.changeText, "en"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        item(key = "question_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_new_question_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item(key = "question_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pick(draft.question.prompt, "en"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = pick(draft.question.prompt, "ja"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    draft.question.options.forEachIndexed { idx, opt ->
                        val isCorrect = idx == draft.question.correctIndex
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.small,
                            color = if (isCorrect) StatusColors.GreenContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusColors.OnGreenContainer,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column {
                                    Text(
                                        text = pick(opt, "en"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCorrect) StatusColors.OnGreenContainer else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = pick(opt, "ja"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCorrect) StatusColors.OnGreenContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "action_buttons", contentType = "buttons") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onRedo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isPublishing,
                ) {
                    Text(stringResource(R.string.briefing_new_btn_redo))
                }
                Button(
                    onClick = onPublish,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isPublishing,
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.briefing_new_btn_publish))
                }
            }
        }
    }
}
