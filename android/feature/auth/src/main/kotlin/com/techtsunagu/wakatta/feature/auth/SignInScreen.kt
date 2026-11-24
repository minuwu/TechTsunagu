package com.techtsunagu.wakatta.feature.auth

import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import android.content.Intent
import android.content.ClipData
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techtsunagu.wakatta.core.model.Role

@Composable
fun SignInScreen(
    onNavigateToChangePin: () -> Unit = {},
    onSignInSuccess: () -> Unit = {},
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prefs = LocalContext.current.getSharedPreferences(INTRO_PREFS, Context.MODE_PRIVATE)
    var showIntro by rememberSaveable { mutableStateOf(!prefs.getBoolean(INTRO_SEEN, false)) }
    // The form opens blank; the phone remembers the last shop as a one-tap button, so a forgotten code never locks
    // anyone out.
    val lastCode = remember { prefs.getString(LAST_CODE, null) }
    val useLastShop: () -> Unit = {
        Role.entries.firstOrNull { it.name == prefs.getString(LAST_ROLE, null) }
            ?.let { viewModel.onEvent(SignInEvent.OnRoleChanged(it)) }
        lastCode?.let { viewModel.onEvent(SignInEvent.OnShopCodeChanged(it)) }
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            val s = viewModel.state.value
            if (AuthRules.validateShopCode(s.shopCode)) {
                prefs.edit().putString(LAST_CODE, s.shopCode).putString(LAST_ROLE, s.role.name).apply()
            }
            when (effect) {
                SignInEffect.NavigateToHome -> onSignInSuccess()
                SignInEffect.NavigateToChangePin -> onNavigateToChangePin()
            }
        }
    }
    if (showIntro) {
        IntroScreen(onDone = {
            prefs.edit().putBoolean(INTRO_SEEN, true).apply()
            showIntro = false
        })
        return
    }
    SignInContent(
        state = state,
        onEvent = viewModel::onEvent,
        onShowIntro = { showIntro = true },
        lastCode = lastCode,
        onUseLastShop = useLastShop,
    )
}

private const val INTRO_PREFS = "auth_intro"
private const val INTRO_SEEN = "seen"
private const val LAST_CODE = "last_shop_code"
private const val LAST_ROLE = "last_role"

/** Sign-in, Shop Registration and the new shop's code. Japanese leads every label; English sits under it, smaller. */
@Composable
fun SignInContent(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    modifier: Modifier = Modifier,
    onShowIntro: () -> Unit = {},
    lastCode: String? = null,
    onUseLastShop: () -> Unit = {},
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "Wakatta?",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
            )
            BiHeading(R.string.auth_si_tagline_ja, R.string.auth_si_tagline_en, small = true)
            Spacer(Modifier.height(24.dp))
            val page = when {
                state.newShopCode != null -> 2
                state.registering -> 1
                state.finding -> 3
                else -> 0
            }
            AnimatedContent(targetState = page, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "auth") {
                when (it) {
                    2 -> ShopReady(state.newShopCode.orEmpty(), onEvent)
                    1 -> RegisterForm(state, onEvent)
                    3 -> FindForm(state, onEvent)
                    else -> SignInForm(state, onEvent, onShowIntro, lastCode, onUseLastShop)
                }
            }
        }
    }
}

