package com.fruits.debugPanel.extension

import com.fruits.debugPanel.ChatMockEditState
import com.fruits.domain.model.chat.Chat

fun Chat.toEditState() = ChatMockEditState(
    id = id,
    name = name,
    lastMessage = lastMessage,
    unreadCount = unreadCount.toString(),
)

fun List<Chat>.toEditState() = map { it.toEditState() }
