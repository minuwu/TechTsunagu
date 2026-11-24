package com.techtsunagu.wakatta.feature.nearmiss

import android.graphics.BitmapFactory
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.StatusChip
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.rememberDictation
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.PhotoCapture
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.nearmiss.data.QueuedNearMiss
import com.techtsunagu.wakatta.feature.nearmiss.data.nearMissOutboxKind
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NearMissNewUiState(
    val machineId: String? = null,
    val machines: List<Machine> = emptyList(),
    val text: String = "",
    val severity: NearMissSeverity = NearMissSeverity.MID,
    val hasPhoto: Boolean = false,
    val photoUrl: String? = null,
    val photoFile: File? = null,
    val photoMark: PhotoMark = PhotoMark(56f, 46f),
    val recentReports: List<NearMissDto> = emptyList(),
    val isForeman: Boolean = false,
    val foremanName: String = "",
    val userLang: String = "en",
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val isQueuedOffline: Boolean = false,
    val sendFailed: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null,
)

sealed interface NearMissNewEvent {
    data class Init(val machineId: String?) : NearMissNewEvent
    data class SelectMachine(val machineId: String) : NearMissNewEvent
    data class UpdateText(val text: String) : NearMissNewEvent
    data class SelectSeverity(val severity: NearMissSeverity) : NearMissNewEvent
    data class AddPhoto(val file: File? = null, val url: String? = null) : NearMissNewEvent
    data class UpdatePhotoMark(val x: Float, val y: Float) : NearMissNewEvent
    data object RemovePhoto : NearMissNewEvent
    data object SendReport : NearMissNewEvent
    data object Reset : NearMissNewEvent
    data object Retry : NearMissNewEvent
    data object Refresh : NearMissNewEvent
}

sealed interface NearMissNewEffect {
    data object NavigateBack : NearMissNewEffect
    data class NavigateToDetail(val id: String) : NearMissNewEffect
    data object NavigateToList : NearMissNewEffect
}

