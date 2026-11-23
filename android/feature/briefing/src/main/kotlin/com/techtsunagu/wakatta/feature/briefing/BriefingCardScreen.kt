package com.techtsunagu.wakatta.feature.briefing

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.briefing.data.BriefingRepository
import com.techtsunagu.wakatta.feature.briefing.data.userMessage
import com.techtsunagu.wakatta.feature.briefing.model.BriefingForWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

data class BriefingCardUiState(
    val id: String? = null,
    val briefing: BriefingForWorker? = null,
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val selectedOptionIndex: Int? = null,
    val isAnswered: Boolean = false,
    val status: WakattaStatus = WakattaStatus.WAIT,
    val isPassed: Boolean? = null,
    val correctAnswerText: String? = null,
)

sealed interface BriefingCardEvent {
    data class Init(val id: String?) : BriefingCardEvent
    data class SelectAnswer(val index: Int) : BriefingCardEvent
    data object ToggleReadAloud : BriefingCardEvent
    data object StopSpeech : BriefingCardEvent
    data object Retry : BriefingCardEvent
    data object Done : BriefingCardEvent
}

sealed interface BriefingCardEffect {
    data object Close : BriefingCardEffect
}

@HiltViewModel
class BriefingCardViewModel @Inject constructor(
    private val briefingRepository: BriefingRepository,
    private val receiptRepository: ReceiptRepository,
    private val checkRepository: CheckRepository,
    private val speaker: Speaker,
) : MviViewModel<BriefingCardUiState, BriefingCardEvent, BriefingCardEffect>(BriefingCardUiState()) {

    override fun onEvent(event: BriefingCardEvent) {
        when (event) {
            is BriefingCardEvent.Init -> {
                updateState { it.copy(id = event.id) }
                loadBriefing(event.id)
            }
            is BriefingCardEvent.SelectAnswer -> submitAnswer(event.index)
            is BriefingCardEvent.ToggleReadAloud -> toggleReadAloud()
            is BriefingCardEvent.StopSpeech -> stopSpeech()
            is BriefingCardEvent.Retry -> {
                updateState { it.copy(error = null) }
                loadBriefing(state.value.id)
            }
            is BriefingCardEvent.Done -> {
                stopSpeech()
                sendEffect(BriefingCardEffect.Close)
            }
        }
    }

    private fun loadBriefing(id: String?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            when (val result = briefingRepository.getBriefingForWorker(id)) {
                is AppResult.Success -> {
                    val briefing = result.value
                    // Mark opened via ReceiptRepository and BriefingRepository
                    receiptRepository.markOpened(CheckTargetKind.BRIEFING, briefing.id)
                    briefingRepository.markOpened(briefing.id)

                    val isAnswered = briefing.wakattaStatus != WakattaStatus.WAIT
                    val correctText = pick(briefing.question.options.firstOrNull(), "en")

                    updateState {
                        it.copy(
                            id = briefing.id,
                            briefing = briefing,
                            isLoading = false,
                            isEmpty = false,
                            isAnswered = isAnswered,
                            selectedOptionIndex = briefing.userAnswer,
                            status = briefing.wakattaStatus,
                            isPassed = briefing.passed,
                            correctAnswerText = correctText,
                        )
                    }
                }
                is AppResult.Failure -> {
                    updateState {
                        it.copy(
                            isLoading = false,
                            isEmpty = true,
                            error = result.error.userMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun submitAnswer(index: Int) {
        if (state.value.isAnswered || state.value.isSubmitting) return
        val currentBriefing = state.value.briefing ?: return

        updateState { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            stopSpeech()
            val idempotencyKey = UUID.randomUUID().toString()

            val answerList = if (index >= 0) listOf(index) else listOf(-1)
            checkRepository.submitCheck(
                targetKind = CheckTargetKind.BRIEFING,
                targetId = currentBriefing.id,
                language = "ja",
                answers = answerList,
                idempotencyKey = idempotencyKey,
            )

            val status = when {
                index == 0 -> WakattaStatus.GREEN
                index == -1 -> WakattaStatus.ORANGE
                else -> WakattaStatus.RED
            }
            val passed = status == WakattaStatus.GREEN
            val correctText = pick(currentBriefing.question.options.firstOrNull(), "en")

            briefingRepository.recordWorkerAnswer(currentBriefing.id, "u_rahim", index, status)

            updateState {
                it.copy(
                    isSubmitting = false,
                    isAnswered = true,
                    selectedOptionIndex = index,
                    status = status,
                    isPassed = passed,
                    correctAnswerText = correctText,
                )
            }
        }
    }

    private fun toggleReadAloud() {
        if (state.value.isSpeaking) {
            stopSpeech()
        } else {
            val briefing = state.value.briefing ?: return
            val focus = pick(briefing.focusText, "en")
            val change = pick(briefing.changeText, "en")
            val textToRead = "$focus. $change"
            speaker.speak(textToRead, "en")
            updateState { it.copy(isSpeaking = true) }
        }
    }

    private fun stopSpeech() {
        speaker.stop()
        updateState { it.copy(isSpeaking = false) }
    }

    override fun onCleared() {
        super.onCleared()
        speaker.stop()
    }
}

@Composable
fun BriefingCardScreen(
    id: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: BriefingCardViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) {
        viewModel.onEvent(BriefingCardEvent.Init(id))
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BriefingCardEffect.Close -> onBackClick()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(BriefingCardEvent.StopSpeech)
        }
    }

    BriefingCardContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BriefingCardContent(
    state: BriefingCardUiState,
    onEvent: (BriefingCardEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.briefing_card_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.briefing_close),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
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
                state.error != null && state.briefing == null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(BriefingCardEvent.Retry) },
                        retryLabel = stringResource(R.string.briefing_retry),
                    )
                }
                state.isEmpty || state.briefing == null -> {
                    EmptyState(
                        title = stringResource(R.string.briefing_card_screen_title),
                        message = stringResource(R.string.briefing_empty),
                        actionLabel = stringResource(R.string.briefing_retry),
                        onActionClick = { onEvent(BriefingCardEvent.Retry) },
                    )
                }
                else -> {
                    BriefingWorkerCardContent(
                        briefing = state.briefing,
                        isSpeaking = state.isSpeaking,
                        isAnswered = state.isAnswered,
                        selectedOptionIndex = state.selectedOptionIndex,
                        status = state.status,
                        correctAnswerText = state.correctAnswerText,
                        onSelectAnswer = { onEvent(BriefingCardEvent.SelectAnswer(it)) },
                        onToggleSpeech = { onEvent(BriefingCardEvent.ToggleReadAloud) },
                        onDone = { onEvent(BriefingCardEvent.Done) },
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BriefingWorkerCardContent(
    briefing: BriefingForWorker,
    isSpeaking: Boolean,
    isAnswered: Boolean,
    selectedOptionIndex: Int?,
    status: WakattaStatus,
    correctAnswerText: String?,
    onSelectAnswer: (Int) -> Unit,
    onToggleSpeech: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Foreman Header Item
        item(key = "from_item", contentType = "from") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(name = "Tanaka")
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tanaka Kenichi (Foreman)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = briefing.createdAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Safety Focus Card
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
                        text = pick(briefing.focusText, "en"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // One Change Card
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
                        text = pick(briefing.changeText, "en"),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // Read Aloud Button
        item(key = "read_aloud_btn", contentType = "button") {
            FilledTonalButton(
                onClick = onToggleSpeech,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpeaking) {
                        stringResource(R.string.briefing_card_stop_reading)
                    } else {
                        stringResource(R.string.briefing_card_read_aloud)
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        // 1 Quick Question Section Header
        item(key = "question_header", contentType = "header") {
            Text(
                text = stringResource(R.string.briefing_card_question_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Question and Options Card
        item(key = "question_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = pick(briefing.question.prompt, "en"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Options: regular options + "Not sure" (-1)
                    val optionList = briefing.question.options
                    optionList.forEachIndexed { index, optionTri ->
                        OptionButton(
                            text = pick(optionTri, "en"),
                            index = index,
                            isAnswered = isAnswered,
                            selectedOptionIndex = selectedOptionIndex,
                            isCorrect = index == 0,
                            onClick = { onSelectAnswer(index) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Not sure button
                    OptionButton(
                        text = stringResource(R.string.briefing_card_not_sure_option),
                        index = -1,
                        isAnswered = isAnswered,
                        selectedOptionIndex = selectedOptionIndex,
                        isCorrect = false,
                        onClick = { onSelectAnswer(-1) },
                    )

                    // Answer feedback card
                    if (isAnswered) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FeedbackBanner(
                            status = status,
                            correctAnswerText = correctAnswerText,
                        )
                    }
                }
            }
        }

        // Done button when answered
        if (isAnswered) {
            item(key = "done_button", contentType = "button") {
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.briefing_done),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    index: Int,
    isAnswered: Boolean,
    selectedOptionIndex: Int?,
    isCorrect: Boolean,
    onClick: () -> Unit,
) {
    val isSelected = selectedOptionIndex == index

    val (containerColor, contentColor, borderColor) = when {
        !isAnswered -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.outlineVariant,
        )
        isCorrect -> Triple(
            StatusColors.GreenContainer,
            StatusColors.OnGreenContainer,
            StatusColors.Green,
        )
        isSelected && !isCorrect -> Triple(
            StatusColors.RedContainer,
            StatusColors.OnRedContainer,
            StatusColors.Red,
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            Color.Transparent,
        )
    }

    OutlinedButton(
        onClick = onClick,
        enabled = !isAnswered,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor,
        ),
        border = BorderStroke(1.5.dp, borderColor),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected || (isAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal,
            )
            if (isAnswered) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = StatusColors.OnGreenContainer,
                        modifier = Modifier.size(20.dp),
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = StatusColors.OnRedContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private data class FeedbackConfig(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val title: String,
)

@Composable
private fun FeedbackBanner(
    status: WakattaStatus,
    correctAnswerText: String?,
) {
    val understoodTitle = stringResource(R.string.briefing_card_feedback_understood)
    val notSureTitle = stringResource(R.string.briefing_card_feedback_not_sure)
    val incorrectTitle = stringResource(R.string.briefing_card_feedback_incorrect)

    val config = when (status) {
        WakattaStatus.GREEN -> FeedbackConfig(
            containerColor = StatusColors.GreenContainer,
            contentColor = StatusColors.OnGreenContainer,
            icon = Icons.Default.CheckCircle,
            title = understoodTitle,
        )
        WakattaStatus.ORANGE -> FeedbackConfig(
            containerColor = StatusColors.OrangeContainer,
            contentColor = StatusColors.OnOrangeContainer,
            icon = Icons.Default.Warning,
            title = notSureTitle,
        )
        WakattaStatus.RED, WakattaStatus.WAIT, WakattaStatus.UNKNOWN -> FeedbackConfig(
            containerColor = StatusColors.RedContainer,
            contentColor = StatusColors.OnRedContainer,
            icon = Icons.Default.Warning,
            title = incorrectTitle,
        )
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = config.containerColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                tint = config.contentColor,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = config.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = config.contentColor,
                )
                if (status != WakattaStatus.GREEN && !correctAnswerText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.briefing_card_correct_answer, correctAnswerText),
                        style = MaterialTheme.typography.bodySmall,
                        color = config.contentColor,
                    )
                }
            }
        }
    }
}
