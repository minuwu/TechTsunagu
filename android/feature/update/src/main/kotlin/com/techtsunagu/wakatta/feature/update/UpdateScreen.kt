package com.techtsunagu.wakatta.feature.update

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class UpdatePhase { Checking, UpToDate, Available, Downloading, Installing, Done, Failed }

enum class UpdateError { Network, Checksum, Install, NoDownload }

data class UpdateUiState(
    val phase: UpdatePhase = UpdatePhase.Checking,
    val isHard: Boolean = false,
    val latestVersionName: String = "",
    val canDownload: Boolean = false,
    val progressPercent: Int = 0,
    val needsInstallPermission: Boolean = false,
    val error: UpdateError? = null,
)

sealed interface UpdateEvent {
    data object Check : UpdateEvent
    data object Retry : UpdateEvent
    data object Download : UpdateEvent
    data object Cancel : UpdateEvent
    data object AllowInstall : UpdateEvent
    data object Resume : UpdateEvent
}

sealed interface UpdateEffect {
    data object OpenInstallSettings : UpdateEffect
}

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val gate: UpdateVersionGate,
    private val downloader: ApkDownloader,
    private val installer: ApkInstaller,
) : MviViewModel<UpdateUiState, UpdateEvent, UpdateEffect>(UpdateUiState()) {

    private var offer: UpdateOffer? = null
    private var apk: File? = null
    private var job: Job? = null

    init {
        check()
    }

    override fun onEvent(event: UpdateEvent) {
        when (event) {
            UpdateEvent.Check -> check()
            UpdateEvent.Retry -> if (offer != null && state.value.canDownload) download() else check()
            UpdateEvent.Download -> download()
            UpdateEvent.Cancel -> cancel()
            UpdateEvent.AllowInstall -> sendEffect(UpdateEffect.OpenInstallSettings)
            UpdateEvent.Resume -> if (state.value.needsInstallPermission && installer.canRequestInstalls()) install()
        }
    }

    private fun check() {
        job?.cancel()
        job = viewModelScope.launch {
            updateState { UpdateUiState() }
            when (val result = gate.refresh()) {
                is AppResult.Success -> {
                    offer = result.value
                    updateState { result.value.toState() }
                }
                is AppResult.Failure -> updateState {
                    it.copy(
                        phase = UpdatePhase.Failed,
                        error = UpdateError.Network,
                        isHard = result.error == AppError.UpgradeRequired,
                    )
                }
            }
        }
    }

    private fun UpdateOffer.toState() = UpdateUiState(
        phase = if (level == VersionGateResult.UpToDate) UpdatePhase.UpToDate else UpdatePhase.Available,
        isHard = level == VersionGateResult.Hard,
        latestVersionName = latestVersionName,
        // An APK without a SHA-256 cannot be verified, so it is never offered.
        canDownload = !downloadUrl.isNullOrBlank() && !sha256.isNullOrBlank(),
    )

    private fun download() {
        val url = offer?.downloadUrl
        val sha = offer?.sha256
        if (url.isNullOrBlank() || sha.isNullOrBlank()) {
            fail(UpdateError.NoDownload)
            return
        }
        job?.cancel()
        job = viewModelScope.launch {
            updateState { it.copy(phase = UpdatePhase.Downloading, progressPercent = 0, error = null) }
            val result = downloader.download(url, sha) { percent ->
                updateState { if (it.phase == UpdatePhase.Downloading) it.copy(progressPercent = percent) else it }
            }
            when (result) {
                is DownloadResult.Done -> {
                    apk = result.file
                    install()
                }
                DownloadResult.ChecksumMismatch -> fail(UpdateError.Checksum)
                DownloadResult.Failed -> fail(UpdateError.Network)
            }
        }
    }

    private fun cancel() {
        job?.cancel()
        updateState { it.copy(phase = UpdatePhase.Available, progressPercent = 0) }
    }

    private fun install() {
        val file = apk ?: return
        if (!installer.canRequestInstalls()) {
            updateState { it.copy(phase = UpdatePhase.Installing, needsInstallPermission = true) }
            sendEffect(UpdateEffect.OpenInstallSettings)
            return
        }
        try {
            installer.install(file)
            updateState { it.copy(phase = UpdatePhase.Installing, needsInstallPermission = false) }
        } catch (_: IOException) {
            fail(UpdateError.Install)
        } catch (_: SecurityException) {
            fail(UpdateError.Install)
        }
    }

    private fun fail(error: UpdateError) {
        updateState { it.copy(phase = UpdatePhase.Failed, error = error, needsInstallPermission = false) }
    }
}

