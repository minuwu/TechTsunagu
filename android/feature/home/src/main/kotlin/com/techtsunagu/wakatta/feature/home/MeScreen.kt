package com.techtsunagu.wakatta.feature.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.viewModelScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.LanguageManager
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SettingsStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.core.navigation.LearningKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.NoteKey
import com.techtsunagu.wakatta.core.navigation.OnboardingKey
import com.techtsunagu.wakatta.core.navigation.SkillsKey
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.home.data.HomeBoardSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class TeamMemberItem(
    val id: String,
    val name: String,
    val permittedCount: Int,
    val totalCount: Int = 3,
)

data class PassportMachineItem(
    val id: String,
    val name: String,
    val isSignedOff: Boolean,
    val signedOffDetail: String,
)

data class MeUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
    val isForeman: Boolean = false,
    val userId: String = "",
    val userName: String = "",
    val currentLanguage: String = "ja",
    val isDarkMode: Boolean = false,
    val isLargeText: Boolean = false,
    val isSimulateOffline: Boolean = false,
    val notifyMessages: Boolean = true,
    val teamMembers: List<TeamMemberItem> = emptyList(),
    val passportMachines: List<PassportMachineItem> = emptyList(),
    val showLanguageDialog: Boolean = false,
)

sealed interface MeEvent {
    data object Retry : MeEvent
    data class ToggleDarkMode(val enabled: Boolean) : MeEvent
    data class ToggleLargeText(val enabled: Boolean) : MeEvent
    data class ToggleSimulateOffline(val enabled: Boolean) : MeEvent
    data class ToggleNotifyMessages(val enabled: Boolean) : MeEvent
    data object OpenLanguagePicker : MeEvent
    data object DismissLanguagePicker : MeEvent
    data class SelectLanguage(val langCode: String) : MeEvent
    data object OpenSkillsMatrix : MeEvent
    data object OpenMyLearning : MeEvent
    data class PassportMachineClick(val machineId: String, val isSignedOff: Boolean) : MeEvent
    data object ReplayOnboarding : MeEvent
    data object SwitchRoleDemo : MeEvent
    data object Refresh : MeEvent
    data object SignOut : MeEvent
}

sealed interface MeEffect {
    data class Navigate(val key: Any) : MeEffect
    data class ApplyLocales(val languageTag: String) : MeEffect
}

