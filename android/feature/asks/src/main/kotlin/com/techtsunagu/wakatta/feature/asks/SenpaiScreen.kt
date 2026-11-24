package com.techtsunagu.wakatta.feature.asks

import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
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
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TodayWord
import com.techtsunagu.wakatta.core.model.TodayWordSource
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.asks.data.MeetupRepository
import com.techtsunagu.wakatta.feature.asks.data.SenpaiRepository
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import com.techtsunagu.wakatta.feature.asks.model.TodayWordDto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SENPAI_NAME_MAX = 40

data class SenpaiUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    @StringRes val errorRes: Int? = null,
    val isForeman: Boolean = false,
    val workerId: String = "",
    val workerName: String = "",
    val pair: SenpaiPairDto? = null,
    val pairs: List<SenpaiPairDto> = emptyList(),
    val meetups: List<MeetupDto> = emptyList(),
    val todayWord: TodayWordDto? = null,
    val isMeetupSheetOpen: Boolean = false,
    val meetupDraftKind: String = "coffee",
    val meetupDraftDate: String = "",
    val meetupDraftNote: String = "",
    val workersWithoutSenpai: List<WorkerInfo> = emptyList(),
    val isPairSheetOpen: Boolean = false,
    val pairDraftWorkerId: String = "",
    val pairDraftName: String = "",
    val pairDraftKind: String = "coworker",
    val createdPairToken: String? = null,
)

sealed interface SenpaiEvent {
    data class OpenMeetupSheet(val kind: String = "coffee") : SenpaiEvent
    data object CloseMeetupSheet : SenpaiEvent
    data class MeetupKindChanged(val kind: String) : SenpaiEvent
    data class MeetupDateChanged(val date: String) : SenpaiEvent
    data class MeetupNoteChanged(val note: String) : SenpaiEvent
    data object SaveMeetupClick : SenpaiEvent
    data object OpenPairSheet : SenpaiEvent
    data object ClosePairSheet : SenpaiEvent
    data class PairDraftChanged(val workerId: String, val name: String, val kind: String) : SenpaiEvent
    data object CreatePairLinkClick : SenpaiEvent
    data object DismissTokenDialog : SenpaiEvent
    data object AskQuestionClick : SenpaiEvent
    data object Retry : SenpaiEvent
}

sealed interface SenpaiEffect {
    data object NavigateToAsk : SenpaiEffect
    data class ShowToast(@StringRes val messageRes: Int) : SenpaiEffect
}

