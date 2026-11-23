package com.techtsunagu.wakatta.feature.notices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.feature.notices.data.NoticeRepository
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeCreateRequest
import com.techtsunagu.wakatta.feature.notices.model.NoticeDraftDto
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

enum class NoticeNewStage {
    PICK,
    RECORDING,
    BUSY,
    REVIEW,
}

data class SampleFlyerItem(
    val id: String,
    val captionJa: String,
    val captionEn: String,
)

data class NoticeNewUiState(
    val isAllowedToPost: Boolean = false,
    val stage: NoticeNewStage = NoticeNewStage.PICK,
    val selectedSourceKind: NoticeSourceKind = NoticeSourceKind.PHOTO,
    val customText: String = "",
    val selectedSampleId: String? = null,
    val isRecording: Boolean = false,
    val busyMessage: String = "",
    val draft: NoticeDraftDto? = null,
    val editableTitle: String = "",
    val events: List<EventDto> = emptyList(),
    val isPublishing: Boolean = false,
    val publishError: String? = null,
    val publishSuccess: Boolean = false,
    val publishedNoticeId: String? = null,
    val error: String? = null,
)

sealed interface NoticeNewEvent {
    data class SelectSourceKind(val kind: NoticeSourceKind) : NoticeNewEvent
    data class SelectSample(val sampleId: String) : NoticeNewEvent
    data class UpdateText(val text: String) : NoticeNewEvent
    data object ToggleRecording : NoticeNewEvent
    data object ProcessAI : NoticeNewEvent
    data class UpdateTitle(val title: String) : NoticeNewEvent
    data class ToggleEventConfirmed(val eventId: String) : NoticeNewEvent
    data object Publish : NoticeNewEvent
    data object Reset : NoticeNewEvent
    data object Retry : NoticeNewEvent
}

sealed interface NoticeNewEffect

