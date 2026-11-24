package com.techtsunagu.wakatta.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.lifecycle.viewModelScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role as UserRole
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TaskColumn
import com.techtsunagu.wakatta.core.model.TaskRepository
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.core.navigation.AnalyticsKey
import com.techtsunagu.wakatta.core.navigation.AskKey
import com.techtsunagu.wakatta.core.navigation.BoardKey
import com.techtsunagu.wakatta.core.navigation.BriefingCardKey
import com.techtsunagu.wakatta.core.navigation.BriefingNewKey
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.CourseKey
import com.techtsunagu.wakatta.core.navigation.FollowUpsKey
import com.techtsunagu.wakatta.core.navigation.LearningKey
import com.techtsunagu.wakatta.core.navigation.LifeKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.MachinesKey
import com.techtsunagu.wakatta.core.navigation.NearMissListKey
import com.techtsunagu.wakatta.core.navigation.NearMissNewKey
import com.techtsunagu.wakatta.core.navigation.NoteKey
import com.techtsunagu.wakatta.core.navigation.NotificationsKey
import com.techtsunagu.wakatta.core.navigation.ScanKey
import com.techtsunagu.wakatta.core.navigation.SkillsKey
import com.techtsunagu.wakatta.core.navigation.SupportLogKey
import com.techtsunagu.wakatta.core.navigation.TaskKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.techtsunagu.wakatta.feature.home.data.HomeBoardSource
import javax.inject.Inject

data class AttentionCardItem(
    val id: String,
    val name: String,
    val message: String,
    val status: WakattaStatus,
)

data class LockedMachineItem(
    val id: String,
    val name: String,
    val lockedWorkersText: String,
)

data class BriefingItem(
    val id: String,
    val focusText: String,
)

data class PendingCheckItem(
    val id: String,
    val fromName: String,
    val time: String,
    val text: String,
    val threadId: String,
)

data class HomeTaskItem(
    val id: String,
    val title: String,
    val dueText: String,
)

data class HomeMachineItem(
    val id: String,
    val name: String,
    val isReady: Boolean,
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val isForeman: Boolean = false,
    val userName: String = "",
    val userLanguage: String = "ja",
    val dateText: String = "",
    // Foreman data
    val followUpCount: Int = 0,
    val followUpDetailUser: String = "",
    val followUpDetailTerm: String = "",
    val tallyGreen: Int = 0,
    val tallyOrange: Int = 0,
    val tallyRed: Int = 0,
    val openTasksCount: Int = 0,
    val lockedMachinesCount: Int = 0,
    val attentionCards: List<AttentionCardItem> = emptyList(),
    val lockedMachines: List<LockedMachineItem> = emptyList(),
    // Worker data
    val todayBriefing: BriefingItem? = null,
    val pendingChecks: List<PendingCheckItem> = emptyList(),
    val myTasks: List<HomeTaskItem> = emptyList(),
    val myMachines: List<HomeMachineItem> = emptyList(),
    val unreadNotificationsCount: Int = 0,
)

sealed interface HomeEvent {
    data object Retry : HomeEvent
    data object Refresh : HomeEvent
    data object BellClick : HomeEvent
    data object FollowUpClick : HomeEvent
    data object BriefingClick : HomeEvent
    data object NearMissClick : HomeEvent
    data object AnalyticsClick : HomeEvent
    data object SkillsClick : HomeEvent
    data object LifeClick : HomeEvent
    data object SupportLogClick : HomeEvent
    data object BoardTileClick : HomeEvent
    data object MachinesTileClick : HomeEvent
    data object AttentionItemClick : HomeEvent
    data class LockedMachineClick(val machineId: String) : HomeEvent
    data class PendingCheckClick(val threadId: String) : HomeEvent
    data class TaskClick(val taskId: String) : HomeEvent
    data class MachineClick(val machineId: String, val isReady: Boolean) : HomeEvent
    data object ScanMachineClick : HomeEvent
    data object ReportNearMissClick : HomeEvent
    data object AskClick : HomeEvent
    data object SafetyCourseClick : HomeEvent
    data object MyLearningClick : HomeEvent
}

sealed interface HomeEffect {
    data class Navigate(val key: Any) : HomeEffect
}

