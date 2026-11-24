package com.techtsunagu.wakatta.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.viewModelScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isForeman: Boolean = false,
    val step: Int = 0,
    val totalSteps: Int = if (isForeman) 2 else 3,
    val userName: String = "Rahim Uddin",
    // Worker step 0 (Join team)
    val isJoined: Boolean = false,
    val isScanning: Boolean = false,
    // Worker step 1 (Language)
    val selectedLanguage: String = "en",
    // Worker step 2 (Check question)
    val selectedAnswerIndex: Int = -1,
    val isAnswerCorrect: Boolean = false,
    val isAnswerIncorrect: Boolean = false,
    // Foreman step 0 (Team code)
    val teamCode: String = "PRESS-2F-4821",
    val codeCopied: Boolean = false,
    // Foreman step 1 (Mic)
    val micGranted: Boolean = false,
)

sealed interface OnboardingEvent {
    data object Next : OnboardingEvent
    data object Prev : OnboardingEvent
    data object Skip : OnboardingEvent
    data object ScanTeamQr : OnboardingEvent
    data class SelectLanguage(val langCode: String) : OnboardingEvent
    data class SelectCheckAnswer(val index: Int) : OnboardingEvent
    data object CopyTeamCode : OnboardingEvent
    data object GrantMicPermission : OnboardingEvent
}

sealed interface OnboardingEffect {
    data object FinishOnboarding : OnboardingEffect
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionStore: SessionStore,
    private val settingsStore: SettingsStore,
) : MviViewModel<OnboardingUiState, OnboardingEvent, OnboardingEffect>(OnboardingUiState()) {

    init {
        viewModelScope.launch {
            val session = sessionStore.session.firstOrNull()
            val isForeman = session?.user?.role == Role.FOREMAN
            val userName = session?.user?.displayName ?: if (isForeman) "田中 健一" else "Rahim Uddin"
            updateState {
                it.copy(
                    isForeman = isForeman,
                    totalSteps = if (isForeman) 2 else 3,
                    userName = userName,
                )
            }
        }
    }

    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.Skip -> finishOnboarding()
            OnboardingEvent.Prev -> {
                if (state.value.step > 0) {
                    updateState { it.copy(step = it.step - 1) }
                }
            }
            OnboardingEvent.Next -> {
                val current = state.value
                if (current.step >= current.totalSteps - 1) {
                    finishOnboarding()
                } else {
                    updateState { it.copy(step = it.step + 1) }
                }
            }
            OnboardingEvent.ScanTeamQr -> {
                viewModelScope.launch {
                    updateState { it.copy(isScanning = true) }
                    delay(500L)
                    updateState { it.copy(isScanning = false, isJoined = true) }
                }
            }
            is OnboardingEvent.SelectLanguage -> {
                updateState { it.copy(selectedLanguage = event.langCode) }
            }
            is OnboardingEvent.SelectCheckAnswer -> {
                val isCorrect = event.index == 0
                updateState {
                    it.copy(
                        selectedAnswerIndex = event.index,
                        isAnswerCorrect = isCorrect,
                        isAnswerIncorrect = !isCorrect,
                    )
                }
            }
            OnboardingEvent.CopyTeamCode -> {
                updateState { it.copy(codeCopied = true) }
            }
            OnboardingEvent.GrantMicPermission -> {
                updateState { it.copy(micGranted = true) }
            }
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            settingsStore.setOnboardingDone(true)
            sendEffect(OnboardingEffect.FinishOnboarding)
        }
    }
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    onFinish: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingEffect.FinishOnboarding -> onFinish()
            }
        }
    }

    OnboardingContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastStep = state.step == state.totalSteps - 1
    val canProceed = if (state.isForeman) {
        true
    } else {
        when (state.step) {
            0 -> state.isJoined
            1 -> true
            2 -> state.isAnswerCorrect
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.step > 0) {
                    IconButton(
                        onClick = { onEvent(OnboardingEvent.Prev) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.home_ic_arrow_back),
                            contentDescription = stringResource(R.string.home_ob_back),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(state.totalSteps) { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == state.step) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx <= state.step) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                        )
                    }
                }

                TextButton(
                    onClick = { onEvent(OnboardingEvent.Skip) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_ob_skip),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Button(
                        onClick = { onEvent(OnboardingEvent.Next) },
                        enabled = canProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = if (isLastStep) {
                                stringResource(R.string.home_ob_start)
                            } else {
                                stringResource(R.string.home_ob_next)
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.isForeman) {
                when (state.step) {
                    0 -> ForemanStep0(state = state, onEvent = onEvent)
                    1 -> ForemanStep1(state = state, onEvent = onEvent)
                }
            } else {
                when (state.step) {
                    0 -> WorkerStep0(state = state, onEvent = onEvent)
                    1 -> WorkerStep1(state = state, onEvent = onEvent)
                    2 -> WorkerStep2(state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@Composable
private fun ForemanStep0(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Icon(
        painter = painterResource(R.drawable.home_ic_groups),
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.home_ob_fm_step0_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.home_ob_fm_step0_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))

    // QR box
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.home_ic_qr),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Code text
    Text(
        text = state.teamCode,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(16.dp))

    FilledTonalButton(
        onClick = { onEvent(OnboardingEvent.CopyTeamCode) },
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.home_ic_copy),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (state.codeCopied) {
                stringResource(R.string.home_ob_fm_code_copied)
            } else {
                stringResource(R.string.home_ob_fm_copy_code)
            },
        )
    }
}

@Composable
private fun ForemanStep1(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Icon(
        painter = painterResource(R.drawable.home_ic_mic),
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.home_ob_fm_step1_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.home_ob_fm_step1_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))

    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PointRow(text = stringResource(R.string.home_ob_fm_mic_pt1))
            PointRow(text = stringResource(R.string.home_ob_fm_mic_pt2))
            PointRow(text = stringResource(R.string.home_ob_fm_mic_pt3))
        }
    }
    Spacer(modifier = Modifier.height(24.dp))

    if (state.micGranted) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.home_ic_check),
                contentDescription = null,
                tint = StatusColors.Green,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.home_ob_fm_mic_granted),
                color = StatusColors.Green,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        FilledTonalButton(
            onClick = { onEvent(OnboardingEvent.GrantMicPermission) },
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.home_ic_mic),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.home_ob_fm_mic_grant))
        }
    }
}