@HiltViewModel
class NearMissNewViewModel @Inject constructor(
    private val nearMissRepository: NearMissRepository,
    private val machineRepository: MachineRepository,
    private val photoCapture: PhotoCapture,
    private val outboxWriter: OutboxWriter,
    private val sessionStore: SessionStore,
) : MviViewModel<NearMissNewUiState, NearMissNewEvent, NearMissNewEffect>(NearMissNewUiState()) {

    /** Kept until the report is out, so a second tap after an error is the same report. */
    private var sendKey: String = UUID.randomUUID().toString()

    override fun onEvent(event: NearMissNewEvent) {
        when (event) {
            is NearMissNewEvent.Init -> loadInitialData(event.machineId)
            // R6: picking another Machine keeps what the Worker already wrote and the photo
            is NearMissNewEvent.SelectMachine -> updateState { it.copy(machineId = event.machineId) }
            is NearMissNewEvent.UpdateText -> updateState { it.copy(text = event.text) }
            is NearMissNewEvent.SelectSeverity -> updateState { it.copy(severity = event.severity) }
            is NearMissNewEvent.AddPhoto -> {
                if (event.file != null || event.url != null) {
                    updateState {
                        it.copy(
                            hasPhoto = true,
                            photoFile = event.file,
                            photoUrl = event.url ?: event.file?.absolutePath,
                            photoMark = PhotoMark(56f, 46f),
                        )
                    }
                } else {
                    viewModelScope.launch {
                        val file = photoCapture.captureOrPick()
                        if (file != null) {
                            updateState {
                                it.copy(
                                    hasPhoto = true,
                                    photoFile = file,
                                    photoUrl = file.absolutePath,
                                    photoMark = PhotoMark(56f, 46f),
                                )
                            }
                        }
                    }
                }
            }
            is NearMissNewEvent.UpdatePhotoMark -> {
                updateState { it.copy(photoMark = PhotoMark(event.x, event.y)) }
            }
            is NearMissNewEvent.RemovePhoto -> {
                updateState {
                    it.copy(
                        hasPhoto = false,
                        photoFile = null,
                        photoUrl = null,
                        photoMark = PhotoMark(56f, 46f),
                    )
                }
            }
            is NearMissNewEvent.SendReport -> submitReport()
            is NearMissNewEvent.Reset -> resetForm()
            is NearMissNewEvent.Retry -> loadInitialData(state.value.machineId)
            is NearMissNewEvent.Refresh -> loadInitialData(state.value.machineId)
        }
    }

    private fun loadInitialData(initialMachineId: String?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            val session = sessionStore.session.firstOrNull()
            val isForeman = session?.user?.role == Role.FOREMAN
            val userLang = session?.user?.language ?: "en"

            val machinesResult = machineRepository.getMachines()
            val machines = if (machinesResult is AppResult.Success) machinesResult.value else emptyList()

            val selectedMachineId = initialMachineId ?: state.value.machineId ?: machines.firstOrNull()?.id

            val reportsResult = nearMissRepository.getNearMisses()
            val recent = if (reportsResult is AppResult.Success) {
                val userId = session?.user?.id.orEmpty()
                if (userId.isNotEmpty()) reportsResult.value.filter { it.userId == userId }.take(3)
                else emptyList()
            } else {
                emptyList()
            }

            // A queued report the server refused comes back into the form.
            val refused = session?.user?.id?.let { outboxWriter.takeFailed(nearMissOutboxKind(it)) }?.let(QueuedNearMiss::decode)

            updateState {
                it.copy(
                    isLoading = false,
                    isForeman = isForeman,
                    userLang = userLang,
                    machines = machines,
                    machineId = selectedMachineId,
                    recentReports = recent,
                )
            }
            if (refused != null) {
                updateState {
                    it.copy(
                        machineId = refused.machineId,
                        text = refused.text,
                        severity = refused.severity,
                        hasPhoto = refused.photo != null,
                        photoFile = refused.photo?.let(::File),
                        photoUrl = refused.photo,
                        photoMark = refused.photoMark ?: it.photoMark,
                        sendFailed = true,
                    )
                }
            }
        }
    }

    private fun submitReport() {
        val form = state.value
        val mid = form.machineId ?: return
        val text = form.text.trim()
        if (text.length <= 2 || form.isLoading) return
        val userId = sessionStore.session.value?.user?.id ?: return
        updateState { it.copy(isLoading = true, sendFailed = false) }
        viewModelScope.launch {
            val request = NearMissCreateRequest(
                machineId = mid,
                text = text,
                severity = form.severity,
                photoFile = form.photoFile,
                photoUrl = form.photoUrl,
                photoMark = if (form.hasPhoto) form.photoMark else null,
                idempotencyKey = sendKey,
            )
            when (val result = nearMissRepository.createNearMiss(request)) {
                is AppResult.Success -> updateState { it.copy(isLoading = false, isSent = true, isQueuedOffline = false) }
                is AppResult.Failure -> when (result.error) {
                    AppError.Offline, AppError.Timeout, AppError.Server, AppError.Busy -> {
                        outboxWriter.enqueue(nearMissOutboxKind(userId), sendKey, QueuedNearMiss.of(request).encode())
                        updateState { it.copy(isLoading = false, isSent = true, isQueuedOffline = true) }
                    }
                    else -> updateState { it.copy(isLoading = false, sendFailed = true) }
                }
            }
        }
    }

    private fun resetForm() {
        sendKey = UUID.randomUUID().toString()
        updateState {
            it.copy(
                text = "",
                hasPhoto = false,
                photoFile = null,
                photoUrl = null,
                photoMark = PhotoMark(56f, 46f),
                isSent = false,
                isQueuedOffline = false,
                error = null,
            )
        }
    }
}

@Composable
fun NearMissNewScreen(
    machineId: String? = null,
    onBackClick: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: NearMissNewViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(machineId) {
        viewModel.onEvent(NearMissNewEvent.Init(machineId))
    }

    NearMissNewContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onNavigateToList = onNavigateToList,
        onNavigateToDetail = onNavigateToDetail,
    )
}