private val NO_NETWORK = setOf(AppError.Offline, AppError.Timeout)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    private val taskRepository: TaskRepository,
    private val boardSource: HomeBoardSource,
    private val sessionStore: SessionStore,
    private val languageManager: LanguageManager,
) : MviViewModel<HomeUiState, HomeEvent, HomeEffect>(HomeUiState(isLoading = true)) {

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = it.userName.isEmpty(), error = null) }
            val user = sessionStore.session.firstOrNull()?.user ?: return@launch
            val lang = languageManager.currentLanguage.value

            val machinesCall = async { machineRepository.getMachines() }
            val tasksCall = async { taskRepository.getTasks() }
            val machinesResult = machinesCall.await()
            val tasksResult = tasksCall.await()
            if (tasksResult is AppResult.Failure && tasksResult.error in NO_NETWORK && state.value.userName.isNotEmpty()) {
                // A refresh without a network keeps what is shown.
                updateState { it.copy(isLoading = false) }
                return@launch
            }
            // One failed part leaves its card empty. Both failed means offline or the server is down.
            if (machinesResult is AppResult.Failure && tasksResult is AppResult.Failure) {
                updateState { it.copy(isLoading = false, error = tasksResult.error.toString()) }
                return@launch
            }
            val openTasks = tasksResult.orNull().orEmpty().filter { it.column != TaskColumn.DONE }
            updateState {
                it.copy(
                    isForeman = user.role == UserRole.FOREMAN,
                    userName = user.displayName,
                    userLanguage = lang,
                    dateText = LocalDate.now().format(
                        if (lang == "ja") DateTimeFormatter.ofPattern("M月d日 (E)", Locale.JAPANESE)
                        else DateTimeFormatter.ofPattern("EEE, d MMM", Locale.forLanguageTag(lang)),
                    ),
                    openTasksCount = openTasks.size,
                )
            }

            if (user.role == UserRole.FOREMAN) {
                val lockedCall = async { boardSource.getLockedMachines() }
                val followUpsCall = async { boardSource.getOpenFollowUps() }
                val tallyCall = async { boardSource.getTodayTally() }
                val locked = lockedCall.await().orNull().orEmpty()
                val followUps = followUpsCall.await().orNull().orEmpty()
                val tally = tallyCall.await().orNull()
                updateState {
                    it.copy(
                        isLoading = false,
                        followUpCount = followUps.size,
                        followUpDetailUser = followUps.firstOrNull()?.workerName.orEmpty(),
                        followUpDetailTerm = followUps.firstOrNull()?.termJa.orEmpty(),
                        tallyGreen = tally?.green ?: 0,
                        tallyOrange = tally?.orange ?: 0,
                        tallyRed = tally?.red ?: 0,
                        lockedMachinesCount = locked.size,
                        attentionCards = followUps.take(ATTENTION_MAX).map { f ->
                            AttentionCardItem(f.id, f.workerName, f.termJa, WakattaStatus.RED)
                        },
                        lockedMachines = locked.map { m ->
                            LockedMachineItem(m.machineId, pick(m.name, lang), m.workerNames.joinToString(", "))
                        },
                    )
                }
            } else {
                val briefingCall = async { boardSource.getBriefing() }
                val checksCall = async { boardSource.getPendingChecks() }
                val briefing = briefingCall.await().orNull()
                val checks = checksCall.await().orNull().orEmpty()
                updateState {
                    it.copy(
                        isLoading = false,
                        todayBriefing = briefing?.let { b -> BriefingItem(b.id, pick(b.text, lang)) },
                        pendingChecks = checks.map { c ->
                            PendingCheckItem(c.id, c.threadTitle, shortTime(c.sentAt), pick(c.text, lang), c.threadId)
                        },
                        myTasks = openTasks.filter { t -> user.id in t.assignees || t.assignees.isEmpty() }.map { t ->
                            HomeTaskItem(t.id, pick(t.title, lang), t.dueAt.orEmpty())
                        },
                        myMachines = machinesResult.orNull().orEmpty().map { m ->
                            HomeMachineItem(m.id, pick(m.name, lang), m.isReady)
                        },
                    )
                }
            }
        }
    }

    private fun <T> AppResult<T>.orNull(): T? = (this as? AppResult.Success)?.value

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Retry -> loadData()
            HomeEvent.Refresh -> loadData()
            HomeEvent.BellClick -> sendEffect(HomeEffect.Navigate(NotificationsKey))
            HomeEvent.FollowUpClick -> sendEffect(HomeEffect.Navigate(FollowUpsKey))
            HomeEvent.BriefingClick -> {
                if (state.value.isForeman) {
                    sendEffect(HomeEffect.Navigate(BriefingNewKey))
                } else {
                    sendEffect(HomeEffect.Navigate(BriefingCardKey(state.value.todayBriefing?.id)))
                }
            }
            HomeEvent.NearMissClick -> sendEffect(HomeEffect.Navigate(NearMissListKey))
            HomeEvent.AnalyticsClick -> sendEffect(HomeEffect.Navigate(AnalyticsKey))
            HomeEvent.SkillsClick -> sendEffect(HomeEffect.Navigate(SkillsKey))
            HomeEvent.LifeClick -> sendEffect(HomeEffect.Navigate(LifeKey))
            HomeEvent.SupportLogClick -> sendEffect(HomeEffect.Navigate(SupportLogKey()))
            HomeEvent.BoardTileClick -> sendEffect(HomeEffect.Navigate(BoardKey))
            HomeEvent.MachinesTileClick -> sendEffect(HomeEffect.Navigate(MachinesKey))
            HomeEvent.AttentionItemClick -> sendEffect(HomeEffect.Navigate(FollowUpsKey))
            is HomeEvent.LockedMachineClick -> sendEffect(HomeEffect.Navigate(MachineKey(event.machineId)))
            is HomeEvent.PendingCheckClick -> sendEffect(HomeEffect.Navigate(ChatKey(event.threadId)))
            is HomeEvent.TaskClick -> sendEffect(HomeEffect.Navigate(TaskKey(event.taskId)))
            is HomeEvent.MachineClick -> {
                if (event.isReady) {
                    sendEffect(HomeEffect.Navigate(MachineKey(event.machineId)))
                } else {
                    sendEffect(HomeEffect.Navigate(NoteKey(event.machineId)))
                }
            }
            HomeEvent.ScanMachineClick -> sendEffect(HomeEffect.Navigate(ScanKey))
            HomeEvent.ReportNearMissClick -> sendEffect(HomeEffect.Navigate(NearMissNewKey()))
            HomeEvent.AskClick -> sendEffect(HomeEffect.Navigate(AskKey))
            HomeEvent.SafetyCourseClick -> sendEffect(HomeEffect.Navigate(CourseKey))
            HomeEvent.MyLearningClick -> sendEffect(HomeEffect.Navigate(LearningKey()))
        }
    }
}

