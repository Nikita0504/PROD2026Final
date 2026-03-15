package com.fruits.chat

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.chatScreen(onBack: () -> Unit) {
    composable<Route.Chat> {
        ChatRoute(onBack = onBack)
    }
}