package com.techtsunagu.wakatta.feature.home

import android.content.ClipData
import android.content.Intent
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.techtsunagu.wakatta.core.common.MviViewModel
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.feature.home.data.Member
import com.techtsunagu.wakatta.feature.home.data.MembersSource
import com.techtsunagu.wakatta.feature.home.data.firstPin
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** What the Foreman hands over: the shop code, the name to pick and a first PIN the person changes at sign-in. */
data class Invite(val name: String, val role: Role, val pin: String)

data class MembersUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val members: List<Member> = emptyList(),
    val myId: String = "",
    val shopCode: String = "",
    val adding: Boolean = false,
    val editing: Member? = null,
    val invite: Invite? = null,
    val isSaving: Boolean = false,
    @StringRes val error: Int? = null,
)

sealed interface MembersEvent {
    data object Load : MembersEvent
    data class OpenAdd(val open: Boolean) : MembersEvent
    data class Add(val name: String, val role: Role, val language: String) : MembersEvent
    data class Open(val member: Member?) : MembersEvent
    data class Rename(val member: Member, val name: String) : MembersEvent
    data class ResetPin(val member: Member) : MembersEvent
    data class SetActive(val member: Member, val active: Boolean) : MembersEvent
    data object CloseInvite : MembersEvent
}

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val source: MembersSource,
    sessionStore: SessionStore,
) : MviViewModel<MembersUiState, MembersEvent, Unit>(MembersUiState()) {

    init {
        sessionStore.session.value?.user?.let { u -> updateState { it.copy(myId = u.id, shopCode = u.shopCode.orEmpty()) } }
        load()
    }

    override fun onEvent(event: MembersEvent) {
        when (event) {
            MembersEvent.Load -> load()
            is MembersEvent.OpenAdd -> updateState { it.copy(adding = event.open, error = null) }
            is MembersEvent.Open -> updateState { it.copy(editing = event.member, error = null) }
            MembersEvent.CloseInvite -> updateState { it.copy(invite = null) }
            is MembersEvent.Add -> {
                val pin = firstPin(event.role)
                save({ source.add(event.role, event.name.trim(), event.language, pin) }) {
                    copy(adding = false, invite = Invite(it.name, it.role, pin))
                }
            }
            is MembersEvent.Rename -> save({ source.update(event.member, name = event.name.trim()) }) { copy(editing = null) }
            is MembersEvent.ResetPin -> {
                val pin = firstPin(event.member.role)
                save({ source.update(event.member, pin = pin) }) { copy(editing = null, invite = Invite(it.name, it.role, pin)) }
            }
            is MembersEvent.SetActive -> save({ source.update(event.member, active = event.active) }) { copy(editing = null) }
        }
    }

    private fun load() {
        updateState { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            when (val r = source.list()) {
                is AppResult.Success -> updateState { it.copy(isLoading = false, members = r.value) }
                is AppResult.Failure -> updateState { it.copy(isLoading = false, loadFailed = true) }
            }
        }
    }

    private fun save(call: suspend () -> AppResult<Member>, done: MembersUiState.(Member) -> MembersUiState) {
        updateState { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val r = call()) {
                is AppResult.Success -> updateState { s ->
                    val list = s.members.filterNot { it.id == r.value.id } + r.value
                    s.copy(isSaving = false, members = list).done(r.value)
                }
                is AppResult.Failure -> updateState { it.copy(isSaving = false, error = errorText(r.error)) }
            }
        }
    }

    private fun errorText(e: AppError) = when (e) {
        AppError.Offline, AppError.Timeout -> R.string.home_mb_err_offline
        is AppError.Validation -> R.string.home_mb_err_invalid
        is AppError.Conflict -> R.string.home_mb_err_last_foreman
        else -> R.string.home_mb_err_unknown
    }
}

@Composable
fun MembersScreen(onBack: () -> Unit, viewModel: MembersViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MembersContent(state, viewModel::onEvent, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersContent(state: MembersUiState, onEvent: (MembersEvent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = { WakattaTopBar(title = stringResource(R.string.home_mb_title), onBackClick = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(MembersEvent.OpenAdd(true)) },
                icon = { Icon(Icons.Default.PersonAdd, null) },
                text = { Text(stringResource(R.string.home_mb_add)) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.loadFailed -> ErrorState(
                message = stringResource(R.string.home_mb_err_offline),
                modifier = Modifier.padding(padding),
                onRetry = { onEvent(MembersEvent.Load) },
            )
            else -> MemberList(state, onEvent, PaddingValues(top = padding.calculateTopPadding()))
        }
    }

    if (state.adding) {
        ModalBottomSheet(onDismissRequest = { onEvent(MembersEvent.OpenAdd(false)) }) { AddSheet(state, onEvent) }
    }
    state.editing?.let { m ->
        ModalBottomSheet(onDismissRequest = { onEvent(MembersEvent.Open(null)) }) { EditSheet(m, state, onEvent) }
    }
    state.invite?.let { inv ->
        ModalBottomSheet(onDismissRequest = { onEvent(MembersEvent.CloseInvite) }) { InviteSheet(inv, state.shopCode, onEvent) }
    }
}

@Composable
private fun MemberList(state: MembersUiState, onEvent: (MembersEvent) -> Unit, padding: PaddingValues) {
    val (foremen, workers) = state.members.sortedBy { it.name }.partition { it.role == Role.FOREMAN }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.shopCode.isNotEmpty()) {
            item(key = "code") { ShopCodeCard(state.shopCode) }
        }
        listOf(R.string.home_mb_workers to workers, R.string.home_mb_foremen to foremen).forEach { (label, people) ->
            item(key = "h$label") {
                Text(
                    stringResource(label, people.size),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                )
            }
            if (people.isEmpty()) {
                item(key = "e$label") {
                    Text(
                        stringResource(R.string.home_mb_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            items(people, key = { it.id }) { m -> MemberRow(m, isMe = m.id == state.myId) { onEvent(MembersEvent.Open(m)) } }
        }
    }
}

@Composable
private fun ShopCodeCard(code: String) {
    val context = LocalContext.current
    val share = stringResource(R.string.home_mb_share_code, code)
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_mb_code),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    code,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            TextButton(onClick = { shareText(context, share) }) {
                Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.home_mb_share))
            }
        }
    }
}