private const val ATTENTION_MAX = 5

/** Time only for today, else month/day and time. */
internal fun shortTime(iso: String, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): String =
    runCatching {
        val local = OffsetDateTime.parse(iso).atZoneSameInstant(zone)
        local.format(DateTimeFormatter.ofPattern(if (local.toLocalDate() == today) "HH:mm" else "M/d HH:mm"))
    }.getOrDefault("")

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigate: (Any) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RefreshOnReturn { viewModel.onEvent(HomeEvent.Refresh) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.Navigate -> onNavigate(effect.key)
            }
        }
    }

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.home_screen_title),
                actions = {
                    IconButton(
                        onClick = { onEvent(HomeEvent.BellClick) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.home_unread_badge,
                                                state.unreadNotificationsCount,
                                            ),
                                            fontSize = 10.sp,
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.home_ic_bell),
                                contentDescription = stringResource(R.string.home_bell_content_desc),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingState(message = stringResource(R.string.home_loading))
                }
            }
            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorState(
                        message = stringResource(R.string.home_error),
                        retryLabel = stringResource(R.string.home_retry),
                        onRetry = { onEvent(HomeEvent.Retry) },
                    )
                }
            }
            state.isEmpty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = stringResource(R.string.home_screen_title),
                        message = stringResource(R.string.home_empty),
                    )
                }
            }
            state.isForeman -> {
                ForemanHomeLayout(
                    state = state,
                    onEvent = onEvent,
                    innerPadding = innerPadding,
                )
            }
            else -> {
                WorkerHomeLayout(
                    state = state,
                    onEvent = onEvent,
                    innerPadding = innerPadding,
                )
            }
        }
    }
}

