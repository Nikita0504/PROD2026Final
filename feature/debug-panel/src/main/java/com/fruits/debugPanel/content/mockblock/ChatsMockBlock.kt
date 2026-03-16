package com.fruits.debugPanel.content.mockblock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fruits.debugPanel.ChatMockEditState
import com.fruits.debugPanel.content.MockEditBlock

@Composable
fun ChatsMockBlock(
    states: List<ChatMockEditState>,
    enabled: Boolean,
    onStateChange: (ChatMockEditState) -> Unit,
    onSave: () -> Unit,
) {
    var saved by remember { mutableStateOf(false) }

    MockEditBlock(
        title = "Chats (${states.size})",
        enabled = enabled,
        saved = saved,
        onSave = { onSave(); saved = true },
    ) {
        states.forEachIndexed { index, state ->
            ChatItemBlock(
                index = index + 1,
                state = state,
                enabled = enabled,
                onStateChange = {
                    onStateChange(it)
                    saved = false
                },
            )
            if (index < states.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
            }
        }
    }
}

@Composable
private fun ChatItemBlock(
    index: Int,
    state: ChatMockEditState,
    enabled: Boolean,
    onStateChange: (ChatMockEditState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "№$index  ${state.name}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = { onStateChange(state.copy(name = it)) },
                label = { Text("name") },
                modifier = Modifier.weight(1f),
                enabled = enabled,
                singleLine = true,
            )
            OutlinedTextField(
                value = state.unreadCount,
                onValueChange = { onStateChange(state.copy(unreadCount = it)) },
                label = { Text("unread") },
                modifier = Modifier.width(80.dp),
                enabled = enabled,
                singleLine = true,
            )
        }

        OutlinedTextField(
            value = state.lastMessage,
            onValueChange = { onStateChange(state.copy(lastMessage = it)) },
            label = { Text("lastMessage") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
        )
    }
}