@Composable
private fun PointRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.home_ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WorkerStep0(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Icon(
        painter = painterResource(R.drawable.home_ic_qr),
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.home_ob_w_step0_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.home_ob_w_step0_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))

    if (state.isJoined) {
        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StatusColors.Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.home_ic_check),
                        contentDescription = null,
                        tint = StatusColors.Green,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.home_ob_w_joined_team),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.home_ob_w_joined_as, state.userName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                ) {
                    Avatar(name = "Tanaka", size = 32.dp)
                    Avatar(name = "Nguyen", size = 32.dp)
                    Avatar(name = "Siti", size = 32.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.home_ob_w_team_plus_you),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { onEvent(OnboardingEvent.ScanTeamQr) },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.home_ic_qr),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.isScanning) {
                        stringResource(R.string.home_ob_w_scanning)
                    } else {
                        stringResource(R.string.home_ob_w_tap_to_scan)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun WorkerStep1(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    Icon(
        painter = painterResource(R.drawable.home_ic_globe),
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.home_ob_w_step1_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.home_ob_w_step1_desc),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(24.dp))

    val langs = listOf(
        "en" to ("English" to "English"),
        "ja" to ("日本語" to "Japanese"),
    )

    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            langs.forEach { (code, names) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(OnboardingEvent.SelectLanguage(code)) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = names.first, fontWeight = FontWeight.Bold)
                        Text(text = names.second, style = MaterialTheme.typography.bodySmall)
                    }
                    RadioButton(
                        selected = state.selectedLanguage == code,
                        onClick = { onEvent(OnboardingEvent.SelectLanguage(code)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerStep2(
    state: OnboardingUiState,
    onEvent: (OnboardingEvent) -> Unit,
) {
    if (state.isAnswerCorrect) {
        // Celebration View
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(StatusColors.Green.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.home_ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = StatusColors.Green,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.home_ob_w_ready_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_ob_w_ready_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Icon(
            painter = painterResource(R.drawable.home_ic_help),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_ob_w_step2_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_ob_w_step2_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))

        WakattaCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.home_ob_w_q_head_protect),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))

                val options = listOf(
                    stringResource(R.string.home_ob_w_opt_helmet),
                    stringResource(R.string.home_ob_w_opt_gloves),
                    stringResource(R.string.home_ob_w_opt_boots),
                )

                options.forEachIndexed { index, option ->
                    val isSelected = state.selectedAnswerIndex == index
                    val isWrong = isSelected && state.isAnswerIncorrect
                    FilledTonalButton(
                        onClick = { onEvent(OnboardingEvent.SelectCheckAnswer(index)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .heightIn(min = 48.dp),
                        colors = if (isWrong) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        },
                    ) {
                        Text(text = option)
                    }
                }

                if (state.isAnswerIncorrect) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_ob_w_q_wrong),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