@Composable
private fun MemberRow(m: Member, isMe: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(enabled = !isMe, onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = m.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(m.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                Text(
                    if (m.language == "ja") "日本語" else "English",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                isMe -> Tag(stringResource(R.string.home_mb_me), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                !m.active -> Tag(stringResource(R.string.home_mb_stopped), StatusColors.GreyContainer, StatusColors.OnGreyContainer)
                m.mustChangePin -> Tag(stringResource(R.string.home_mb_invited), StatusColors.OrangeContainer, StatusColors.OnOrangeContainer)
            }
            if (!isMe) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Tag(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = fg,
        modifier = Modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun AddSheet(state: MembersUiState, onEvent: (MembersEvent) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Role.WORKER) }
    var language by remember { mutableStateOf("en") }
    SheetColumn {
        Text(stringResource(R.string.home_mb_add_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(stringResource(R.string.home_mb_add_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40) },
            label = { Text(stringResource(R.string.home_mb_name)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Choice(R.string.home_mb_role) {
            FilterChip(role == Role.WORKER, { role = Role.WORKER }, { Text(stringResource(R.string.home_mb_worker)) })
            FilterChip(role == Role.FOREMAN, { role = Role.FOREMAN; language = "ja" }, { Text(stringResource(R.string.home_mb_foreman)) })
        }
        Choice(R.string.home_mb_language) {
            FilterChip(language == "ja", { language = "ja" }, { Text("日本語") })
            FilterChip(language == "en", { language = "en" }, { Text("English") })
        }
        state.error?.let { ErrorText(it) }
        Button(
            onClick = { onEvent(MembersEvent.Add(name, role, language)) },
            enabled = name.isNotBlank() && !state.isSaving,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.home_mb_add_submit), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun EditSheet(m: Member, state: MembersUiState, onEvent: (MembersEvent) -> Unit) {
    var name by remember(m.id) { mutableStateOf(m.name) }
    SheetColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = m.name, size = 48.dp)
            Spacer(Modifier.width(12.dp))
            Text(m.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(40) },
            label = { Text(stringResource(R.string.home_mb_name)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (name.isNotBlank() && name.trim() != m.name) {
                    TextButton(onClick = { onEvent(MembersEvent.Rename(m, name)) }) { Text(stringResource(R.string.home_mb_save)) }
                }
            },
        )
        state.error?.let { ErrorText(it) }
        OutlinedButton(
            onClick = { onEvent(MembersEvent.ResetPin(m)) },
            enabled = !state.isSaving,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(stringResource(R.string.home_mb_reset_pin)) }
        Text(stringResource(R.string.home_mb_reset_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(
            onClick = { onEvent(MembersEvent.SetActive(m, !m.active)) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(
                stringResource(if (m.active) R.string.home_mb_stop else R.string.home_mb_resume),
                color = if (m.active) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun InviteSheet(inv: Invite, shopCode: String, onEvent: (MembersEvent) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val text = stringResource(R.string.home_mb_invite_text, inv.name, shopCode, inv.pin)
    SheetColumn {
        Text(stringResource(R.string.home_mb_invite_title, inv.name), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(stringResource(R.string.home_mb_invite_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InviteLine(R.string.home_mb_code, shopCode)
                InviteLine(R.string.home_mb_name, inv.name)
                InviteLine(R.string.home_mb_first_pin, inv.pin)
            }
        }
        Button(
            onClick = { shareText(context, text) },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Icon(Icons.Default.Share, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.home_mb_share_invite), style = MaterialTheme.typography.titleSmall)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("invite", text))) } },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.weight(1f).height(48.dp),
            ) {
                Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.home_mb_copy))
            }
            TextButton(onClick = { onEvent(MembersEvent.CloseInvite) }, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(stringResource(R.string.home_mb_done))
            }
        }
    }
}

@Composable
private fun InviteLine(@StringRes label: Int, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
    }
}

@Composable
private fun SheetColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) { content() }
}

@Composable
private fun Choice(@StringRes label: Int, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { chips() }
    }
}

@Composable
private fun ErrorText(@StringRes text: Int) {
    Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(stringResource(text), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(send, null))
}