@Composable
private fun SignInForm(
    state: SignInUiState,
    onEvent: (SignInEvent) -> Unit,
    onShowIntro: () -> Unit,
    lastCode: String?,
    onUseLastShop: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DemoAccounts(onPick = { onEvent(SignInEvent.PrefillDemo(it)) })

        Section {
            BiHeading(R.string.auth_si_title_ja, R.string.auth_si_title_en)
            RoleToggle(role = state.role, onRoleChange = { onEvent(SignInEvent.OnRoleChanged(it)) })
            OutlinedTextField(
                value = state.shopCode,
                onValueChange = { onEvent(SignInEvent.OnShopCodeChanged(it)) },
                label = { FieldLabel(R.string.auth_si_code_ja, R.string.auth_si_code_en) },
                supportingText = { Text(stringResource(R.string.auth_si_code_hint_ja) + " · " + stringResource(R.string.auth_si_code_hint_en)) },
                isError = state.shopCode.isNotEmpty() && !state.isShopCodeValid,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (lastCode != null && state.shopCode.isEmpty()) {
                TextButton(onClick = onUseLastShop) {
                    Text(stringResource(R.string.auth_si_last_shop, lastCode))
                }
            }
            if (state.error != null) {
                ErrorLine(state, onRetry = { onEvent(SignInEvent.Retry) })
            }
            when {
                state.isRosterLoading -> Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
                state.isEmpty -> BiHeading(R.string.auth_si_no_names_ja, R.string.auth_si_no_names_en, small = true)
                state.roster.isNotEmpty() -> {
                    BiHeading(R.string.auth_si_name_ja, R.string.auth_si_name_en, small = true)
                    state.roster.forEach { user ->
                        NameRow(user.displayName, selected = state.selectedUser?.id == user.id) {
                            onEvent(SignInEvent.OnUserSelected(user))
                        }
                    }
                    PinField(
                        value = state.pin,
                        onChange = { onEvent(SignInEvent.OnPinChanged(it)) },
                        ja = R.string.auth_si_pin_ja,
                        en = R.string.auth_si_pin_en,
                        isError = state.pin.isNotEmpty() && !state.isPinValid,
                    )
                }
            }
            PrimaryButton(
                ja = R.string.auth_si_submit_ja,
                en = R.string.auth_si_submit_en,
                enabled = state.isShopCodeValid && state.selectedUser != null && state.isPinValid,
                loading = state.isLoading,
                onClick = { onEvent(SignInEvent.SubmitSignIn) },
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            BiHeading(R.string.auth_si_no_code_ja, R.string.auth_si_no_code_en, small = true, center = true)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onEvent(SignInEvent.ShowRegister(true)) },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { BiLabel(R.string.auth_si_register_ja, R.string.auth_si_register_en) }
            TextButton(onClick = { onEvent(SignInEvent.ShowFind(true)) }, modifier = Modifier.heightIn(min = 48.dp)) {
                BiLabel(R.string.auth_fd_link_ja, R.string.auth_fd_link_en)
            }
            TextButton(onClick = onShowIntro, modifier = Modifier.heightIn(min = 48.dp)) {
                BiLabel(R.string.auth_si_what_ja, R.string.auth_si_what_en)
            }
        }
    }
}

