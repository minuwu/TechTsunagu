package com.techtsunagu.wakatta.feature.notifications

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaListItem
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

enum class PermissionPrompt { Hidden, Ask, Denied }

data class NotificationRow(val id: String, val text: String, val isUnread: Boolean)

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val items: List<NotificationRow> = emptyList(),
    val permission: PermissionPrompt = PermissionPrompt.Hidden,
)

sealed interface NotificationsEvent {
    data object Retry : NotificationsEvent
    data class SystemStatus(val enabled: Boolean) : NotificationsEvent
    data object TurnOn : NotificationsEvent
    data object NotNow : NotificationsEvent
    data class PermissionResult(val granted: Boolean) : NotificationsEvent
    data object OpenSettings : NotificationsEvent
    data class MarkAsRead(val id: String) : NotificationsEvent
}

sealed interface NotificationsEffect {
    data object RequestPermission : NotificationsEffect
    data object OpenSystemSettings : NotificationsEffect
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val registrar: DeviceRegistrar,
    private val languages: LanguageManager,
) : MviViewModel<NotificationsUiState, NotificationsEvent, NotificationsEffect>(NotificationsUiState()) {

    private var dismissed = false

    init {
        load()
    }

    override fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Retry -> load()
            is NotificationsEvent.SystemStatus -> setPrompt(if (event.enabled) PermissionPrompt.Hidden else pendingPrompt())
            NotificationsEvent.TurnOn -> sendEffect(NotificationsEffect.RequestPermission)
            NotificationsEvent.NotNow -> {
                dismissed = true
                setPrompt(PermissionPrompt.Denied)
            }
            is NotificationsEvent.PermissionResult -> onPermissionResult(event.granted)
            NotificationsEvent.OpenSettings -> sendEffect(NotificationsEffect.OpenSystemSettings)
            is NotificationsEvent.MarkAsRead -> onMarkAsRead(event.id)
        }
    }

    private fun pendingPrompt() = if (dismissed) PermissionPrompt.Denied else PermissionPrompt.Ask

    private fun setPrompt(prompt: PermissionPrompt) = updateState { it.copy(permission = prompt) }

    private fun onPermissionResult(granted: Boolean) {
        dismissed = !granted
        setPrompt(if (granted) PermissionPrompt.Hidden else PermissionPrompt.Denied)
        viewModelScope.launch { registrar.register(notificationsEnabled = granted) }
    }

    private fun onMarkAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
            updateState { current ->
                current.copy(
                    items = current.items.map { item ->
                        if (item.id == id) item.copy(isUnread = false) else item
                    },
                )
            }
        }
    }

    private fun load() {
        updateState { it.copy(isLoading = true, hasError = false) }
        viewModelScope.launch {
            when (val result = repository.load()) {
                is AppResult.Success -> {
                    val language = languages.currentLanguage.value
                    val rows = result.value.mapNotNull { item ->
                        NotificationTextBuilder.buildNotificationText(item.kind, item.senderName, language)
                            ?.let { NotificationRow(item.id, it, item.isUnread) }
                    }
                    updateState { it.copy(isLoading = false, items = rows) }
                }
                is AppResult.Failure -> updateState { it.copy(isLoading = false, hasError = true) }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit = {},
    viewModel: NotificationsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) NotificationChannels.createAll(context)
        viewModel.onEvent(NotificationsEvent.PermissionResult(granted))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                NotificationsEffect.RequestPermission ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onEvent(NotificationsEvent.OpenSettings)
                    }
                NotificationsEffect.OpenSystemSettings -> context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                )
            }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(NotificationsEvent.SystemStatus(NotificationManagerCompat.from(context).areNotificationsEnabled()))
    }
    NotificationsContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick)
}

@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    onEvent: (NotificationsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.notifications_screen_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingState(
                modifier = Modifier.padding(innerPadding),
                message = stringResource(R.string.notifications_loading),
            )
            state.hasError -> EmptyState(
                modifier = Modifier.padding(innerPadding),
                message = stringResource(R.string.notifications_error),
                actionLabel = stringResource(R.string.notifications_retry),
                onActionClick = { onEvent(NotificationsEvent.Retry) },
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.permission != PermissionPrompt.Hidden) {
                    item(key = "permission", contentType = "permission") {
                        PermissionCard(state.permission, onEvent)
                    }
                }
                if (state.items.isEmpty()) {
                    item(key = "empty", contentType = "empty") {
                        EmptyState(message = stringResource(R.string.notifications_empty))
                    }
                }
                items(state.items, key = { it.id }, contentType = { "notification" }) { row ->
                    WakattaListItem(
                        modifier = Modifier.clickable { onEvent(NotificationsEvent.MarkAsRead(row.id)) },
                        headlineContent = {
                            Text(
                                text = row.text,
                                fontWeight = if (row.isUnread) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(prompt: PermissionPrompt, onEvent: (NotificationsEvent) -> Unit) {
    WakattaCard(modifier = Modifier.padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (prompt == PermissionPrompt.Ask) {
                Text(
                    text = stringResource(R.string.notifications_permission_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.notifications_permission_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onEvent(NotificationsEvent.TurnOn) }) {
                        Text(stringResource(R.string.notifications_turn_on))
                    }
                    OutlinedButton(onClick = { onEvent(NotificationsEvent.NotNow) }) {
                        Text(stringResource(R.string.notifications_not_now))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.notifications_off),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { onEvent(NotificationsEvent.OpenSettings) }) {
                    Text(stringResource(R.string.notifications_open_settings))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsContentPreview() {
    NotificationsContent(
        state = NotificationsUiState(
            isLoading = false,
            items = listOf(
                NotificationRow("n1", "New message from Tanaka", isUnread = true),
                NotificationRow("n2", "New message from Rahim", isUnread = false),
            ),
            permission = PermissionPrompt.Ask,
        ),
        onEvent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun NotificationsContentEmptyPreview() {
    NotificationsContent(
        state = NotificationsUiState(
            isLoading = false,
            items = emptyList(),
            permission = PermissionPrompt.Hidden,
        ),
        onEvent = {},
    )
}