@HiltViewModel
class MeViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingsStore: SettingsStore,
    private val signOffRepository: SignOffRepository,
    private val machineRepository: MachineRepository,
    private val boardSource: HomeBoardSource,
    private val languageManager: LanguageManager,
) : MviViewModel<MeUiState, MeEvent, MeEffect>(MeUiState(isLoading = true)) {

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            updateState { it.copy(isLoading = it.userName.isEmpty(), error = null) }
            try {
                val user = sessionStore.session.firstOrNull()?.user ?: return@launch
                val isForeman = user.role == Role.FOREMAN
                val currentLang = languageManager.currentLanguage.value

                val darkMode = settingsStore.isDarkMode.value ?: false
                val largeText = settingsStore.isLargeText.value
                val offlineHint = settingsStore.isOfflineHintEnabled.value
                val notifications = settingsStore.isNotificationEnabled.value

                val teamMembers = if (isForeman) {
                    (boardSource.getTeam() as? AppResult.Success)?.value.orEmpty()
                        .map { TeamMemberItem(it.id, it.name, it.permitted, it.total) }
                } else {
                    emptyList()
                }

                val passportItems = if (isForeman) {
                    emptyList()
                } else {
                    val passedAt = (signOffRepository.getPassport(user.id) as? AppResult.Success)?.value?.signOffs.orEmpty()
                        .associate { it.machineId to it.passedAt }
                    (machineRepository.getMachines() as? AppResult.Success)?.value.orEmpty().map { m ->
                        PassportMachineItem(
                            id = m.id,
                            name = pick(m.name, currentLang),
                            isSignedOff = m.isReady,
                            signedOffDetail = passedAt[m.id]?.take(10).orEmpty(),
                        )
                    }
                }

                updateState {
                    it.copy(
                        isLoading = false,
                        error = null,
                        isForeman = isForeman,
                        userId = user.id,
                        userName = user.displayName,
                        currentLanguage = currentLang,
                        isDarkMode = darkMode,
                        isLargeText = largeText,
                        isSimulateOffline = offlineHint,
                        notifyMessages = notifications,
                        teamMembers = teamMembers,
                        passportMachines = passportItems,
                    )
                }
            } catch (t: Throwable) {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: "Failed to load profile",
                    )
                }
            }
        }
    }

    override fun onEvent(event: MeEvent) {
        when (event) {
            MeEvent.Retry -> loadData()
            MeEvent.Refresh -> loadData()
            is MeEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    settingsStore.setDarkMode(event.enabled)
                    updateState { it.copy(isDarkMode = event.enabled) }
                }
            }
            is MeEvent.ToggleLargeText -> {
                viewModelScope.launch {
                    settingsStore.setLargeText(event.enabled)
                    updateState { it.copy(isLargeText = event.enabled) }
                }
            }
            is MeEvent.ToggleSimulateOffline -> {
                viewModelScope.launch {
                    settingsStore.setOfflineHint(event.enabled)
                    updateState { it.copy(isSimulateOffline = event.enabled) }
                }
            }
            is MeEvent.ToggleNotifyMessages -> {
                viewModelScope.launch {
                    settingsStore.setNotification(event.enabled)
                    updateState { it.copy(notifyMessages = event.enabled) }
                }
            }
            MeEvent.OpenLanguagePicker -> updateState { it.copy(showLanguageDialog = true) }
            MeEvent.DismissLanguagePicker -> updateState { it.copy(showLanguageDialog = false) }
            is MeEvent.SelectLanguage -> {
                viewModelScope.launch {
                    languageManager.setLanguage(event.langCode)
                    updateState { it.copy(currentLanguage = event.langCode, showLanguageDialog = false) }
                    sendEffect(MeEffect.ApplyLocales(event.langCode))
                }
            }
            MeEvent.OpenSkillsMatrix -> sendEffect(MeEffect.Navigate(SkillsKey))
            MeEvent.OpenMyLearning -> sendEffect(MeEffect.Navigate(LearningKey(state.value.userId)))
            is MeEvent.PassportMachineClick -> {
                if (event.isSignedOff) {
                    sendEffect(MeEffect.Navigate(MachineKey(event.machineId)))
                } else {
                    sendEffect(MeEffect.Navigate(NoteKey(event.machineId)))
                }
            }
            MeEvent.ReplayOnboarding -> sendEffect(MeEffect.Navigate(OnboardingKey))
            MeEvent.SwitchRoleDemo -> {
                viewModelScope.launch {
                    val currentIsForeman = state.value.isForeman
                    val targetUser = if (currentIsForeman) DemoSeed.demoWorker else DemoSeed.demoForeman
                    sessionStore.save(
                        Session(
                            token = "mock_token_${targetUser.id}",
                            user = targetUser,
                        )
                    )
                    loadData()
                }
            }
            MeEvent.SignOut -> {
                viewModelScope.launch {
                    // Best effort: it also stops the pushes to this phone. Signing out works offline too.
                    withTimeoutOrNull(SIGN_OUT_WAIT_MS) { boardSource.signOut() }
                    sessionStore.clear()
                    // The next person on this phone gets the language of their own account.
                    sendEffect(MeEffect.ApplyLocales(""))
                }
            }
        }
    }
}

private const val SIGN_OUT_WAIT_MS = 3_000L