@Composable
fun NearMissNewContent(
    state: NearMissNewUiState,
    onEvent: (NearMissNewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.nearmiss_new_screen_title),
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
                state.isLoading && state.machines.isEmpty() -> {
                    LoadingState(message = stringResource(R.string.nearmiss_loading))
                }
                state.error != null -> {
                    EmptyState(
                        title = stringResource(R.string.nearmiss_error),
                        message = stringResource(R.string.nearmiss_error_hint),
                        actionLabel = stringResource(R.string.nearmiss_retry),
                        onActionClick = { onEvent(NearMissNewEvent.Retry) },
                    )
                }
                state.isForeman -> {
                    ForemanNearMissPlaceholder(onNavigateToList = onNavigateToList)
                }
                state.isSent -> {
                    SentSuccessView(
                        foremanName = state.foremanName,
                        isQueued = state.isQueuedOffline,
                        onDone = onBackClick,
                        onReportAnother = { onEvent(NearMissNewEvent.Reset) },
                    )
                }
                else -> {
                    WorkerNearMissForm(
                        state = state,
                        onEvent = onEvent,
                        onNavigateToDetail = onNavigateToDetail,
                    )
                }
            }
        }
    }
}

@Composable
private fun ForemanNearMissPlaceholder(
    onNavigateToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.nearmiss_foreman_info),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTonalButton(
            onClick = onNavigateToList,
            modifier = Modifier.height(48.dp),
        ) {
            Text(stringResource(R.string.nearmiss_view_list))
        }
    }
}

@Composable
private fun SentSuccessView(
    foremanName: String,
    isQueued: Boolean,
    onDone: () -> Unit,
    onReportAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isQueued) stringResource(R.string.nearmiss_status_waiting) else stringResource(R.string.nearmiss_sent_title, foremanName),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(if (isQueued) R.string.nearmiss_queued_message else R.string.nearmiss_sent_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text(stringResource(R.string.nearmiss_done))
            }
            Button(
                onClick = onReportAnother,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text(stringResource(R.string.nearmiss_report_another))
            }
        }
    }
}

