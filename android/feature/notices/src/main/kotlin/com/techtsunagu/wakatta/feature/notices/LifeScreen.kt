package com.techtsunagu.wakatta.feature.notices

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.notices.data.NoticeRepository
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.LifeSummary
import com.techtsunagu.wakatta.feature.notices.model.NoticeForPoster
import com.techtsunagu.wakatta.feature.notices.model.NoticeForWorker
import com.techtsunagu.wakatta.feature.notices.model.TodayWordInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class LifeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isForeman: Boolean = false,
    val isPosterAllowed: Boolean = false,
    val userLanguage: String = "ja",
    val upcomingEvents: List<EventDto> = emptyList(),
    val notices: List<NoticeForWorker> = emptyList(),
    val posterNotices: List<NoticeForPoster> = emptyList(),
    val pendingNoticeCount: Int = 0,
    val todayWord: TodayWordInfo? = null,
    val summary: LifeSummary = LifeSummary(),
    val error: String? = null,
)

sealed interface LifeEvent {
    data object Init : LifeEvent
    data object Refresh : LifeEvent
    data object Retry : LifeEvent
    data class ToggleReminder(val eventId: String) : LifeEvent
}

sealed interface LifeEffect

@HiltViewModel
class LifeViewModel @Inject constructor(
    private val noticeRepository: NoticeRepository,
    private val sessionStore: SessionStore,
) : MviViewModel<LifeUiState, LifeEvent, LifeEffect>(LifeUiState(isLoading = true)) {

    init {
        loadData()
    }

    override fun onEvent(event: LifeEvent) {
        when (event) {
            is LifeEvent.Init -> loadData()
            is LifeEvent.Refresh -> refreshData()
            is LifeEvent.Retry -> loadData()
            is LifeEvent.ToggleReminder -> toggleReminder(event.eventId)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            fetchDataInternal()
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            updateState { it.copy(isRefreshing = true) }
            fetchDataInternal()
            updateState { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun fetchDataInternal() {
        val session = sessionStore.session.firstOrNull()
        val role = session?.user?.role ?: Role.WORKER
        val isForeman = role == Role.FOREMAN
        val isPoster = role == Role.POSTER
        val userLang = session?.user?.language ?: if (isForeman) "ja" else "en"

        val eventsResult = noticeRepository.getUpcomingEvents()
        val events = (eventsResult as? AppResult.Success)?.value.orEmpty()

        if (isForeman) {
            val posterNoticesResult = noticeRepository.getNoticesForPoster()
            val posterNotices = (posterNoticesResult as? AppResult.Success)?.value.orEmpty()
            val todayWordResult = noticeRepository.getTodayWord()
            val todayWord = (todayWordResult as? AppResult.Success)?.value
            val summaryResult = noticeRepository.getLifeSummary()
            val summary = (summaryResult as? AppResult.Success)?.value ?: LifeSummary()

            updateState {
                it.copy(
                    isLoading = false,
                    isForeman = true,
                    isPosterAllowed = isPoster,
                    userLanguage = userLang,
                    upcomingEvents = events,
                    posterNotices = posterNotices,
                    todayWord = todayWord,
                    summary = summary,
                    error = null,
                )
            }
        } else {
            val noticesResult = noticeRepository.getNoticesForWorker()
            val notices = (noticesResult as? AppResult.Success)?.value.orEmpty()
            val pendingCount = notices.count { it.wakattaStatus == WakattaStatus.WAIT }
            val summaryResult = noticeRepository.getLifeSummary()
            val summary = (summaryResult as? AppResult.Success)?.value ?: LifeSummary()

            updateState {
                it.copy(
                    isLoading = false,
                    isForeman = false,
                    isPosterAllowed = isPoster,
                    userLanguage = userLang,
                    upcomingEvents = events,
                    notices = notices,
                    pendingNoticeCount = pendingCount,
                    summary = summary,
                    error = null,
                )
            }
        }
    }

    private fun toggleReminder(eventId: String) {
        viewModelScope.launch {
            noticeRepository.toggleReminder(eventId)
            val updatedEvents = noticeRepository.getUpcomingEvents()
            val events = (updatedEvents as? AppResult.Success)?.value.orEmpty()
            updateState { it.copy(upcomingEvents = events) }
        }
    }
}

@Composable
fun LifeScreen(
    onNoticeClick: (String) -> Unit = {},
    onNewNoticeClick: () -> Unit = {},
    onAskClick: () -> Unit = {},
    onSenpaiClick: () -> Unit = {},
    onSupportLogClick: () -> Unit = {},
    viewModel: LifeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNoticeClick = onNoticeClick,
        onNewNoticeClick = onNewNoticeClick,
        onAskClick = onAskClick,
        onSenpaiClick = onSenpaiClick,
        onSupportLogClick = onSupportLogClick,
    )
}

@Composable
fun LifeContent(
    state: LifeUiState,
    onEvent: (LifeEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNoticeClick: (String) -> Unit = {},
    onNewNoticeClick: () -> Unit = {},
    onAskClick: () -> Unit = {},
    onSenpaiClick: () -> Unit = {},
    onSupportLogClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(title = stringResource(R.string.notices_life_screen_title))
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
                    onRetry = { onEvent(LifeEvent.Retry) },
                    retryLabel = stringResource(R.string.notices_retry),
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
                    if (state.isForeman) {
                        foremanContent(
                            state = state,
                            onEvent = onEvent,
                            onNoticeClick = onNoticeClick,
                            onNewNoticeClick = onNewNoticeClick,
                            onAskClick = onAskClick,
                            onSenpaiClick = onSenpaiClick,
                            onSupportLogClick = onSupportLogClick,
                        )
                    } else {
                        workerContent(
                            state = state,
                            onEvent = onEvent,
                            onNoticeClick = onNoticeClick,
                            onAskClick = onAskClick,
                            onSenpaiClick = onSenpaiClick,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.workerContent(
    state: LifeUiState,
    onEvent: (LifeEvent) -> Unit,
    onNoticeClick: (String) -> Unit,
    onAskClick: () -> Unit,
    onSenpaiClick: () -> Unit,
) {
    item(key = "header_events", contentType = "header") {
        SectionHeader(title = stringResource(R.string.notices_upcoming_events))
    }

    if (state.upcomingEvents.isEmpty()) {
        item(key = "empty_events", contentType = "empty") {
            EmptyCard(message = stringResource(R.string.notices_no_events))
        }
    } else {
        items(
            items = state.upcomingEvents,
            key = { it.id },
            contentType = { "event_card" },
        ) { event ->
            EventRowItem(
                event = event,
                userLang = state.userLanguage,
                onToggleReminder = { onEvent(LifeEvent.ToggleReminder(event.id)) },
                onClick = { onNoticeClick(event.noticeId) },
            )
        }
    }

    item(key = "header_notices", contentType = "header") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(title = stringResource(R.string.notices_list_title))
            if (state.pendingNoticeCount > 0) {
                StatusChip(
                    status = WakattaStatus.WAIT,
                    label = stringResource(R.string.notices_pending_badge, state.pendingNoticeCount),
                )
            }
        }
    }

    if (state.notices.isEmpty()) {
        item(key = "empty_notices", contentType = "empty") {
            EmptyState(
                title = stringResource(R.string.notices_list_title),
                message = stringResource(R.string.notices_empty),
            )
        }
    } else {
        items(
            items = state.notices,
            key = { it.id },
            contentType = { "notice_item" },
        ) { notice ->
            WorkerNoticeCard(
                notice = notice,
                userLang = state.userLanguage,
                onClick = { onNoticeClick(notice.id) },
            )
        }
    }

    item(key = "ask_card", contentType = "action_card") {
        AskAboutLifeCard(onClick = onAskClick)
    }

    item(key = "senpai_card", contentType = "senpai_card") {
        MySenpaiCard(
            summary = state.summary,
            onClick = onSenpaiClick,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.foremanContent(
    state: LifeUiState,
    onEvent: (LifeEvent) -> Unit,
    onNoticeClick: (String) -> Unit,
    onNewNoticeClick: () -> Unit,
    onAskClick: () -> Unit,
    onSenpaiClick: () -> Unit,
    onSupportLogClick: () -> Unit,
) {
    if (state.isPosterAllowed) {
        item(key = "new_notice_btn", contentType = "button") {
            Button(
                onClick = onNewNoticeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.notices_create_notice))
            }
        }
    }

    state.todayWord?.let { word ->
        item(key = "today_word", contentType = "word_card") {
            TodayWordCard(word = word)
        }
    }

    item(key = "header_published", contentType = "header") {
        SectionHeader(title = stringResource(R.string.notices_published_title))
    }

    if (state.posterNotices.isEmpty()) {
        item(key = "empty_poster_notices", contentType = "empty") {
            EmptyState(
                title = stringResource(R.string.notices_published_title),
                message = stringResource(R.string.notices_empty),
            )
        }
    } else {
        items(
            items = state.posterNotices,
            key = { it.id },
            contentType = { "poster_notice_item" },
        ) { notice ->
            PosterNoticeCard(
                notice = notice,
                onClick = { onNoticeClick(notice.id) },
            )
        }
    }

    item(key = "header_senpai_support", contentType = "header") {
        SectionHeader(title = stringResource(R.string.notices_senpai_support_title))
    }

    item(key = "support_summary_card", contentType = "support_card") {
        ForemanSupportCard(
            summary = state.summary,
            onSenpaiClick = onSenpaiClick,
            onAskClick = onAskClick,
            onSupportLogClick = onSupportLogClick,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun EmptyCard(message: String) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventRowItem(
    event: EventDto,
    userLang: String,
    onToggleReminder: () -> Unit,
    onClick: () -> Unit,
) {
    WakattaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DateBadge(date = event.date)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    TriText(
                        tri = event.title,
                        lang = userLang,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${event.date} · ${event.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val reminderDesc = if (event.isReminderOn) {
                stringResource(R.string.notices_reminder_on)
            } else {
                stringResource(R.string.notices_add_reminder)
            }
            IconButton(
                onClick = onToggleReminder,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = reminderDesc
                    },
            ) {
                Icon(
                    imageVector = if (event.isReminderOn) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = if (event.isReminderOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DateBadge(date: String) {
    val parts = date.split("-")
    val day = parts.getOrNull(2) ?: "1"
    val month = parts.getOrNull(1) ?: "1"
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(48.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${month}月",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkerNoticeCard(
    notice: NoticeForWorker,
    userLang: String,
    onClick: () -> Unit,
) {
    WakattaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIconBox(category = notice.category)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TriText(
                        tri = notice.title,
                        lang = userLang,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${notice.from} · ${notice.publishedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusChip(status = notice.wakattaStatus)
        }
    }
}

@Composable
private fun PosterNoticeCard(
    notice: NoticeForPoster,
    onClick: () -> Unit,
) {
    val statuses = notice.workerStatuses
    val greenCount = statuses.count { it.status == WakattaStatus.GREEN }
    val orangeCount = statuses.count { it.status == WakattaStatus.ORANGE }
    val redCount = statuses.count { it.status == WakattaStatus.RED }
    val waitCount = statuses.count { it.status == WakattaStatus.WAIT }

    WakattaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIconBox(category = notice.category)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    TriText(
                        tri = notice.title,
                        lang = "ja",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${notice.from} · ${notice.publishedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CountChip(label = "OK $greenCount", status = WakattaStatus.GREEN)
                CountChip(label = "確認 $orangeCount", status = WakattaStatus.ORANGE)
                CountChip(label = "要説明 $redCount", status = WakattaStatus.RED)
                CountChip(label = "未回答 $waitCount", status = WakattaStatus.WAIT)
            }
        }
    }
}

@Composable
private fun CountChip(label: String, status: WakattaStatus) {
    val statusColor = StatusColors.forStatus(status)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = statusColor.containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = statusColor.onContainerColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun CategoryIconBox(category: NoticeCategory) {
    val icon: ImageVector = when (category) {
        NoticeCategory.GARBAGE -> Icons.Default.Delete
        NoticeCategory.DISASTER -> Icons.Default.Warning
        NoticeCategory.EVENT -> Icons.Default.DateRange
        NoticeCategory.DUTY -> Icons.Default.People
        NoticeCategory.OTHER, NoticeCategory.UNKNOWN -> Icons.Default.Info
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun AskAboutLifeCard(onClick: () -> Unit) {
    WakattaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Help,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notices_ask_card_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.notices_ask_card_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MySenpaiCard(
    summary: LifeSummary,
    onClick: () -> Unit,
) {
    WakattaCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                name = summary.senpaiName,
                containerColor = Color(summary.senpaiColor),
                size = 40.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notices_senpai_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                val nextText = summary.nextMeetDate?.let { date ->
                    stringResource(R.string.notices_next_meet, summary.senpaiName, date)
                } ?: summary.senpaiName
                Text(
                    text = nextText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TodayWordCard(word: TodayWordInfo) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.notices_today_word_label,
                    word.workerName,
                    word.languageName,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = word.term,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            val jaMeaning = pick(word.meaning, "ja")
            val meaningText = if (word.reading.isNotEmpty()) {
                "${word.reading} = $jaMeaning"
            } else {
                "= $jaMeaning"
            }
            Text(
                text = meaningText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.notices_today_word_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ForemanSupportCard(
    summary: LifeSummary,
    onSenpaiClick: () -> Unit,
    onAskClick: () -> Unit,
    onSupportLogClick: () -> Unit,
) {
    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SupportListItem(
                title = stringResource(R.string.notices_senpai_pairs),
                subtitle = stringResource(
                    R.string.notices_senpai_pairs_desc,
                    summary.senpaiPairsCount,
                    summary.meetupsCount,
                ),
                onClick = onSenpaiClick,
            )
            SupportListItem(
                title = stringResource(R.string.notices_forwarded_asks),
                subtitle = stringResource(
                    R.string.notices_forwarded_asks_desc,
                    summary.forwardedAsksCount,
                ),
                onClick = onAskClick,
            )
            SupportListItem(
                title = stringResource(R.string.notices_support_log),
                subtitle = stringResource(R.string.notices_support_log_desc),
                onClick = onSupportLogClick,
            )
        }
    }
}

@Composable
private fun SupportListItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