@Composable
fun MeScreen(
    viewModel: MeViewModel = viewModel(),
    onNavigate: (Any) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RefreshOnReturn { viewModel.onEvent(MeEvent.Refresh) }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MeEffect.Navigate -> onNavigate(effect.key)
                is MeEffect.ApplyLocales -> {
                    try {
                        val localeList = LocaleListCompat.forLanguageTags(effect.languageTag)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    MeContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun MeContent(
    state: MeUiState,
    onEvent: (MeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.showLanguageDialog) {
        LanguagePickerDialog(
            currentLanguage = state.currentLanguage,
            onSelect = { onEvent(MeEvent.SelectLanguage(it)) },
            onDismiss = { onEvent(MeEvent.DismissLanguagePicker) },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(title = stringResource(R.string.home_me_screen_title))
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
                        onRetry = { onEvent(MeEvent.Retry) },
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 8.dp, bottom = 24.dp),
                ) {
                    // Profile Header
                    item(key = "profile_header", contentType = "profile") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(
                                name = state.userName,
                                size = 56.dp,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = state.userName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (state.isForeman) {
                                        stringResource(R.string.home_me_plant_foreman)
                                    } else {
                                        stringResource(R.string.home_me_plant_worker)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Foreman: Team Section
                    if (state.isForeman) {
                        item(key = "team_header", contentType = "header") {
                            Text(
                                text = stringResource(R.string.home_me_team),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "team_card", contentType = "card") {
                            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    state.teamMembers.forEachIndexed { index, member ->
                                        if (index > 0) HorizontalDivider()
                                        WakattaListItem(
                                            headlineContent = { Text(text = member.name, fontWeight = FontWeight.Bold) },
                                            supportingContent = {
                                                Text(
                                                    text = stringResource(
                                                        R.string.home_me_team_machines_permitted,
                                                        member.permittedCount,
                                                        member.totalCount,
                                                    ),
                                                )
                                            },
                                            leadingContent = { Avatar(name = member.name) },
                                        )
                                    }
                                    HorizontalDivider()
                                    WakattaListItem(
                                        headlineContent = { Text(text = stringResource(R.string.home_me_open_skills)) },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_grid),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_arrow_forward),
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = { onEvent(MeEvent.OpenSkillsMatrix) },
                                    )
                                }
                            }
                        }
                    } else {
                        // Worker: Skill Passport Section
                        item(key = "passport_header", contentType = "header") {
                            Text(
                                text = stringResource(R.string.home_me_skill_passport),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        item(key = "passport_card", contentType = "card") {
                            WakattaCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    state.passportMachines.forEachIndexed { index, machine ->
                                        if (index > 0) HorizontalDivider()
                                        WakattaListItem(
                                            headlineContent = { Text(text = machine.name, fontWeight = FontWeight.Bold) },
                                            supportingContent = {
                                                Text(
                                                    text = if (machine.isSignedOff) {
                                                        if (machine.signedOffDetail.isNotEmpty()) {
                                                            stringResource(
                                                                R.string.home_me_signed_off,
                                                                machine.signedOffDetail,
                                                            )
                                                        } else {
                                                            stringResource(R.string.home_me_valid_signoff)
                                                        }
                                                    } else {
                                                        stringResource(R.string.home_me_not_signed_off)
                                                    },
                                                )
                                            },
                                            leadingContent = {
                                                Icon(
                                                    painter = painterResource(if (machine.isSignedOff) R.drawable.home_ic_check else R.drawable.home_ic_lock),
                                                    contentDescription = null,
                                                    tint = if (machine.isSignedOff) StatusColors.Green else StatusColors.Red,
                                                )
                                            },
                                            trailingContent = {
                                                if (machine.isSignedOff) {
                                                    StatusChip(
                                                        status = WakattaStatus.GREEN,
                                                        label = stringResource(R.string.home_me_ready),
                                                    )
                                                } else {
                                                    StatusChip(
                                                        status = WakattaStatus.WAIT,
                                                        label = stringResource(R.string.home_me_take_check),
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onEvent(
                                                    MeEvent.PassportMachineClick(
                                                        machine.id,
                                                        machine.isSignedOff,
                                                    )
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Settings Section
                    item(key = "settings_header", contentType = "header") {
                        Text(
                            text = stringResource(R.string.home_me_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    item(key = "settings_card", contentType = "card") {
                        WakattaCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                // Language
                                WakattaListItem(
                                    headlineContent = { Text(text = stringResource(R.string.home_me_language)) },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.home_ic_globe),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                    trailingContent = {
                                        val langLabel = when (state.currentLanguage) {
                                            "ja" -> stringResource(R.string.home_lang_ja)
                                            "bn" -> stringResource(R.string.home_lang_bn)
                                            else -> stringResource(R.string.home_lang_en)
                                        }
                                        Text(
                                            text = langLabel,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    },
                                    onClick = { onEvent(MeEvent.OpenLanguagePicker) },
                                )
                                HorizontalDivider()
                                // Dark Mode
                                WakattaListItem(
                                    headlineContent = { Text(text = stringResource(R.string.home_me_dark_mode)) },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.home_ic_dark),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = state.isDarkMode,
                                            onCheckedChange = { onEvent(MeEvent.ToggleDarkMode(it)) },
                                        )
                                    },
                                )
                                HorizontalDivider()
                                // Large Text
                                WakattaListItem(
                                    headlineContent = { Text(text = stringResource(R.string.home_me_large_text)) },
                                    leadingContent = {
                                        Icon(
                                            painter = painterResource(R.drawable.home_ic_text),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = state.isLargeText,
                                            onCheckedChange = { onEvent(MeEvent.ToggleLargeText(it)) },
                                        )
                                    },
                                )
                                if (!ApiConfig.USE_LIVE_API) {
                                    HorizontalDivider()
                                    // Simulate Offline
                                    WakattaListItem(
                                        headlineContent = { Text(text = stringResource(R.string.home_me_simulate_offline)) },
                                        supportingContent = { Text(text = stringResource(R.string.home_me_simulate_offline_sub)) },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_wifioff),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        trailingContent = {
                                            Switch(
                                                checked = state.isSimulateOffline,
                                                onCheckedChange = { onEvent(MeEvent.ToggleSimulateOffline(it)) },
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }

                    // Notification Preferences Section
                    item(key = "notif_header", contentType = "header") {
                        Text(
                            text = stringResource(R.string.home_me_notifications),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    item(key = "notif_card", contentType = "card") {
                        WakattaCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                WakattaListItem(
                                    headlineContent = { Text(text = stringResource(R.string.home_me_notify_messages)) },
                                    trailingContent = {
                                        Switch(
                                            checked = state.notifyMessages,
                                            onCheckedChange = { onEvent(MeEvent.ToggleNotifyMessages(it)) },
                                        )
                                    },
                                )
                            }
                        }
                    }

                    // Actions Section
                    item(key = "actions_card", contentType = "card") {
                        WakattaCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                if (!state.isForeman) {
                                    WakattaListItem(
                                        headlineContent = { Text(text = stringResource(R.string.home_my_learning_title)) },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_book),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_arrow_forward),
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = { onEvent(MeEvent.OpenMyLearning) },
                                    )
                                    HorizontalDivider()
                                }
                                // shortcut: the onboarding steps are the prototype demo (team QR, team code), open them in live mode when they use real data.
                                if (!ApiConfig.USE_LIVE_API) {
                                    WakattaListItem(
                                        headlineContent = { Text(text = stringResource(R.string.home_me_replay_onboarding)) },
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_replay),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_arrow_forward),
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = { onEvent(MeEvent.ReplayOnboarding) },
                                    )
                                    HorizontalDivider()
                                    WakattaListItem(
                                        headlineContent = { Text(text = stringResource(R.string.home_me_switch_role)) },
                                        supportingContent = {
                                            Text(
                                                text = if (state.isForeman) {
                                                    "Foreman -> Worker (English)"
                                                } else {
                                                    "Worker -> Foreman (日本語)"
                                                },
                                            )
                                        },
                                        trailingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.home_ic_arrow_forward),
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = { onEvent(MeEvent.SwitchRoleDemo) },
                                    )
                                    HorizontalDivider()
                                }
                                WakattaListItem(
                                    headlineContent = {
                                        Text(
                                            text = stringResource(R.string.home_me_sign_out),
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = { onEvent(MeEvent.SignOut) },
                                )
                            }
                        }
                    }

                    // Demo Data Notice Banner
                    if (!ApiConfig.USE_LIVE_API) item(key = "demo_notice", contentType = "notice") {
                        Text(
                            text = stringResource(R.string.home_me_demo_data_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        "ja" to stringResource(R.string.home_lang_ja),
        "en" to stringResource(R.string.home_lang_en),
        "bn" to stringResource(R.string.home_lang_bn),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.home_me_select_language)) },
        text = {
            Column {
                options.forEach { (code, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (code == currentLanguage),
                            onClick = { onSelect(code) },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "OK")
            }
        },
    )
}