@Composable
private fun RegisterForm(state: SignInUiState, onEvent: (SignInEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section {
            BiHeading(R.string.auth_rg_title_ja, R.string.auth_rg_title_en)
            BiHeading(R.string.auth_rg_note_ja, R.string.auth_rg_note_en, small = true)
            OutlinedTextField(
                value = state.shopName,
                onValueChange = { onEvent(SignInEvent.OnShopNameChanged(it)) },
                label = { FieldLabel(R.string.auth_rg_shop_ja, R.string.auth_rg_shop_en) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = { onEvent(SignInEvent.OnOwnerNameChanged(it)) },
                label = { FieldLabel(R.string.auth_rg_name_ja, R.string.auth_rg_name_en) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.wantedCode,
                onValueChange = { onEvent(SignInEvent.OnWantedCodeChanged(it)) },
                label = { FieldLabel(R.string.auth_rg_code_ja, R.string.auth_rg_code_en) },
                placeholder = { Text("TECHTSUNAGU") },
                supportingText = {
                    Text(stringResource(R.string.auth_rg_code_hint_ja) + " · " + stringResource(R.string.auth_rg_code_hint_en))
                },
                isError = state.wantedCode.isNotEmpty() && !AuthRules.validateShopCode(state.wantedCode),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            PinField(
                value = state.ownerPin,
                onChange = { onEvent(SignInEvent.OnOwnerPinChanged(it)) },
                ja = R.string.auth_rg_pin_ja,
                en = R.string.auth_rg_pin_en,
                isError = false,
                hint = stringResource(R.string.auth_rg_pin_hint_ja) + " · " + stringResource(R.string.auth_rg_pin_hint_en),
            )
            if (state.error != null) ErrorLine(state, onRetry = null)
            PrimaryButton(
                ja = R.string.auth_rg_submit_ja,
                en = R.string.auth_rg_submit_en,
                enabled = state.canRegister,
                loading = state.isLoading,
                onClick = { onEvent(SignInEvent.SubmitRegister) },
            )
        }
        TextButton(
            onClick = { onEvent(SignInEvent.ShowRegister(false)) },
            modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
        ) { BiLabel(R.string.auth_rg_back_ja, R.string.auth_rg_back_en) }
    }
}

@Composable
private fun FindForm(state: SignInUiState, onEvent: (SignInEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section {
            BiHeading(R.string.auth_fd_title_ja, R.string.auth_fd_title_en)
            BiHeading(R.string.auth_fd_note_ja, R.string.auth_fd_note_en, small = true)
            OutlinedTextField(
                value = state.shopName,
                onValueChange = { onEvent(SignInEvent.OnShopNameChanged(it)) },
                label = { FieldLabel(R.string.auth_rg_shop_ja, R.string.auth_rg_shop_en) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.ownerName,
                onValueChange = { onEvent(SignInEvent.OnOwnerNameChanged(it)) },
                label = { FieldLabel(R.string.auth_rg_name_ja, R.string.auth_rg_name_en) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            PinField(
                value = state.ownerPin,
                onChange = { onEvent(SignInEvent.OnOwnerPinChanged(it)) },
                ja = R.string.auth_si_pin_ja,
                en = R.string.auth_si_pin_en,
                isError = false,
            )
            if (state.error != null) ErrorLine(state, onRetry = null)
            PrimaryButton(
                ja = R.string.auth_si_submit_ja,
                en = R.string.auth_si_submit_en,
                enabled = state.canFind,
                loading = state.isLoading,
                onClick = { onEvent(SignInEvent.SubmitFind) },
            )
        }
        TextButton(
            onClick = { onEvent(SignInEvent.ShowFind(false)) },
            modifier = Modifier.align(Alignment.CenterHorizontally).heightIn(min = 48.dp),
        ) { BiLabel(R.string.auth_rg_back_ja, R.string.auth_rg_back_en) }
    }
}

@Composable
private fun ShopReady(code: String, onEvent: (SignInEvent) -> Unit) {
    Section {
        Box(
            Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp)) }
        BiHeading(R.string.auth_rg_done_ja, R.string.auth_rg_done_en)
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                FieldLabel(R.string.auth_si_code_ja, R.string.auth_si_code_en)
                Text(
                    text = code,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                val context = LocalContext.current
                val clipboard = LocalClipboard.current
                val scope = rememberCoroutineScope()
                Row {
                    IconButton(onClick = { scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Shop code", code))) } }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.auth_rg_copy_en))
                    }
                    IconButton(onClick = {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.auth_rg_share_text, code))
                        context.startActivity(Intent.createChooser(send, null))
                    }) { Icon(Icons.Default.Share, contentDescription = stringResource(R.string.auth_rg_share_en)) }
                }
            }
        }
        BiHeading(R.string.auth_rg_done_note_ja, R.string.auth_rg_done_note_en, small = true)
        PrimaryButton(R.string.auth_rg_start_ja, R.string.auth_rg_start_en, enabled = true, loading = false) {
            onEvent(SignInEvent.FinishRegister)
        }
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
    }
}

/** Japanese line, then the English one smaller and lighter beneath. */
@Composable
private fun BiHeading(@StringRes ja: Int, @StringRes en: Int, small: Boolean = false, center: Boolean = false) {
    Column(horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start) {
        Text(
            stringResource(ja),
            style = if (small) {
                MaterialTheme.typography.bodyLarge
            } else {
                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            },
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            stringResource(en),
            style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FieldLabel(@StringRes ja: Int, @StringRes en: Int) {
    Text(stringResource(ja) + "  " + stringResource(en))
}

@Composable
private fun PinField(
    value: String,
    onChange: (String) -> Unit,
    @StringRes ja: Int,
    @StringRes en: Int,
    isError: Boolean,
    hint: String? = null,
) {
    var shown by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { FieldLabel(ja, en) },
        supportingText = hint?.let { { Text(it) } },
        visualTransformation = if (shown) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { shown = !shown }) {
                Icon(
                    if (shown) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(if (shown) R.string.auth_si_pin_hide else R.string.auth_si_pin_show),
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        isError = isError,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PrimaryButton(
    @StringRes ja: Int,
    @StringRes en: Int,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            BiLabel(ja, en, onPrimary = true)
        }
    }
}

@Composable
private fun ErrorLine(state: SignInUiState, onRetry: (() -> Unit)?) {
    val kind = state.error ?: return
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                authErrorMessage(kind, state.errorArg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
            )
            if (onRetry != null && kind != AuthErrorKind.UNKNOWN_SHOP && kind != AuthErrorKind.WRONG_PIN) {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.auth_retry)) }
            }
        }
    }
}

