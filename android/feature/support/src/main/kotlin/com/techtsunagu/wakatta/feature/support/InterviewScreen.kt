package com.techtsunagu.wakatta.feature.support

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.WakattaShapes
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.InterviewMethod
import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.support.domain.InterviewCreateRequest
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueCalculator
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewPrepDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewQuestionItem
import com.techtsunagu.wakatta.feature.support.domain.InterviewRecordDto
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal data class InterviewUiState(
    val selectedWorkerId: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: SupportError? = null,
    val issue: InterviewIssue? = null,
    val dueInfo: InterviewDueDto? = null,
    val prepInfo: InterviewPrepDto? = null,
    val pastInterviews: List<InterviewRecordDto> = emptyList(),
    val workers: List<WorkerInfo> = emptyList(),
    val method: InterviewMethod = InterviewMethod.FACE,
    val consent: Boolean = false,
    val date: String = InterviewDueCalculator.DEMO_TODAY_ISO,
    val note: String = "",
) {
    val isEmpty: Boolean get() = dueInfo == null && prepInfo == null && !isLoading
}

internal sealed interface InterviewEvent {
    data class Init(val workerId: String?) : InterviewEvent
    data class SelectWorker(val workerId: String) : InterviewEvent
    data class SetMethod(val method: InterviewMethod) : InterviewEvent
    data class SetConsent(val consent: Boolean) : InterviewEvent
    data class SetDate(val date: String) : InterviewEvent
    data class SetNote(val note: String) : InterviewEvent
    data object SubmitInterview : InterviewEvent
    data object Retry : InterviewEvent
}

internal sealed interface InterviewEffect {
    data object Saved : InterviewEffect
    data class Failed(val error: SupportError) : InterviewEffect
}

/** The record rules from the prototype `saveIv`: note required, no future date, online needs consent. */
internal fun validateInterview(note: String, date: String, method: InterviewMethod, consent: Boolean): InterviewIssue? = when {
    note.isBlank() -> InterviewIssue.NOTE_REQUIRED
    date > InterviewDueCalculator.DEMO_TODAY_ISO -> InterviewIssue.FUTURE_DATE
    method == InterviewMethod.ONLINE && !consent -> InterviewIssue.CONSENT_REQUIRED
    else -> null
}

@HiltViewModel
internal class InterviewViewModel @Inject constructor(
    private val interviewRepository: InterviewRepository,
) : MviViewModel<InterviewUiState, InterviewEvent, InterviewEffect>(InterviewUiState()) {

    private var loadJob: Job? = null

    override fun onEvent(event: InterviewEvent) {
        when (event) {
            is InterviewEvent.Init -> load(event.workerId)
            is InterviewEvent.SelectWorker -> {
                updateState { it.copy(note = "", consent = false, issue = null) }
                load(event.workerId)
            }
            is InterviewEvent.SetMethod -> updateState { it.copy(method = event.method, issue = null) }
            is InterviewEvent.SetConsent -> updateState { it.copy(consent = event.consent, issue = null) }
            is InterviewEvent.SetDate -> updateState { it.copy(date = event.date, issue = null) }
            is InterviewEvent.SetNote -> updateState { it.copy(note = event.note, issue = null) }
            is InterviewEvent.SubmitInterview -> submitInterview()
            is InterviewEvent.Retry -> load(state.value.selectedWorkerId)
        }
    }

    /** A blank [workerId] picks the first worker from the repository. */
    private fun load(workerId: String?) {
        loadJob?.cancel()
        updateState { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            val workersRes = interviewRepository.getAllWorkers()
            val workers = workersRes.getOrNull().orEmpty()
            val id = workerId.takeUnless { it.isNullOrBlank() } ?: workers.firstOrNull()?.id.orEmpty()
            val dueRes = interviewRepository.getDueInfo(id)
            val prepRes = interviewRepository.getInterviewPrep(id)
            val pastRes = interviewRepository.getPastInterviews(id)

            val failure = listOf(workersRes, dueRes, prepRes, pastRes).firstNotNullOfOrNull { it.errorOrNull() }
            if (failure != null) {
                updateState { it.copy(isLoading = false, error = failure.toSupportError()) }
                return@launch
            }
            updateState {
                it.copy(
                    isLoading = false,
                    workers = workers,
                    dueInfo = dueRes.getOrNull(),
                    prepInfo = prepRes.getOrNull(),
                    pastInterviews = pastRes.getOrNull().orEmpty(),
                    selectedWorkerId = id,
                )
            }
        }
    }

    private fun submitInterview() {
        val s = state.value
        if (s.isSaving) return
        val issue = validateInterview(s.note, s.date, s.method, s.consent)
        if (issue != null) {
            updateState { it.copy(issue = issue) }
            return
        }
        updateState { it.copy(isSaving = true, issue = null) }
        viewModelScope.launch {
            val req = InterviewCreateRequest(s.selectedWorkerId, s.date, s.method, s.consent, s.note.trim())
            when (val res = interviewRepository.recordInterview(req)) {
                is AppResult.Success -> {
                    updateState {
                        it.copy(isSaving = false, note = "", consent = false, date = InterviewDueCalculator.DEMO_TODAY_ISO)
                    }
                    sendEffect(InterviewEffect.Saved)
                    load(s.selectedWorkerId)
                }
                is AppResult.Failure -> {
                    updateState { it.copy(isSaving = false) }
                    sendEffect(InterviewEffect.Failed(res.error.toSupportError()))
                }
            }
        }
    }
}