@Composable
fun UpdateScreen(
    onBackClick: () -> Unit = {},
    onSignOutClick: (() -> Unit)? = null,
    viewModel: UpdateViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                UpdateEffect.OpenInstallSettings -> context.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onEvent(UpdateEvent.Resume) }
    UpdateContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onSignOutClick = onSignOutClick,
    )
}

@Composable
fun UpdateContent(
    state: UpdateUiState,
    onEvent: (UpdateEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSignOutClick: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // A required update blocks the app: no way back.
            WakattaTopBar(
                title = stringResource(R.string.update_screen_title),
                onBackClick = if (state.isHard) null else onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state.phase) {
                UpdatePhase.Checking -> LoadingState(message = stringResource(R.string.update_checking))
                UpdatePhase.UpToDate -> EmptyState(message = stringResource(R.string.update_up_to_date))
                UpdatePhase.Available -> AvailableBody(state, onEvent, onSignOutClick)
                UpdatePhase.Downloading -> DownloadingBody(state, onEvent)
                UpdatePhase.Installing -> InstallingBody(state, onEvent)
                UpdatePhase.Done -> EmptyState(message = stringResource(R.string.update_done))
                UpdatePhase.Failed -> {
                    EmptyState(
                        message = errorText(state.error),
                        actionLabel = stringResource(R.string.update_retry),
                        onActionClick = { onEvent(UpdateEvent.Retry) },
                    )
                    if (state.isHard && onSignOutClick != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onSignOutClick, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.update_sign_out))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun errorText(error: UpdateError?): String = stringResource(
    when (error) {
        UpdateError.Checksum -> R.string.update_error_checksum
        UpdateError.Install -> R.string.update_error_install
        UpdateError.NoDownload -> R.string.update_no_download
        UpdateError.Network, null -> R.string.update_error_network
    },
)

@Composable
private fun AvailableBody(
    state: UpdateUiState,
    onEvent: (UpdateEvent) -> Unit,
    onSignOutClick: (() -> Unit)? = null,
) {
    Text(
        text = stringResource(R.string.update_available, state.latestVersionName),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    if (state.isHard) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.update_required),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    if (state.canDownload) {
        Button(onClick = { onEvent(UpdateEvent.Download) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.update_download))
        }
    } else {
        Text(
            text = stringResource(R.string.update_no_download),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (state.isHard && onSignOutClick != null) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onSignOutClick, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.update_sign_out))
        }
    }
}

@Composable
private fun DownloadingBody(state: UpdateUiState, onEvent: (UpdateEvent) -> Unit) {
    Text(
        text = stringResource(R.string.update_downloading, state.progressPercent),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(12.dp))
    LinearProgressIndicator(progress = { state.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedButton(onClick = { onEvent(UpdateEvent.Cancel) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.update_cancel))
    }
}

@Composable
private fun InstallingBody(state: UpdateUiState, onEvent: (UpdateEvent) -> Unit) {
    if (state.needsInstallPermission) {
        Text(
            text = stringResource(R.string.update_allow_install_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onEvent(UpdateEvent.AllowInstall) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.update_allow_install))
        }
    } else {
        LoadingState(message = stringResource(R.string.update_installing))
    }
}