@Composable
private fun RoleToggle(role: Role, onRoleChange: (Role) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
            .padding(4.dp),
    ) {
        listOf(
            Triple(Role.WORKER, R.string.auth_si_worker_ja, R.string.auth_si_worker_en),
            Triple(Role.FOREMAN, R.string.auth_si_foreman_ja, R.string.auth_si_foreman_en),
        ).forEach { (r, ja, en) ->
            val on = r == role
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .background(
                        if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(16.dp),
                    )
                    .clickable { onRoleChange(r) },
                contentAlignment = Alignment.Center,
            ) {
                val color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(stringResource(ja), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(en), style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun NameRow(name: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(16.dp),
            )
            .then(if (selected) Modifier.border(2.dp, primary, RoundedCornerShape(16.dp)) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(36.dp).background(if (selected) primary else MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.take(1),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) Icon(Icons.Default.Check, null, tint = primary)
    }
}

@Composable
private fun DemoAccounts(onPick: (DemoAccount) -> Unit) {
    // Demo accounts stay folded away so picking one is a deliberate tap, not an accident.
    var open by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { open = !open }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.auth_si_demo_ja),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.auth_si_demo_en),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Default.KeyboardArrowDown, null,
                modifier = Modifier.rotate(if (open) 180f else 0f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(open) {
            DemoAccountChips(onPick)
        }
        HorizontalDivider(Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun DemoAccountChips(onPick: (DemoAccount) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.auth_si_demo_hint_ja) + " · " + stringResource(R.string.auth_si_demo_hint_en),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DEMO_ACCOUNTS.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { a ->
                    val foreman = a.role == Role.FOREMAN
                    Column(
                        Modifier
                            .weight(1f)
                            .background(
                                if (foreman) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLowest,
                                RoundedCornerShape(20.dp),
                            )
                            .clickable { onPick(a) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        val fg = if (foreman) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        Text(
                            stringResource(if (foreman) R.string.auth_si_foreman_ja else R.string.auth_si_worker_ja) + " " +
                                stringResource(if (foreman) R.string.auth_si_foreman_en else R.string.auth_si_worker_en),
                            style = MaterialTheme.typography.labelSmall,
                            color = fg.copy(alpha = 0.75f),
                        )
                        Text(a.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = fg)
                    }
                }
            }
        }
    }
}
