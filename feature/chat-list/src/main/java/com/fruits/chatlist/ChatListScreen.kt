package com.fruits.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.fruits.domain.model.chat.Chat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.fruits.logger.Log
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import kotlin.Long
import kotlin.String

@Composable
fun ChatListRoute(
    onNavigateToChatDetail: (String) -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(effect) {
        when (val e = effect) {
            is ChatListEffect.NavigateToChatDetail -> onNavigateToChatDetail(e.chatId)
            ChatListEffect.ShowErrorSnackbar -> {
                // Snackbar handling logic (e.g., via ScaffoldMessenger in parent)
            }

            null -> Unit
        }
    }

    ChatListScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    state: ChatListState,
    onEvent: (ChatListEvent) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // State is required when customizing the indicator
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Чаты",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("chat_list_title")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        containerColor = colorScheme.surface
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(ChatListEvent.Refresh) },
            state = pullRefreshState,
            modifier = Modifier
                .padding(paddingValues)
                .testTag("pull_to_refresh_box"),
        ) {
            // Content
            if (state.isLoading && state.chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        modifier = Modifier.testTag("chat_list_progress")
                    )
                }
            } else if (state.chats.isEmpty() && state.error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет активных чатов",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("chat_list_empty_message")
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chat_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    if (state.error != null && state.chats.isEmpty()) {
                        item {
                            ErrorCard(message = state.error)
                        }
                    }

                    items(state.chats, key = { it.id }) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onEvent(ChatListEvent.ChatClicked(chat.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val personIconPainter = rememberVectorPainter(Icons.Default.Person)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
            .testTag("chat_item_${chat.id}"),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (chat.avatarUrl != null) {
                AsyncImage(
                    model = chat.avatarUrl,
                    contentDescription = "Avatar of ${chat.name}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("chat_avatar_${chat.id}"),
                    contentScale = ContentScale.Crop,
                    placeholder = personIconPainter,
                    error = personIconPainter,
                    onError = { state: AsyncImagePainter.State.Error ->
                        Log.e(
                            "ChatListScreen",
                            "Avatar load FAILED: chatId=${chat.id}, url=${chat.avatarUrl}, error=${state.result.throwable}",
                            state.result.throwable
                        )
                    },
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer)
                        .testTag("chat_placeholder_${chat.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("chat_name_${chat.id}")
                    )
                    Text(
                        text = formatTime(chat.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_last_message_${chat.id}")
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onPrimary,
                                modifier = Modifier
                                    .testTag("chat_unread_count_${chat.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("chat_list_error_card"),
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return if (diff < 24 * 60 * 60 * 1000) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(timestamp))
    }
}
