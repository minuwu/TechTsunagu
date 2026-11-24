package com.techtsunagu.wakatta.feature.asks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.techtsunagu.wakatta.core.design.component.WakattaChip
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.media.rememberDictation
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.pick
import com.techtsunagu.wakatta.feature.asks.model.KnowledgeReplyKind

/**
 * "Ask Wakatta" (XREQ-016, Android fix plan M7): answers only from the shop's instructions and the official guides,
 * with the page, then one practice question graded on the phone. From a Machine screen, [machineId] makes that
 * Machine's instruction rank first.
 */
@Composable
fun ChatbotScreen(
    machineId: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: ChatbotViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(machineId) { viewModel.onEvent(ChatbotEvent.SetMachine(machineId)) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChatbotEffect.OpenUrl -> runCatching { uriHandler.openUri(effect.url) }
            }
        }
    }
    ChatbotContent(state = state, onEvent = viewModel::onEvent, onBackClick = onBackClick)
}

@Composable
fun ChatbotContent(
    state: ChatbotUiState,
    onEvent: (ChatbotEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.turns.size, state.sending) {
        if (state.turns.isNotEmpty()) listState.animateScrollToItem(state.turns.size)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { WakattaTopBar(title = stringResource(R.string.asks_bot_title), onBackClick = onBackClick) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "intro") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.asks_bot_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
                items(state.turns, key = { it.id }) { turn ->
                    ChatbotTurnItem(turn = turn, language = state.language, onEvent = onEvent)
                }
                if (state.sending) {
                    item(key = "thinking") {
                        Text(
                            text = stringResource(R.string.asks_bot_thinking),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            ChatbotInput(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun ChatbotTurnItem(turn: ChatbotTurn, language: String, onEvent: (ChatbotEvent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.widthIn(max = 300.dp),
            ) {
                Text(turn.reply.question, modifier = Modifier.padding(12.dp))
            }
        }
        when (turn.kind) {
            KnowledgeReplyKind.EMERGENCY -> EmergencyCard(pick(turn.reply.answer, language))
            KnowledgeReplyKind.COVERED -> CoveredCard(turn, language, onEvent)
            KnowledgeReplyKind.NOT_COVERED -> NotCoveredCard(turn, onEvent)
        }
    }
}

@Composable
private fun EmergencyCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.asks_bot_emergency_title),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.semantics { heading() },
            )
            Text(text, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun CoveredCard(turn: ChatbotTurn, language: String, onEvent: (ChatbotEvent) -> Unit) {
    val reply = turn.reply
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(pick(reply.answer, language))
            if (reply.sources.isNotEmpty()) {
                Text(stringResource(R.string.asks_bot_sources), style = MaterialTheme.typography.labelMedium)
                reply.sources.forEach { source ->
                    val label = source.page?.let { "${source.title} · ${stringResource(R.string.asks_bot_page, it)}" }
                        ?: source.title
                    WakattaChip(
                        text = label,
                        selected = false,
                        onClick = { source.url?.let { onEvent(ChatbotEvent.OpenSource(it)) } },
                    )
                }
                reply.sources.mapNotNull { it.attribution }.distinct().forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            reply.check?.let { check ->
                Text(
                    text = stringResource(R.string.asks_bot_check),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(pick(check.prompt, language))
                check.options.forEachIndexed { i, option ->
                    val picked = turn.picked
                    val colors = when {
                        picked == null -> ButtonDefaults.outlinedButtonColors()
                        i == check.correctIndex -> ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                        i == picked -> ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        )
                        else -> ButtonDefaults.outlinedButtonColors()
                    }
                    OutlinedButton(
                        onClick = { onEvent(ChatbotEvent.PickOption(turn.id, i)) },
                        enabled = picked == null,
                        colors = colors,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(pick(option, language)) }
                }
                turn.pickedRight?.let { right ->
                    Text(
                        text = if (right) {
                            stringResource(R.string.asks_bot_right)
                        } else {
                            stringResource(R.string.asks_bot_wrong, pick(check.options[check.correctIndex], language))
                        },
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotCoveredCard(turn: ChatbotTurn, onEvent: (ChatbotEvent) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.asks_bot_not_covered))
            if (turn.forwarded) {
                Text(stringResource(R.string.asks_bot_sent_senpai), fontWeight = FontWeight.Bold)
            } else {
                Button(onClick = { onEvent(ChatbotEvent.AskSenpai(turn.id)) }, enabled = !turn.forwarding) {
                    Text(stringResource(R.string.asks_bot_ask_senpai))
                }
            }
        }
    }
}

@Composable
private fun ChatbotInput(state: ChatbotUiState, onEvent: (ChatbotEvent) -> Unit) {
    val dictation = rememberDictation(state.language, state.input) { onEvent(ChatbotEvent.InputChanged(it)) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        val suggestions = listOf(
            stringResource(R.string.asks_sug_garbage),
            stringResource(R.string.asks_sug_drill),
            stringResource(R.string.asks_sug_bus),
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { sug ->
                WakattaChip(
                    text = sug,
                    selected = false,
                    onClick = {
                        if (!state.sending) {
                            onEvent(ChatbotEvent.InputChanged(sug))
                            onEvent(ChatbotEvent.Send)
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        state.error?.let { error ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(errorText(error)),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(ChatbotEvent.DismissError) }) { Text(stringResource(R.string.asks_bot_ok)) }
            }
        }
        dictation.errorRes?.let {
            Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.input,
                onValueChange = { onEvent(ChatbotEvent.InputChanged(it)) },
                placeholder = { Text(stringResource(R.string.asks_bot_placeholder)) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = dictation.toggle,
                modifier = Modifier.size(48.dp).background(
                    color = if (dictation.isListening) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = CircleShape,
                ),
            ) {
                Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.asks_bot_mic))
            }
            IconButton(
                onClick = { onEvent(ChatbotEvent.Send) },
                enabled = state.canSend,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.asks_bot_send))
            }
        }
    }
}

/** A 429 from the per-user AI allowance says when to try again (Retry-After over 2 minutes: today's limit). */
private fun errorText(error: AppError): Int = when {
    error is AppError.RateLimited && (error.retryAfter ?: 0) > 120 -> R.string.asks_bot_limit
    error is AppError.RateLimited || error is AppError.Busy -> R.string.asks_bot_busy
    else -> R.string.asks_bot_error
}