@Composable
private fun WorkerNearMissForm(
    state: NearMissNewUiState,
    onEvent: (NearMissNewEvent) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Step 1: Which machine?
        Text(
            text = stringResource(R.string.nearmiss_step_machine),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MachineChipsRow(
            machines = state.machines,
            selectedMachineId = state.machineId,
            userLang = state.userLang,
            onSelectMachine = { onEvent(NearMissNewEvent.SelectMachine(it)) },
        )

        // Step 2: What happened?
        Text(
            text = stringResource(R.string.nearmiss_step_what),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val dictation = rememberDictation(state.userLang, state.text) { onEvent(NearMissNewEvent.UpdateText(it)) }
        RecordSection(
            isRecording = dictation.isListening,
            durationSeconds = dictation.seconds,
            onToggle = dictation.toggle,
        )
        dictation.errorRes?.let {
            Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(
            value = state.text,
            onValueChange = { onEvent(NearMissNewEvent.UpdateText(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text(stringResource(R.string.nearmiss_text_placeholder)) },
        )
        Text(
            text = stringResource(R.string.nearmiss_auto_translate_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Step 3: Photo (optional)
        Text(
            text = stringResource(R.string.nearmiss_step_photo),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PhotoAnnotationSection(
            hasPhoto = state.hasPhoto,
            photo = state.photoFile,
            mark = state.photoMark,
            onAddPhoto = { onEvent(NearMissNewEvent.AddPhoto()) },
            onRemovePhoto = { onEvent(NearMissNewEvent.RemovePhoto) },
            onUpdateMark = { x, y -> onEvent(NearMissNewEvent.UpdatePhotoMark(x, y)) },
        )

        // Step 4: Severity
        Text(
            text = stringResource(R.string.nearmiss_step_severity),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SeverityChipsRow(
            selected = state.severity,
            onSelect = { onEvent(NearMissNewEvent.SelectSeverity(it)) },
        )

        // My reports list
        if (state.recentReports.isNotEmpty()) {
            Text(
                text = stringResource(R.string.nearmiss_my_reports),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RecentReportsCard(
                reports = state.recentReports,
                userLang = state.userLang,
                onReportClick = onNavigateToDetail,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Send Button
        if (state.sendFailed) {
            Text(
                text = stringResource(R.string.nearmiss_send_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        val canSend = state.machineId != null && state.text.trim().length > 2 && !state.isLoading
        Button(
            onClick = { onEvent(NearMissNewEvent.SendReport) },
            enabled = canSend,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (state.foremanName.isNotEmpty()) {
                    stringResource(R.string.nearmiss_send_button, state.foremanName)
                } else {
                    stringResource(R.string.nearmiss_send_button_default)
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MachineChipsRow(
    machines: List<Machine>,
    selectedMachineId: String?,
    userLang: String,
    onSelectMachine: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        machines.forEach { machine ->
            val label = pick(machine.name, userLang)
            WakattaChip(
                text = if (label.isNotEmpty()) label else machine.code,
                selected = machine.id == selectedMachineId,
                onClick = { onSelectMachine(machine.id) },
            )
        }
    }
}

@Composable
private fun RecordSection(
    isRecording: Boolean,
    durationSeconds: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val buttonColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        IconButton(
            onClick = onToggle,
            modifier = Modifier
                .size(64.dp)
                .background(color = buttonColor, shape = CircleShape),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.nearmiss_mic_label),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }

        // Waveform indicator
        WaveformBars(isRecording = isRecording, modifier = Modifier.weight(1f))

        Text(
            text = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WaveformBars(
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave_scale",
    )

    Row(
        modifier = modifier.height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val barCount = 12
        repeat(barCount) { index ->
            val factor = if (isRecording) {
                val offset = (index % 3) * 0.2f
                (animScale + offset).coerceIn(0.2f, 1f)
            } else {
                0.2f
            }
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((32 * factor).dp)
                    .background(
                        color = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

@Composable
private fun PhotoAnnotationSection(
    hasPhoto: Boolean,
    photo: File?,
    mark: PhotoMark,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onUpdateMark: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!hasPhoto) {
        FilledTonalButton(
            onClick = onAddPhoto,
            modifier = modifier.height(48.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.nearmiss_add_photo))
        }
    } else {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xPercent = ((offset.x / size.width) * 100f).coerceIn(5f, 95f)
                            val yPercent = ((offset.y / size.height) * 100f).coerceIn(5f, 95f)
                            onUpdateMark(xPercent, yPercent)
                        }
                    },
            ) {
                val bitmap by produceState<ImageBitmap?>(null, photo) {
                    value = photo?.let { file ->
                        withContext(Dispatchers.IO) {
                            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
                        }
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it,
                        contentDescription = stringResource(R.string.nearmiss_step_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Red circle ring at target spot
                val ringSize = 48.dp
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = ((mark.x / 100f) * constraints.maxWidth - (ringSize.toPx() / 2)).roundToInt(),
                                y = ((mark.y / 100f) * constraints.maxHeight - (ringSize.toPx() / 2)).roundToInt(),
                            )
                        }
                        .size(ringSize)
                        .border(width = 3.dp, color = MaterialTheme.colorScheme.error, shape = CircleShape),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.nearmiss_tap_circle_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRemovePhoto) {
                    Text(
                        text = stringResource(R.string.nearmiss_remove_photo),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityChipsRow(
    selected: NearMissSeverity,
    onSelect: (NearMissSeverity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val options = listOf(
            Triple(NearMissSeverity.LOW, stringResource(R.string.nearmiss_severity_low), Color(0xFF2E7D32)),
            Triple(NearMissSeverity.MID, stringResource(R.string.nearmiss_severity_mid), Color(0xFFED6C02)),
            Triple(NearMissSeverity.HIGH, stringResource(R.string.nearmiss_severity_high), Color(0xFFD32F2F)),
        )
        options.forEach { (sev, label, dotColor) ->
            WakattaChip(
                selected = selected == sev,
                onClick = { onSelect(sev) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(dotColor, CircleShape),
                    )
                },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun RecentReportsCard(
    reports: List<NearMissDto>,
    userLang: String,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    WakattaCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            reports.forEach { report ->
                Surface(
                    onClick = { onReportClick(report.id) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pick(report.text, userLang).ifEmpty { report.text["en"] ?: "" },
                                maxLines = 1,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${report.machineId} · ${report.createdAt.take(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        val status = when {
                            report.status == NearMissStatus.ACK -> WakattaStatus.GREEN
                            report.isQueuedOffline -> WakattaStatus.WAIT
                            else -> WakattaStatus.ORANGE
                        }
                        val label = when {
                            report.status == NearMissStatus.ACK -> stringResource(R.string.nearmiss_filter_ack)
                            report.isQueuedOffline -> stringResource(R.string.nearmiss_status_waiting)
                            else -> stringResource(R.string.nearmiss_status_sent)
                        }
                        StatusChip(status = status, label = label)
                    }
                }
            }
        }
    }
}
