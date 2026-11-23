package com.techtsunagu.wakatta.feature.notices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.notices.data.NoticeRepository
import com.techtsunagu.wakatta.feature.notices.model.CheckResultDto
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeForPoster
import com.techtsunagu.wakatta.feature.notices.model.NoticeForWorker
import com.techtsunagu.wakatta.feature.notices.model.NoticeWorkerStatus
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class NoticeUiState(
    val id: String = "",
    val isLoading: Boolean = false,
    val isForeman: Boolean = false,
    val userLanguage: String = "ja",
    val workerNotice: NoticeForWorker? = null,
    val posterNotice: NoticeForPoster? = null,
    val selectedAnswer: Int? = null,
    val isSubmittingCheck: Boolean = false,
    val checkResult: CheckResultDto? = null,
    val isSpeaking: Boolean = false,
    val error: String? = null,
)

sealed interface NoticeEvent {
    data class Init(val id: String) : NoticeEvent
    data class SelectAnswer(val index: Int) : NoticeEvent
    data object SubmitCheck : NoticeEvent
    data class ToggleReminder(val eventId: String) : NoticeEvent
    data object ToggleSpeak : NoticeEvent
    data object Retry : NoticeEvent
    data object Refresh : NoticeEvent
}

sealed interface NoticeEffect

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val sessionStore: SessionStore,
    private val checkRepository: CheckRepository,
    private val receiptRepository: ReceiptRepository,
    private val speaker: Speaker,
) : MviViewModel<NoticeUiState, NoticeEvent, NoticeEffect>(NoticeUiState(isLoading = true)) {

    override fun onEvent(event: NoticeEvent) {
        when (event) {
            is NoticeEvent.Init -> loadNotice(event.id)
            is NoticeEvent.SelectAnswer -> updateState { it.copy(selectedAnswer = event.index) }
            is NoticeEvent.SubmitCheck -> submitCheck()
            is NoticeEvent.ToggleReminder -> toggleReminder(event.eventId)
            is NoticeEvent.ToggleSpeak -> toggleSpeak()
            is NoticeEvent.Retry -> loadNotice(state.value.id)
            is NoticeEvent.Refresh -> loadNotice(state.value.id)
        }
    }

    private fun loadNotice(id: String) {
        viewModelScope.launch {
            updateState { it.copy(id = id, isLoading = true, error = null) }
            val session = sessionStore.session.firstOrNull()
            val isForeman = session?.user?.role == Role.FOREMAN
            val userLang = session?.user?.language ?: if (isForeman) "ja" else "en"

            receiptRepository.markOpened(CheckTargetKind.NOTICE, id)
            noticeRepository.markNoticeOpened(id)

            if (isForeman) {
                when (val result = noticeRepository.getNoticeForPoster(id)) {
                    is AppResult.Success -> {
                        updateState {
                            it.copy(
                                isLoading = false,
                                isForeman = true,
                                userLanguage = userLang,
                                posterNotice = result.value,
                                error = null,
                            )
                        }
                    }
                    is AppResult.Failure -> {
                        updateState {
                            it.copy(isLoading = false, error = "Notice not found")
                        }
                    }
                }
            } else {
                when (val result = noticeRepository.getNoticeForWorker(id)) {
                    is AppResult.Success -> {
                        val notice = result.value
                        val initialResult = if (notice.passed != null) {
                            CheckResultDto(
                                passed = notice.passed,
                                status = notice.wakattaStatus,
                                message = if (notice.passed) "Wakatta! That is right." else "Completed.",
                            )
                        } else null
                        updateState {
                            it.copy(
                                isLoading = false,
                                isForeman = false,
                                userLanguage = userLang,
                                workerNotice = notice,
                                selectedAnswer = notice.userAnswer,
                                checkResult = initialResult,
                                error = null,
                            )
                        }
                    }
                    is AppResult.Failure -> {
                        updateState {
                            it.copy(isLoading = false, error = "Notice not found")
                        }
                    }
                }
            }
        }
    }

    private fun submitCheck() {
        val answer = state.value.selectedAnswer ?: return
        val noticeId = state.value.id
        viewModelScope.launch {
            updateState { it.copy(isSubmittingCheck = true) }
            val key = UUID.randomUUID().toString()
            checkRepository.submitCheck(
                targetKind = CheckTargetKind.NOTICE,
                targetId = noticeId,
                language = state.value.userLanguage,
                answers = listOf(answer),
                idempotencyKey = key,
            )
            when (val result = noticeRepository.submitNoticeCheck(noticeId, answer)) {
                is AppResult.Success -> {
                    val updatedNotice = noticeRepository.getNoticeForWorker(noticeId)
                    val notice = (updatedNotice as? AppResult.Success)?.value
                    updateState {
                        it.copy(
                            isSubmittingCheck = false,
                            checkResult = result.value,
                            workerNotice = notice ?: it.workerNotice,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isSubmittingCheck = false, error = "Could not submit check") }
                }
            }
        }
    }

    private fun toggleReminder(eventId: String) {
        viewModelScope.launch {
            noticeRepository.toggleReminder(eventId)
            val updated = noticeRepository.getNoticeForWorker(state.value.id)
            val notice = (updated as? AppResult.Success)?.value
            updateState { it.copy(workerNotice = notice ?: it.workerNotice) }
        }
    }

    private fun toggleSpeak() {
        val currentlySpeaking = state.value.isSpeaking
        if (currentlySpeaking) {
            speaker.stop()
            updateState { it.copy(isSpeaking = false) }
        } else {
            val title = state.value.workerNotice?.title ?: state.value.posterNotice?.title
            val steps = state.value.workerNotice?.steps ?: state.value.posterNotice?.steps.orEmpty()
            val lang = state.value.userLanguage
            val titleText = pick(title, lang)
            val stepsText = steps.joinToString(". ") { pick(it.text, lang) }
            val fullText = "$titleText. $stepsText"
            speaker.speak(fullText, lang)
            updateState { it.copy(isSpeaking = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speaker.stop()
    }
}

@Composable
fun NoticeScreen(
    id: String,
    onBackClick: () -> Unit = {},
    onAskClick: () -> Unit = {},
    viewModel: NoticeViewModel = viewModel(),
) {
    DisposableEffect(id) {
        viewModel.onEvent(NoticeEvent.Init(id))
        onDispose {}
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    NoticeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onAskClick = onAskClick,
    )
}

@Composable
fun NoticeContent(
    state: NoticeUiState,
    onEvent: (NoticeEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAskClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.notices_notice_screen_title),
                onBackClick = onBackClick,
                actions = {
                    val readAloudDesc = if (state.isSpeaking) {
                        stringResource(R.string.notices_stop_read_aloud)
                    } else {
                        stringResource(R.string.notices_read_aloud)
                    }
                    IconButton(
                        onClick = { onEvent(NoticeEvent.ToggleSpeak) },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = readAloudDesc },
                    ) {
                        Icon(
                            imageVector = if (state.isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (state.isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.notices_loading),
                    modifier = Modifier.padding(innerPadding),
                )
            }
            state.error != null -> {
                ErrorState(
                    message = state.error,
                    onRetry = { onEvent(NoticeEvent.Retry) },
                    retryLabel = stringResource(R.string.notices_retry),
                    modifier = Modifier.padding(innerPadding),
                )
            }
            state.workerNotice == null && state.posterNotice == null -> {
                EmptyState(
                    title = stringResource(R.string.notices_notice_screen_title),
                    message = stringResource(R.string.notices_empty),
                    modifier = Modifier.padding(innerPadding),
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(key = "notice_header") {
                        val title = state.workerNotice?.title ?: state.posterNotice?.title
                        val from = state.workerNotice?.from ?: state.posterNotice?.from ?: "自治会"
                        val date = state.workerNotice?.publishedAt ?: state.posterNotice?.publishedAt ?: ""
                        NoticeHeader(
                            title = title,
                            from = from,
                            date = date,
                            userLang = state.userLanguage,
                        )
                    }

                    val steps = state.workerNotice?.steps ?: state.posterNotice?.steps.orEmpty()
                    if (steps.isNotEmpty()) {
                        item(key = "header_steps") {
                            Text(
                                text = stringResource(R.string.notices_what_to_do),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "steps_card") {
                            StepsCard(
                                steps = steps,
                                userLang = state.userLanguage,
                                isForeman = state.isForeman,
                            )
                        }
                    }

                    val events = state.workerNotice?.events ?: state.posterNotice?.events.orEmpty()
                    if (events.isNotEmpty()) {
                        item(key = "header_dates") {
                            Text(
                                text = stringResource(R.string.notices_dates),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "dates_card") {
                            NoticeDatesCard(
                                events = events,
                                userLang = state.userLanguage,
                                isForeman = state.isForeman,
                                onToggleReminder = { onEvent(NoticeEvent.ToggleReminder(it)) },
                            )
                        }
                    }

                    item(key = "header_original") {
                        Text(
                            text = stringResource(R.string.notices_original),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    item(key = "original_content") {
                        OriginalSourceBlock(
                            sourceKind = state.workerNotice?.sourceKind ?: state.posterNotice?.sourceKind,
                            sourceText = state.workerNotice?.sourceText ?: state.posterNotice?.sourceText,
                            audioDuration = state.workerNotice?.audioDuration ?: state.posterNotice?.audioDuration,
                            from = state.workerNotice?.from ?: state.posterNotice?.from ?: "自治会",
                        )
                    }

                    if (state.isForeman && state.posterNotice != null) {
                        item(key = "header_check_results") {
                            Text(
                                text = stringResource(R.string.notices_check_results),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "foreman_results_card") {
                            ForemanResultsCard(notice = state.posterNotice)
                        }
                    } else if (state.workerNotice != null) {
                        item(key = "header_check") {
                            Text(
                                text = stringResource(R.string.notices_check),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "worker_check_card") {
                            WorkerCheckCard(
                                notice = state.workerNotice,
                                userLang = state.userLanguage,
                                selectedAnswer = state.selectedAnswer,
                                checkResult = state.checkResult,
                                isSubmitting = state.isSubmittingCheck,
                                onSelectAnswer = { onEvent(NoticeEvent.SelectAnswer(it)) },
                                onSubmit = { onEvent(NoticeEvent.SubmitCheck) },
                                onAskClick = onAskClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeHeader(
    title: Map<String, String>?,
    from: String,
    date: String,
    userLang: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TriText(
            tri = title,
            lang = userLang,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "$from · $date",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepsCard(
    steps: List<StepDto>,
    userLang: String,
    isForeman: Boolean,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        TriText(
                            tri = step.text,
                            lang = userLang,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (isForeman && userLang != "en") {
                            val enAlt = pick(step.text, "en")
                            if (enAlt.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = enAlt,
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

@Composable
private fun NoticeDatesCard(
    events: List<EventDto>,
    userLang: String,
    isForeman: Boolean,
    onToggleReminder: (String) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            events.forEach { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TriText(
                            tri = event.title,
                            lang = userLang,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "${event.date} ${event.time}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!isForeman) {
                        val reminderLabel = if (event.isReminderOn) {
                            stringResource(R.string.notices_reminder_on)
                        } else {
                            stringResource(R.string.notices_add_reminder)
                        }
                        OutlinedButton(
                            onClick = { onToggleReminder(event.id) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Icon(
                                imageVector = if (event.isReminderOn) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = reminderLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalSourceBlock(
    sourceKind: com.techtsunagu.wakatta.core.model.NoticeSourceKind?,
    sourceText: String?,
    audioDuration: String?,
    from: String,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            when (sourceKind) {
                com.techtsunagu.wakatta.core.model.NoticeSourceKind.PHOTO -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.notices_photo_flyer),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$from · チラシ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                com.techtsunagu.wakatta.core.model.NoticeSourceKind.VOICE -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.notices_audio_recording, audioDuration ?: "0:20"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (!sourceText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "「$sourceText」",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    Text(
                        text = sourceText ?: "自治会よりお知らせです。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$from · テキスト",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerCheckCard(
    notice: NoticeForWorker,
    userLang: String,
    selectedAnswer: Int?,
    checkResult: CheckResultDto?,
    isSubmitting: Boolean,
    onSelectAnswer: (Int) -> Unit,
    onSubmit: () -> Unit,
    onAskClick: () -> Unit,
) {
    val isAnswered = checkResult != null
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            TriText(
                tri = notice.question.prompt,
                lang = userLang,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(16.dp))

            notice.question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                val border = if (isSelected) {
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = border,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(enabled = !isAnswered) {
                            onSelectAnswer(index)
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(26.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${('A'.code + index).toChar()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        TriText(
                            tri = option,
                            lang = userLang,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isAnswered) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            onSelectAnswer(-1)
                            onSubmit()
                        },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(text = stringResource(R.string.notices_not_sure))
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = selectedAnswer != null && !isSubmitting,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(text = "OK")
                    }
                }
            } else {
                CheckResultFeedback(
                    checkResult = checkResult,
                    onAskClick = onAskClick,
                )
            }
        }
    }
}

@Composable
private fun CheckResultFeedback(
    checkResult: CheckResultDto,
    onAskClick: () -> Unit,
) {
    val statusColor = StatusColors.forStatus(checkResult.status)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusColor.containerColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (checkResult.status) {
                    WakattaStatus.GREEN -> Icons.Default.Check
                    WakattaStatus.RED -> Icons.Default.Close
                    else -> Icons.Default.Info
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor.onContainerColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                val message = when (checkResult.status) {
                    WakattaStatus.GREEN -> stringResource(R.string.notices_status_green_msg)
                    WakattaStatus.ORANGE -> stringResource(R.string.notices_status_orange_msg)
                    WakattaStatus.RED -> stringResource(
                        R.string.notices_status_red_msg,
                        checkResult.correctAnswerText ?: "",
                    )
                    else -> checkResult.message
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor.onContainerColor,
                )
            }
            if (checkResult.status == WakattaStatus.RED) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAskClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.notices_ask_question_btn))
                }
            }
        }
    }
}

@Composable
private fun ForemanResultsCard(notice: NoticeForPoster) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = pick(notice.question.prompt, "ja"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            val correctOption = notice.question.options.getOrNull(notice.question.correctIndex)?.get("ja") ?: ""
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.notices_correct_answer, correctOption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(14.dp))

            notice.workerStatuses.forEach { ws ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Avatar(
                            name = ws.worker.displayName,
                            size = 32.dp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = ws.worker.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            val answerDesc = when {
                                ws.status == WakattaStatus.WAIT -> stringResource(R.string.notices_waiting_answer)
                                ws.answerIndex == -1 -> stringResource(R.string.notices_answered_not_sure)
                                ws.answerText != null -> stringResource(R.string.notices_answer_prefix, ws.answerText)
                                else -> stringResource(R.string.notices_waiting_answer)
                            }
                            Text(
                                text = answerDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    StatusChip(status = ws.status)
                }
            }
        }
    }
}
