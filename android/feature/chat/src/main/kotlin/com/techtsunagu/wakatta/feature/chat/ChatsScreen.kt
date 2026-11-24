package com.techtsunagu.wakatta.feature.chat

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.techtsunagu.wakatta.core.design.component.RefreshOnReturn
import com.techtsunagu.wakatta.core.design.component.Avatar
import com.techtsunagu.wakatta.core.design.component.EmptyState
import com.techtsunagu.wakatta.core.design.component.ErrorState
import com.techtsunagu.wakatta.core.design.component.LoadingState
import com.techtsunagu.wakatta.core.design.component.TriText
import com.techtsunagu.wakatta.core.design.component.WakattaTopBar
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.pick
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.IconButton
import com.techtsunagu.wakatta.feature.chat.model.ThreadFilter
import com.techtsunagu.wakatta.feature.chat.model.ThreadUiModel

@Composable
fun ChatsScreen(
    onThreadClick: (String) -> Unit = {},
    onQrClick: () -> Unit = {},
    onNewTalkClick: () -> Unit = {},
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RefreshOnReturn { viewModel.onEvent(ChatsEvent.Init) }
    ChatsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onThreadClick = onThreadClick,
        onQrClick = onQrClick,
        onNewTalkClick = onNewTalkClick,
    )
}

@Composable
fun ChatsContent(
    state: ChatsUiState,
    onEvent: (ChatsEvent) -> Unit,
    modifier: Modifier = Modifier,
    onThreadClick: (String) -> Unit = {},
    onQrClick: () -> Unit = {},
    onNewTalkClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            WakattaTopBar(
                title = stringResource(R.string.chat_chats_screen_title),
                actions = {
                    // prototype: the QR in the Chats bar opens Connect (my code + scan)
                    IconButton(onClick = onQrClick) {
                        Icon(Icons.Default.QrCode2, contentDescription = stringResource(R.string.chat_qr_connect))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.filter == ThreadFilter.ALL,
                    onClick = { onEvent(ChatsEvent.SetFilter(ThreadFilter.ALL)) },
                    label = { Text(text = stringResource(R.string.chat_filter_all)) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                FilterChip(
                    selected = state.filter == ThreadFilter.UNREAD,
                    onClick = { onEvent(ChatsEvent.SetFilter(ThreadFilter.UNREAD)) },
                    label = { Text(text = stringResource(R.string.chat_filter_unread)) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                // action, not a filter: opens the Connect talk threads where "New talk" lives
                FilterChip(
                    selected = false,
                    onClick = onNewTalkClick,
                    label = { Text(text = stringResource(R.string.chat_new_talk)) },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                )
            }

            when {
                state.isLoading -> {
                    LoadingState(message = stringResource(R.string.chat_loading))
                }
                state.error != null -> {
                    ErrorState(
                        message = state.error,
                        onRetry = { onEvent(ChatsEvent.Retry) },
                    )
                }
                state.isEmpty -> {
                    EmptyState(
                        title = stringResource(R.string.chat_chats_screen_title),
                        message = stringResource(R.string.chat_empty_threads),
                        actionLabel = stringResource(R.string.chat_retry),
                        onActionClick = { onEvent(ChatsEvent.Refresh) },
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                    ) {
                        items(
                            items = state.threads,
                            key = { it.id },
                            contentType = { "thread" },
                        ) { thread ->
                            ThreadListItem(
                                thread = thread,
                                onClick = { onThreadClick(thread.id) },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreadListItem(
    thread: ThreadUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        if (thread.isGroup && thread.membersCount > 2) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = stringResource(R.string.chat_chats_screen_title),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(26.dp),
                )
            }
        } else {
            val titleText = pick(thread.title, "ja")
            Avatar(
                name = titleText,
                size = 48.dp,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Center content: Title & last message preview
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            TriText(
                tri = thread.title,
                lang = "ja",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 16.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (thread.isVoice) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.chat_voice_message),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                val you = if (thread.lastFromMe && thread.lastMessage.isNotEmpty()) stringResource(R.string.chat_you_prefix) else ""
                Text(
                    text = you + thread.lastMessage.ifEmpty { stringResource(R.string.chat_no_messages_yet) },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right side: Timestamp, status dot & unread badge
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = thread.lastMessageAt,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (thread.worstStatus != null) {
                    val statusColor = StatusColors.forStatus(thread.worstStatus)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor.color, CircleShape),
                    )
                }

                if (thread.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            text = thread.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                            ),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