@HiltViewModel
class SenpaiViewModel @Inject constructor(
    private val senpaiRepository: SenpaiRepository,
    private val meetupRepository: MeetupRepository,
    private val todayWordSource: TodayWordSource,
    private val sessionStore: SessionStore,
    private val workerRepository: WorkerRepository,
) : MviViewModel<SenpaiUiState, SenpaiEvent, SenpaiEffect>(SenpaiUiState(isLoading = true)) {

    private val reload = MutableStateFlow(0)

    /** The last Meet-up that failed: the same one saved again keeps its key, so it is not logged twice. */
    private var failed: MeetupCreateRequest? = null

    init {
        viewModelScope.launch {
            combine(sessionStore.session, reload) { session, _ -> session?.user }.collectLatest { user ->
                updateState {
                    it.copy(
                        isLoading = true,
                        errorRes = null,
                        isForeman = user?.role == Role.FOREMAN,
                        workerId = user?.id.orEmpty(),
                        workerName = user?.displayName.orEmpty(),
                    )
                }
                try {
                    if (user?.role == Role.FOREMAN) collectForeman() else collectWorker(user?.id.orEmpty())
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) { // a repository flow throws when its list cannot be loaded
                    updateState { it.copy(isLoading = false, errorRes = R.string.asks_error_load) }
                }
            }
        }
    }

    private suspend fun collectForeman() = coroutineScope {
        launch {
            senpaiRepository.getAllSenpaiPairs().collectLatest { pairs ->
                val paired = pairs.map { it.workerId }.toSet()
                val workers = (workerRepository.getWorkers() as? AppResult.Success)?.value.orEmpty()
                updateState { s -> s.copy(pairs = pairs, workersWithoutSenpai = workers.filter { it.id !in paired }, isLoading = false) }
            }
        }
        launch { meetupRepository.getAllMeetups().collect { meetups -> updateState { it.copy(meetups = meetups) } } }
        launch { todayWordSource.getTodayWord().collect { word -> updateState { it.copy(todayWord = word?.toDto()) } } }
    }

    private suspend fun collectWorker(workerId: String) = coroutineScope {
        launch {
            senpaiRepository.getSenpaiPairForWorker(workerId).collect { pair -> updateState { it.copy(pair = pair, isLoading = false) } }
        }
        launch { meetupRepository.getMeetupsForWorker(workerId).collect { meetups -> updateState { it.copy(meetups = meetups) } } }
    }

    private fun TodayWord.toDto() = TodayWordDto(
        workerId = "",
        workerName = "",
        languageName = "",
        word = term,
        reading = reading,
        meaningJa = meaning["ja"].orEmpty(),
        meaningEn = meaning["en"].orEmpty(),
        meaningBn = meaning["bn"].orEmpty(),
    )

    override fun onEvent(event: SenpaiEvent) {
        when (event) {
            is SenpaiEvent.OpenMeetupSheet -> updateState {
                it.copy(
                    isMeetupSheetOpen = true,
                    meetupDraftKind = event.kind,
                    meetupDraftDate = LocalDate.now(TOKYO).toString(),
                    meetupDraftNote = "",
                )
            }
            is SenpaiEvent.CloseMeetupSheet -> updateState { it.copy(isMeetupSheetOpen = false) }
            is SenpaiEvent.MeetupKindChanged -> updateState { it.copy(meetupDraftKind = event.kind) }
            is SenpaiEvent.MeetupDateChanged -> updateState { it.copy(meetupDraftDate = event.date) }
            is SenpaiEvent.MeetupNoteChanged -> updateState { it.copy(meetupDraftNote = event.note) }
            is SenpaiEvent.SaveMeetupClick -> saveMeetup()
            is SenpaiEvent.OpenPairSheet -> updateState {
                it.copy(
                    isPairSheetOpen = true,
                    pairDraftWorkerId = it.workersWithoutSenpai.firstOrNull()?.id.orEmpty(),
                    pairDraftName = "",
                    pairDraftKind = "coworker",
                )
            }
            is SenpaiEvent.ClosePairSheet -> updateState { it.copy(isPairSheetOpen = false) }
            is SenpaiEvent.PairDraftChanged -> updateState {
                it.copy(pairDraftWorkerId = event.workerId, pairDraftName = event.name, pairDraftKind = event.kind)
            }
            is SenpaiEvent.CreatePairLinkClick -> createPairLink()
            is SenpaiEvent.DismissTokenDialog -> updateState { it.copy(createdPairToken = null) }
            is SenpaiEvent.AskQuestionClick -> sendEffect(SenpaiEffect.NavigateToAsk)
            is SenpaiEvent.Retry -> reload.update { it + 1 }
        }
    }

    private fun saveMeetup() {
        val s = state.value
        val date = runCatching { LocalDate.parse(s.meetupDraftDate.trim()) }.getOrNull()
        if (date == null || date.isAfter(LocalDate.now(TOKYO))) return sendEffect(SenpaiEffect.ShowToast(R.string.asks_error_future_date))
        val draft = MeetupCreateRequest(workerId = s.workerId, kind = s.meetupDraftKind, date = date.toString(), note = s.meetupDraftNote)
        val req = failed?.takeIf { it.copy(idempotencyKey = draft.idempotencyKey) == draft } ?: draft
        viewModelScope.launch {
            val res = meetupRepository.logMeetup(s.workerId, req)
            failed = req.takeIf { res is AppResult.Failure }
            if (res is AppResult.Success) {
                updateState { it.copy(isMeetupSheetOpen = false) }
                reload.update { it + 1 }
            }
            sendEffect(SenpaiEffect.ShowToast(if (res is AppResult.Success) R.string.asks_meetup_logged_toast else R.string.asks_error_save))
        }
    }

    private fun createPairLink() {
        val s = state.value
        if (s.pairDraftWorkerId.isEmpty() || s.pairDraftName.isBlank()) return
        viewModelScope.launch {
            val req = SenpaiPairCreateRequest(s.pairDraftWorkerId, s.pairDraftName.trim(), s.pairDraftKind)
            when (val res = senpaiRepository.createSenpaiPair(req)) {
                is AppResult.Success -> {
                    updateState { it.copy(isPairSheetOpen = false, createdPairToken = res.value.token) }
                    reload.update { it + 1 }
                }
                is AppResult.Failure -> {
                    val taken = res.error is AppError.Conflict
                    sendEffect(SenpaiEffect.ShowToast(if (taken) R.string.asks_error_pair_exists else R.string.asks_error_save))
                }
            }
        }
    }

    private companion object {
        val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
    }
}

@Composable
fun SenpaiScreen(
    onBackClick: () -> Unit = {},
    onNavigateToAsk: () -> Unit = {},
    viewModel: SenpaiViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SenpaiEffect.NavigateToAsk -> onNavigateToAsk()
                is SenpaiEffect.ShowToast -> Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
            }
        }
    }

    SenpaiContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@Composable