@Composable
private fun ForemanHomeLayout(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
    ) {
        // Greeting & Date
        item(key = "header", contentType = "header") {
            Column {
                Text(
                    text = stringResource(R.string.home_greeting_foreman, state.userName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.dateText.isNotEmpty()) {
                    Text(
                        text = state.dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Follow-up card
        item(key = "followup", contentType = "card") {
            WakattaCard(
                onClick = { onEvent(HomeEvent.FollowUpClick) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_help),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_followup_card_title, state.followUpCount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (state.followUpCount > 0) {
                                stringResource(
                                    R.string.home_followup_detail,
                                    state.followUpDetailUser,
                                    state.followUpDetailTerm,
                                )
                            } else {
                                stringResource(R.string.home_followup_all_good)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.home_ic_arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Understanding summary
        item(key = "understanding", contentType = "card") {
            Column {
                Text(
                    text = stringResource(R.string.home_today_understanding),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            TallyCountItem(
                                count = state.tallyGreen,
                                label = stringResource(R.string.home_status_green),
                                color = StatusColors.Green,
                            )
                            TallyCountItem(
                                count = state.tallyOrange,
                                label = stringResource(R.string.home_status_orange),
                                color = StatusColors.Orange,
                            )
                            TallyCountItem(
                                count = state.tallyRed,
                                label = stringResource(R.string.home_status_red),
                                color = StatusColors.Red,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Segmented bar
                        val total = state.tallyGreen + state.tallyOrange + state.tallyRed
                        if (total > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                            ) {
                                if (state.tallyGreen > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.tallyGreen.toFloat())
                                            .fillMaxSize()
                                            .background(StatusColors.Green),
                                    )
                                }
                                if (state.tallyOrange > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.tallyOrange.toFloat())
                                            .fillMaxSize()
                                            .background(StatusColors.Orange),
                                    )
                                }
                                if (state.tallyRed > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(state.tallyRed.toFloat())
                                            .fillMaxSize()
                                            .background(StatusColors.Red),
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.home_bar_cap_empty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Quick actions grid
        item(key = "quick_actions", contentType = "actions") {
            Column {
                Text(
                    text = stringResource(R.string.home_quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                ForemanQuickActionsGrid(onEvent = onEvent)
            }
        }

        // Attention rows
        item(key = "attention_title", contentType = "header") {
            Text(
                text = stringResource(R.string.home_attention_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.attentionCards.isEmpty()) {
            item(key = "attention_empty", contentType = "card") {
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_check),
                            contentDescription = null,
                            tint = StatusColors.Green,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_attention_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(
                items = state.attentionCards,
                key = { it.id },
                contentType = { "attention_item" },
            ) { card ->
                WakattaCard(
                    onClick = { onEvent(HomeEvent.AttentionItemClick) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WakattaListItem(
                        headlineContent = {
                            Text(text = card.name, fontWeight = FontWeight.Bold)
                        },
                        supportingContent = {
                            Text(text = card.message, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        },
                        leadingContent = {
                            Avatar(name = card.name)
                        },
                        trailingContent = {
                            StatusChip(status = card.status)
                        },
                    )
                }
            }
        }

        // Tiles
        item(key = "tiles", contentType = "tiles") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TileButton(
                    count = state.openTasksCount,
                    label = stringResource(R.string.home_tile_open_tasks),
                    onClick = { onEvent(HomeEvent.BoardTileClick) },
                    modifier = Modifier.weight(1f),
                )
                TileButton(
                    count = state.lockedMachinesCount,
                    label = stringResource(R.string.home_tile_locked_machines),
                    onClick = { onEvent(HomeEvent.MachinesTileClick) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Locked machines waiting for sign-off
        if (state.lockedMachines.isNotEmpty()) {
            item(key = "locked_header", contentType = "header") {
                Text(
                    text = stringResource(R.string.home_locked_machines_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(
                items = state.lockedMachines,
                key = { it.id },
                contentType = { "locked_machine" },
            ) { machine ->
                WakattaCard(
                    onClick = { onEvent(HomeEvent.LockedMachineClick(machine.id)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WakattaListItem(
                        headlineContent = { Text(text = machine.name, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(text = machine.lockedWorkersText) },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.home_ic_lock),
                                contentDescription = null,
                                tint = StatusColors.Red,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerHomeLayout(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
    ) {
        // Greeting & Date
        item(key = "header", contentType = "header") {
            Column {
                Text(
                    text = stringResource(R.string.home_greeting_worker, state.userName),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.dateText.isNotEmpty()) {
                    Text(
                        text = state.dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Today's Briefing card (if available)
        state.todayBriefing?.let { briefing ->
            item(key = "briefing", contentType = "card") {
                WakattaCard(
                    onClick = { onEvent(HomeEvent.BriefingClick) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.home_ic_camp),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.home_briefing_today),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = briefing.focusText,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        FilledTonalButton(
                            onClick = { onEvent(HomeEvent.BriefingClick) },
                            modifier = Modifier.heightIn(min = 48.dp),
                        ) {
                            Text(text = stringResource(R.string.home_briefing_open))
                        }
                    }
                }
            }
        }

        // Checks waiting for you
        item(key = "checks_title", contentType = "header") {
            Text(
                text = stringResource(R.string.home_checks_waiting),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.pendingChecks.isEmpty()) {
            item(key = "checks_empty", contentType = "card") {
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_check),
                            contentDescription = null,
                            tint = StatusColors.Green,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_checks_all_caught_up),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(
                items = state.pendingChecks,
                key = { it.id },
                contentType = { "pending_check" },
            ) { check ->
                WakattaCard(
                    onClick = { onEvent(HomeEvent.PendingCheckClick(check.threadId)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.home_checks_from, check.fromName, check.time),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = check.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusChip(status = WakattaStatus.WAIT)
                            FilledTonalButton(
                                onClick = { onEvent(HomeEvent.PendingCheckClick(check.threadId)) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(text = stringResource(R.string.home_checks_answer_now))
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        item(key = "worker_qa", contentType = "actions") {
            Column {
                Text(
                    text = stringResource(R.string.home_quick_actions),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                WorkerQuickActionsGrid(onEvent = onEvent)
            }
        }

        // My Learning card
        item(key = "my_learning", contentType = "card") {
            WakattaCard(
                onClick = { onEvent(HomeEvent.MyLearningClick) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_book),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_my_learning_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.home_my_learning_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.home_ic_arrow_forward),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // My tasks
        item(key = "tasks_header", contentType = "header") {
            Text(
                text = stringResource(R.string.home_my_tasks),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.myTasks.isEmpty()) {
            item(key = "tasks_empty", contentType = "card") {
                WakattaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_check),
                            contentDescription = null,
                            tint = StatusColors.Green,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.home_no_tasks),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(
                items = state.myTasks,
                key = { it.id },
                contentType = { "task" },
            ) { task ->
                WakattaCard(
                    onClick = { onEvent(HomeEvent.TaskClick(task.id)) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    WakattaListItem(
                        headlineContent = { Text(text = task.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(text = task.dueText) },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.home_ic_arrow_forward),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                }
            }
        }

        // My machines
        item(key = "machines_header", contentType = "header") {
            Text(
                text = stringResource(R.string.home_my_machines),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        items(
            items = state.myMachines,
            key = { it.id },
            contentType = { "machine" },
        ) { machine ->
            WakattaCard(
                onClick = { onEvent(HomeEvent.MachineClick(machine.id, machine.isReady)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                WakattaListItem(
                    headlineContent = { Text(text = machine.name, fontWeight = FontWeight.Bold) },
                    trailingContent = {
                        StatusChip(
                            status = if (machine.isReady) WakattaStatus.GREEN else WakattaStatus.RED,
                            label = if (machine.isReady) "READY" else "LOCKED",
                        )
                    },
                )
            }
        }

        // Big Scan machine CTA
        item(key = "scan_cta", contentType = "button") {
            Button(
                onClick = { onEvent(HomeEvent.ScanMachineClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.home_ic_qr),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_scan_machine),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun TallyCountItem(
    count: Int,
    label: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ForemanQuickActionsGrid(onEvent: (HomeEvent) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3,
    ) {
        QuickActionButton(
            label = stringResource(R.string.home_qa_briefing),
            iconRes = R.drawable.home_ic_camp,
            onClick = { onEvent(HomeEvent.BriefingClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_nearmiss),
            iconRes = R.drawable.home_ic_warn,
            onClick = { onEvent(HomeEvent.NearMissClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_analytics),
            iconRes = R.drawable.home_ic_chart,
            onClick = { onEvent(HomeEvent.AnalyticsClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_skills),
            iconRes = R.drawable.home_ic_grid,
            onClick = { onEvent(HomeEvent.SkillsClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_life),
            iconRes = R.drawable.home_ic_groups,
            onClick = { onEvent(HomeEvent.LifeClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_supportlog),
            iconRes = R.drawable.home_ic_doc,
            onClick = { onEvent(HomeEvent.SupportLogClick) },
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkerQuickActionsGrid(onEvent: (HomeEvent) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
    ) {
        QuickActionButton(
            label = stringResource(R.string.home_qa_report_nearmiss),
            iconRes = R.drawable.home_ic_warn,
            onClick = { onEvent(HomeEvent.ReportNearMissClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_life),
            iconRes = R.drawable.home_ic_groups,
            onClick = { onEvent(HomeEvent.LifeClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_ask),
            iconRes = R.drawable.home_ic_help,
            onClick = { onEvent(HomeEvent.AskClick) },
            modifier = Modifier.weight(1f),
        )
        QuickActionButton(
            label = stringResource(R.string.home_qa_course),
            iconRes = R.drawable.home_ic_book,
            onClick = { onEvent(HomeEvent.SafetyCourseClick) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TileButton(
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WakattaCard(
        onClick = onClick,
        modifier = modifier.heightIn(min = 68.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
