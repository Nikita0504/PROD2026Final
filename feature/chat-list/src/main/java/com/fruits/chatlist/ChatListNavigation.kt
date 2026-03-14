package com.fruits.chatlist

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.chatListScreen(
    onNavigateToChatDetail: (String) -> Unit,
) {
    composable<Route.ChatList> {
        ChatListRoute(
            onNavigateToChatDetail = onNavigateToChatDetail
        )
    }
}