fun SenpaiContent(
    state: SenpaiUiState,
    onEvent: (SenpaiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.asks_senpai_screen_title),
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
                        onActionClick = { onEvent(SenpaiEvent.Retry) },
                    )
                }
                state.isForeman -> {
                    ForemanSenpaiView(state = state, onEvent = onEvent)
                }
                else -> {
                    WorkerSenpaiView(state = state, onEvent = onEvent)
                }
            }

            if (state.isMeetupSheetOpen) {
                LogMeetupDialog(state = state, onEvent = onEvent)
            }

            if (state.isPairSheetOpen) {
                CreatePairDialog(state = state, onEvent = onEvent)
            }

            if (state.createdPairToken != null) {
                SenpaiTokenDialog(
                    token = state.createdPairToken,
                    onDismiss = { onEvent(SenpaiEvent.DismissTokenDialog) },
                )
            }
        }
    }
}

@Composable
private fun WorkerSenpaiView(
    state: SenpaiUiState,
    onEvent: (SenpaiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "senpai_pair_card", contentType = "pair") {
            if (state.pair == null) {
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.asks_error_no_active_pair),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                WorkerPairHeaderCard(
                    senpaiName = state.pair.senpaiName,
                    senpaiEnName = state.pair.senpaiEnName,
                    onEvent = onEvent,
                )
            }
        }

        if (state.pair != null) {
            item(key = "log_meetup", contentType = "log_meetup") {
                Button(
                    onClick = { onEvent(SenpaiEvent.OpenMeetupSheet()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 48.dp),
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.asks_log_meetup))
                }
            }
        }

        item(key = "meetups_header", contentType = "header") {
            Text(
                text = stringResource(R.string.asks_meetups_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.meetups.isEmpty()) {
            item(key = "empty_meetups", contentType = "empty") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.asks_empty_meetups),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            items(
                items = state.meetups,
                key = { "mu_" + it.id },
                contentType = { "meetup_item" },
            ) { meetup ->
                MeetupItemCard(meetup = meetup, isForeman = false)
            }
        }
    }
}

@Composable
private fun WorkerPairHeaderCard(
    senpaiName: String,
    senpaiEnName: String?,
    onEvent: (SenpaiEvent) -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar(
                name = senpaiName.take(1).ifEmpty { "S" },
                size = 56.dp,
                containerColor = Color(0xFFAD1457),
                contentColor = Color.White,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.asks_your_senpai),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = senpaiEnName ?: senpaiName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (senpaiEnName != null) {
                    Text(
                        text = senpaiName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onEvent(SenpaiEvent.AskQuestionClick) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.asks_ask_question))
                }
            }
        }
    }
}