@HiltViewModel
class NoticeNewViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val sessionStore: SessionStore,
    private val audioRecorder: AudioRecorder,
    private val photoCapture: PhotoCapture,
) : MviViewModel<NoticeNewUiState, NoticeNewEvent, NoticeNewEffect>(NoticeNewUiState()) {

    init {
        checkPosterRole()
    }

    private fun checkPosterRole() {
        viewModelScope.launch {
            val session = sessionStore.session.firstOrNull()
            val isPoster = session?.user?.role == Role.POSTER
            updateState { it.copy(isAllowedToPost = isPoster) }
        }
    }

    override fun onEvent(event: NoticeNewEvent) {
        when (event) {
            is NoticeNewEvent.SelectSourceKind -> updateState { it.copy(selectedSourceKind = event.kind) }
            is NoticeNewEvent.SelectSample -> handleSelectSample(event.sampleId)
            is NoticeNewEvent.UpdateText -> updateState { it.copy(customText = event.text) }
            is NoticeNewEvent.ToggleRecording -> handleToggleRecording()
            is NoticeNewEvent.ProcessAI -> processAi()
            is NoticeNewEvent.UpdateTitle -> updateState { it.copy(editableTitle = event.title) }
            is NoticeNewEvent.ToggleEventConfirmed -> handleToggleEventConfirmed(event.eventId)
            is NoticeNewEvent.Publish -> handlePublish()
            is NoticeNewEvent.Reset -> updateState {
                NoticeNewUiState(isAllowedToPost = it.isAllowedToPost)
            }
            is NoticeNewEvent.Retry -> updateState { it.copy(error = null, publishError = null) }
        }
    }

    private fun handleSelectSample(sampleId: String) {
        updateState { it.copy(selectedSampleId = sampleId) }
        processAi(sampleId = sampleId)
    }

    private fun handleToggleRecording() {
        val recording = state.value.isRecording
        if (!recording) {
            updateState { it.copy(isRecording = true, stage = NoticeNewStage.RECORDING) }
        } else {
            updateState { it.copy(isRecording = false) }
            processAi(sourceKind = NoticeSourceKind.VOICE)
        }
    }

    private fun processAi(
        sampleId: String? = state.value.selectedSampleId,
        sourceKind: NoticeSourceKind = state.value.selectedSourceKind,
    ) {
        viewModelScope.launch {
            val message = if (sourceKind == NoticeSourceKind.PHOTO) {
                "OCR: チラシの文字を読み取り中…"
            } else {
                "AI: やさしい日本語と英語にしています…"
            }
            updateState {
                it.copy(
                    stage = NoticeNewStage.BUSY,
                    busyMessage = message,
                    publishError = null,
                )
            }

            delay(300)
            val request = NoticeCreateRequest(
                sourceKind = sourceKind,
                text = state.value.customText.ifBlank { null },
                sampleId = sampleId,
            )

            when (val result = noticeRepository.createDraft(request)) {
                is AppResult.Success -> {
                    val draft = result.value
                    updateState {
                        it.copy(
                            stage = NoticeNewStage.REVIEW,
                            draft = draft,
                            editableTitle = draft.titleJa,
                            events = draft.events,
                            error = null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            stage = NoticeNewStage.PICK,
                            error = "Could not create notice draft",
                        )
                    }
                }
            }
        }
    }

    private fun handleToggleEventConfirmed(eventId: String) {
        val updatedEvents = state.value.events.map { ev ->
            if (ev.id == eventId) ev.copy(confirmed = !ev.confirmed) else ev
        }
        updateState { it.copy(events = updatedEvents, publishError = null) }
    }

    private fun handlePublish() {
        val currentDraft = state.value.draft ?: return
        val events = state.value.events

        val unconfirmed = events.any { !it.confirmed }
        if (unconfirmed) {
            updateState {
                it.copy(publishError = "EVENTS_NOT_CONFIRMED")
            }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isPublishing = true, publishError = null) }
            val titleMap: Tri = mapOf(
                "ja" to state.value.editableTitle.ifBlank { currentDraft.titleJa },
                "en" to currentDraft.titleEn,
                "bn" to currentDraft.titleBn,
            )
            val steps: List<StepDto> = currentDraft.stepsJa.mapIndexed { index, ja ->
                val en = currentDraft.stepsEn.getOrNull(index) ?: ""
                val bn = currentDraft.stepsBn.getOrNull(index) ?: ""
                StepDto(
                    text = mapOf("ja" to ja, "en" to en, "bn" to bn),
                    icon = "info",
                )
            }

            val result = noticeRepository.publishNotice(
                draftId = currentDraft.id,
                title = titleMap,
                steps = steps,
                events = events,
                question = currentDraft.question,
            )

            when (result) {
                is AppResult.Success -> {
                    if (result.value.success) {
                        updateState {
                            it.copy(
                                isPublishing = false,
                                publishSuccess = true,
                                publishedNoticeId = result.value.noticeId,
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                isPublishing = false,
                                publishError = result.value.error,
                            )
                        }
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isPublishing = false,
                            publishError = "Could not publish notice",
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeNewScreen(
    onBackClick: () -> Unit = {},
    onPublished: (String) -> Unit = {},
    viewModel: NoticeNewViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NoticeNewContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onPublished = onPublished,
    )
}

@Composable
fun NoticeNewContent(
    state: NoticeNewUiState,
    onEvent: (NoticeNewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPublished: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.notices_new_screen_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.isAllowedToPost) {
                item(key = "poster_gate_banner") {
                    PosterGateBanner()
                }
            }

            if (state.publishSuccess && state.publishedNoticeId != null) {
                item(key = "publish_success_card") {
                    PublishSuccessCard(
                        noticeId = state.publishedNoticeId,
                        onViewNotice = { onPublished(state.publishedNoticeId) },
                    )
                }
            } else {
                when (state.stage) {
                    NoticeNewStage.PICK -> {
                        pickStageContent(state = state, onEvent = onEvent)
                    }
                    NoticeNewStage.RECORDING -> {
                        item(key = "recording_card") {
                            RecordingCard(onStop = { onEvent(NoticeNewEvent.ToggleRecording) })
                        }
                    }
                    NoticeNewStage.BUSY -> {
                        item(key = "busy_card") {
                            BusyProgressCard(message = state.busyMessage)
                        }
                    }
                    NoticeNewStage.REVIEW -> {
                        reviewStageContent(state = state, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosterGateBanner() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.notices_create_notice_hidden),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.pickStageContent(
    state: NoticeNewUiState,
    onEvent: (NoticeNewEvent) -> Unit,
) {
    item(key = "source_selector") {
        SourceSegmentedBar(
            selectedKind = state.selectedSourceKind,
            onSelect = { onEvent(NoticeNewEvent.SelectSourceKind(it)) },
        )
    }

    when (state.selectedSourceKind) {
        NoticeSourceKind.PHOTO -> {
            item(key = "sample_flyers_header") {
                Text(
                    text = stringResource(R.string.notices_sample_flyers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item(key = "sample_flyers_list") {
                val samples = listOf(
                    SampleFlyerItem("smp_clean", "町内一斉清掃", "Neighborhood cleanup"),
                    SampleFlyerItem("smp_fire", "夜警・火の用心", "Night patrol: fire watch"),
                    SampleFlyerItem("smp_recycle", "古紙回収", "Waste paper collection"),
                )
                SampleFlyersRow(
                    samples = samples,
                    onSelect = { onEvent(NoticeNewEvent.SelectSample(it)) },
                )
            }
        }
        NoticeSourceKind.TEXT -> {
            item(key = "text_input_area") {
                TextInputBlock(
                    text = state.customText,
                    onTextChange = { onEvent(NoticeNewEvent.UpdateText(it)) },
                    onProcess = { onEvent(NoticeNewEvent.ProcessAI) },
                )
            }
        }
        NoticeSourceKind.VOICE -> {
            item(key = "voice_input_area") {
                VoiceInputBlock(
                    onStart = { onEvent(NoticeNewEvent.ToggleRecording) },
                )
            }
        }
        else -> Unit
    }
}

@Composable
private fun SourceSegmentedBar(
    selectedKind: NoticeSourceKind,
    onSelect: (NoticeSourceKind) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SourceTabButton(
                label = stringResource(R.string.notices_source_flyer),
                icon = Icons.Default.Image,
                isSelected = selectedKind == NoticeSourceKind.PHOTO,
                onClick = { onSelect(NoticeSourceKind.PHOTO) },
                modifier = Modifier.weight(1f),
            )
            SourceTabButton(
                label = stringResource(R.string.notices_source_text),
                icon = Icons.Default.TextFields,
                isSelected = selectedKind == NoticeSourceKind.TEXT,
                onClick = { onSelect(NoticeSourceKind.TEXT) },
                modifier = Modifier.weight(1f),
            )
            SourceTabButton(
                label = stringResource(R.string.notices_source_voice),
                icon = Icons.Default.Mic,
                isSelected = selectedKind == NoticeSourceKind.VOICE,
                onClick = { onSelect(NoticeSourceKind.VOICE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SourceTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SampleFlyersRow(
    samples: List<SampleFlyerItem>,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        samples.forEach { sample ->
            WakattaCard(
                onClick = { onSelect(sample.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sample.captionJa,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = sample.captionEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextInputBlock(
    text: String,
    onTextChange: (String) -> Unit,
    onProcess: () -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.notices_enter_text_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
            Button(
                onClick = onProcess,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.notices_process_ai))
            }
        }
    }
}

@Composable
private fun VoiceInputBlock(onStart: () -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onStart),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.notices_tap_to_talk),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.notices_tap_to_talk),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecordingCard(onStop: () -> Unit) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.notices_listening),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStop,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.notices_stop_read_aloud))
            }
        }
    }
}

@Composable
private fun BusyProgressCard(message: String) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.reviewStageContent(
    state: NoticeNewUiState,
    onEvent: (NoticeNewEvent) -> Unit,
) {
    val draft = state.draft ?: return

    item(key = "title_field") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notices_field_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.editableTitle,
                onValueChange = { onEvent(NoticeNewEvent.UpdateTitle(it)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    item(key = "steps_preview") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notices_field_steps),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    draft.stepsJa.forEachIndexed { i, ja ->
                        Text(
                            text = "${i + 1}. $ja",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }

    item(key = "events_preview") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notices_found_events),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (state.events.isEmpty()) {
                Text(
                    text = stringResource(R.string.notices_no_events_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        state.events.forEach { ev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = ev.confirmed,
                                    onCheckedChange = { onEvent(NoticeNewEvent.ToggleEventConfirmed(ev.id)) },
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = ev.title["ja"] ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "${ev.date} ${ev.time}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    item(key = "question_preview") {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notices_check_question_preview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = draft.question.prompt["ja"] ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    draft.question.options.forEachIndexed { i, opt ->
                        val isCorrect = i == draft.question.correctIndex
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isCorrect) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = opt["ja"] ?: "",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.publishError != null) {
        item(key = "publish_error") {
            val errorMsg = if (state.publishError == "EVENTS_NOT_CONFIRMED") {
                stringResource(R.string.notices_publish_events_unconfirmed)
            } else {
                state.publishError
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }

    item(key = "publish_btn") {
        Button(
            onClick = { onEvent(NoticeNewEvent.Publish) },
            enabled = !state.isPublishing,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.notices_publish_btn))
        }
    }
}

@Composable
private fun PublishSuccessCard(
    noticeId: String,
    onViewNotice: () -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.notices_publish_success),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onViewNotice,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.notices_notice_screen_title))
            }
        }
    }
}