@Composable
internal fun InterviewScreen(
    workerId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: InterviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(workerId) { viewModel.onEvent(InterviewEvent.Init(workerId)) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            val message = when (effect) {
                InterviewEffect.Saved -> context.getString(R.string.support_interview_success)
                is InterviewEffect.Failed -> context.getString(effect.error.message)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    InterviewContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun InterviewContent(
    state: InterviewUiState,
    onEvent: (InterviewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.support_interview_screen_title),
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.support_loading),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.error != null -> {
                    ErrorState(
                        message = stringResource(state.error.message),
                        onRetry = { onEvent(InterviewEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.support_interview_screen_title),
                        message = stringResource(R.string.support_empty),
                        actionLabel = stringResource(R.string.support_retry),
                        onActionClick = { onEvent(InterviewEvent.Retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.dueInfo != null && state.prepInfo != null -> {
                    InterviewBody(
                        state = state,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
private fun InterviewBody(
    state: InterviewUiState,
    onEvent: (InterviewEvent) -> Unit,
) {
    val due = state.dueInfo ?: return
    val prep = state.prepInfo ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Worker selector chips
        item(key = "worker_selector", contentType = "selector") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.support_interview_select_worker),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.workers.forEach { worker ->
                        WakattaChip(
                            selected = state.selectedWorkerId == worker.id,
                            onClick = { onEvent(InterviewEvent.SelectWorker(worker.id)) },
                            leadingIcon = {
                                Avatar(name = worker.displayName, size = 24.dp)
                            },
                            label = { Text(worker.displayName.split(" ").first()) },
                        )
                    }
                }
            }
        }

        // Due Summary Card
        item(key = "due_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = due.workerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        val status = when (due.status) {
                            InterviewStatus.OK -> WakattaStatus.GREEN
                            InterviewStatus.SOON -> WakattaStatus.ORANGE
                            InterviewStatus.OVERDUE, InterviewStatus.UNKNOWN -> WakattaStatus.RED
                        }
                        StatusChip(status = status, label = dueLabel(due))
                    }
                    val dueDesc = due.dueDate?.let { stringResource(R.string.support_interview_next_due, it) }
                        ?: stringResource(R.string.support_interview_initial_due)
                    val lastDesc = stringResource(R.string.support_interview_last_date, due.lastDate ?: stringResource(R.string.support_log_none))
                    val freq = stringResource(R.string.support_interview_frequency)
                    Text(
                        text = "$dueDesc · $lastDesc · $freq",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Facts Section (前回からの記録)
        item(key = "facts_card", contentType = "card") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.support_interview_facts_title, prep.fromDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FactRow(
                            label = stringResource(R.string.support_interview_fact_meetups),
                            value = "${prep.meetupsCount}",
                        )
                        FactRow(
                            label = stringResource(R.string.support_interview_fact_notices),
                            value = "${prep.noticesOk}/${prep.noticesTotal}",
                        )
                        if (prep.confusingNotices.isNotEmpty()) {
                            FactRow(
                                label = stringResource(R.string.support_interview_fact_bad_notices),
                                value = prep.confusingNotices.joinToString("、"),
                            )
                        }
                        if (prep.confusionTerms.isNotEmpty()) {
                            FactRow(
                                label = stringResource(R.string.support_interview_fact_terms),
                                value = prep.confusionTerms.joinToString("、"),
                            )
                        }
                        if (prep.forwardedAsks.isNotEmpty()) {
                            FactRow(
                                label = stringResource(R.string.support_interview_fact_asks),
                                value = prep.forwardedAsks.joinToString("、"),
                            )
                        }
                    }
                }
            }
        }

        // Questions Section (面談で聞くこと)
        item(key = "questions_header", contentType = "header") {
            Text(
                text = stringResource(R.string.support_interview_questions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item(key = "questions_card", contentType = "card") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    prep.questions.forEachIndexed { idx, q ->
                        QuestionItemView(number = idx + 1, question = q)
                        if (idx < prep.questions.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Form Section (面談の記録)
        item(key = "form_header", contentType = "header") {
            Text(
                text = stringResource(R.string.support_interview_form_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item(key = "form_card", contentType = "form") {
            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Method selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.support_interview_method),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WakattaChip(
                                selected = state.method == InterviewMethod.FACE,
                                onClick = { onEvent(InterviewEvent.SetMethod(InterviewMethod.FACE)) },
                                label = { Text(stringResource(R.string.support_interview_method_face)) },
                            )
                            WakattaChip(
                                selected = state.method == InterviewMethod.ONLINE,
                                onClick = { onEvent(InterviewEvent.SetMethod(InterviewMethod.ONLINE)) },
                                label = { Text(stringResource(R.string.support_interview_method_online)) },
                            )
                        }
                    }

                    // Online consent checkbox
                    if (state.method == InterviewMethod.ONLINE) {
                        Surface(
                            shape = WakattaShapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = state.consent,
                                    onCheckedChange = { onEvent(InterviewEvent.SetConsent(it)) },
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.support_interview_consent_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Date input
                    OutlinedTextField(
                        value = state.date,
                        onValueChange = { onEvent(InterviewEvent.SetDate(it)) },
                        label = { Text(stringResource(R.string.support_interview_date)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )

                    // Notes / Actions
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { onEvent(InterviewEvent.SetNote(it)) },
                        label = { Text(stringResource(R.string.support_interview_notes)) },
                        placeholder = { Text(stringResource(R.string.support_interview_notes_placeholder)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp),
                        minLines = 3,
                    )

                    // Validation Error Banner
                    if (state.issue != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.support_icon_warning),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(state.issue.message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = { onEvent(InterviewEvent.SubmitInterview) },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.support_loading))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.support_interview_submit))
                        }
                    }
                }
            }
        }

        // Past interviews section
        if (state.pastInterviews.isNotEmpty()) {
            item(key = "past_header", contentType = "header") {
                Text(
                    text = stringResource(R.string.support_interview_past_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(
                items = state.pastInterviews,
                key = { "past_${it.id}" },
                contentType = { "past_row" },
            ) { record ->
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                val methodStr = if (record.method == InterviewMethod.ONLINE) {
                                    stringResource(R.string.support_interview_method_online)
                                } else {
                                    stringResource(R.string.support_interview_method_face)
                                }
                                Text(
                                    text = "${record.date} · $methodStr",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = record.recordedBy,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (record.noteJa.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = record.noteJa,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Disclaimer Note
        item(key = "disclaimer", contentType = "disclaimer") {
            Text(
                text = stringResource(R.string.support_interview_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        item(key = "bottom_space", contentType = "spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun QuestionItemView(number: Int, question: InterviewQuestionItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "$number. ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = question.ja,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = question.en,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = question.bn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
        }
    }
}