@Composable
private fun ForemanSenpaiView(
    state: SenpaiUiState,
    onEvent: (SenpaiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        state.todayWord?.let { word ->
            item(key = "today_word_card", contentType = "word") {
                TodayWordCard(word = word)
            }
        }

        item(key = "pairs_header", contentType = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.asks_senpai_pairs_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            R.string.asks_senpai_pairs_subtitle,
                            state.pairs.size,
                            state.meetups.size,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = { onEvent(SenpaiEvent.OpenPairSheet) },
                    enabled = state.workersWithoutSenpai.isNotEmpty(),
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(text = stringResource(R.string.asks_create_pair_link), fontSize = 12.sp)
                }
            }
        }

        items(
            items = state.pairs,
            key = { "pair_" + it.id },
            contentType = { "pair_item" },
        ) { pair ->
            ForemanPairItemCard(pair = pair)
        }

        item(key = "meetups_header_fm", contentType = "header") {
            Text(
                text = stringResource(R.string.asks_meetups_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        items(
            items = state.meetups,
            key = { "fm_mu_" + it.id },
            contentType = { "meetup_item" },
        ) { meetup ->
            MeetupItemCard(meetup = meetup, isForeman = true)
        }
    }
}

@Composable
private fun TodayWordCard(word: TodayWordDto) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.asks_today_word_title, word.workerName, word.languageName),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "${word.reading} = ${word.meaningJa}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.asks_today_word_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ForemanPairItemCard(pair: SenpaiPairDto) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(name = pair.workerName, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pair.workerName} ← ${pair.senpaiName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.asks_pair_meetups, pair.meetupCount, pair.lastMeetupDate ?: "—"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MeetupItemCard(meetup: MeetupDto, isForeman: Boolean) {
    val icon = when (meetup.kind) {
        "coffee" -> Icons.Default.Favorite
        "drill" -> Icons.Default.Warning
        "festival" -> Icons.Default.DateRange
        else -> Icons.Default.People
    }
    val kindLabel = when (meetup.kind) {
        "coffee" -> stringResource(R.string.asks_kind_coffee)
        "drill" -> stringResource(R.string.asks_kind_drill)
        "festival" -> stringResource(R.string.asks_kind_festival)
        else -> stringResource(R.string.asks_kind_other)
    }

    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = kindLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                val targetName = if (isForeman) meetup.workerName else stringResource(R.string.asks_with_senpai, meetup.senpaiName)
                Text(
                    text = "${meetup.date} · $targetName",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (meetup.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meetup.note,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogMeetupDialog(
    state: SenpaiUiState,
    onEvent: (SenpaiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(SenpaiEvent.CloseMeetupSheet) },
        title = {
            Text(
                text = stringResource(R.string.asks_log_meetup_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.asks_kind_label),
                    style = MaterialTheme.typography.labelSmall,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        "coffee" to stringResource(R.string.asks_kind_coffee),
                        "drill" to stringResource(R.string.asks_kind_drill),
                        "festival" to stringResource(R.string.asks_kind_festival),
                        "other" to stringResource(R.string.asks_kind_other),
                    ).forEach { (k, l) ->
                        FilterChip(
                            selected = state.meetupDraftKind == k,
                            onClick = { onEvent(SenpaiEvent.MeetupKindChanged(k)) },
                            label = { Text(l) },
                        )
                    }
                }

                OutlinedTextField(
                    value = state.meetupDraftDate,
                    onValueChange = { onEvent(SenpaiEvent.MeetupDateChanged(it)) },
                    label = { Text(stringResource(R.string.asks_date_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.meetupDraftNote,
                    onValueChange = { onEvent(SenpaiEvent.MeetupNoteChanged(it)) },
                    label = { Text(stringResource(R.string.asks_note_label)) },
                    placeholder = { Text(stringResource(R.string.asks_note_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(SenpaiEvent.SaveMeetupClick) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(SenpaiEvent.CloseMeetupSheet) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_cancel))
            }
        },
    )
}

@Composable
private fun CreatePairDialog(
    state: SenpaiUiState,
    onEvent: (SenpaiEvent) -> Unit,
) {
    fun change(workerId: String = state.pairDraftWorkerId, name: String = state.pairDraftName, kind: String = state.pairDraftKind) =
        onEvent(SenpaiEvent.PairDraftChanged(workerId, name, kind))
    AlertDialog(
        onDismissRequest = { onEvent(SenpaiEvent.ClosePairSheet) },
        title = {
            Text(
                text = stringResource(R.string.asks_create_pair_link),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = stringResource(R.string.asks_worker_label), style = MaterialTheme.typography.labelSmall)
                ChipRow(
                    options = state.workersWithoutSenpai.map { it.id to it.displayName },
                    selected = state.pairDraftWorkerId,
                    onSelect = { change(workerId = it) },
                )
                OutlinedTextField(
                    value = state.pairDraftName,
                    onValueChange = { change(name = it.take(SENPAI_NAME_MAX)) },
                    label = { Text(stringResource(R.string.asks_senpai_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                ChipRow(
                    options = listOf(
                        "coworker" to stringResource(R.string.asks_pair_kind_coworker),
                        "jichikai" to stringResource(R.string.asks_pair_kind_jichikai),
                    ),
                    selected = state.pairDraftKind,
                    onSelect = { change(kind = it) },
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(SenpaiEvent.CreatePairLinkClick) },
                enabled = state.pairDraftName.isNotBlank() && state.pairDraftWorkerId.isNotEmpty(),
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(SenpaiEvent.ClosePairSheet) },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_cancel))
            }
        },
    )
}

@Composable
private fun ChipRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (key, label) ->
            FilterChip(selected = selected == key, onClick = { onSelect(key) }, label = { Text(label) })
        }
    }
}

@Composable
private fun SenpaiTokenDialog(
    token: String,
    onDismiss: () -> Unit,
) {
    // shortcut: the web app has no address yet, so the link is only its path until ApiConfig.WEB_BASE_URL is set.
    val link = "${ApiConfig.WEB_BASE_URL}/senpai/$token"
    val context = LocalContext.current
    AlertDialog(
        // The link is shown only once: only the button closes the dialog, not a tap outside it.
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.asks_pair_link_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.asks_pair_link_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = link,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_close))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, link)
                    context.startActivity(Intent.createChooser(send, null))
                },
                modifier = Modifier.sizeIn(minHeight = 48.dp),
            ) {
                Text(text = stringResource(R.string.asks_share_link))
            }
        },
    )
}
