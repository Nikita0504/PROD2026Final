package com.fruits.prod2026final.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

@Composable
fun TapeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Tape screen")
    }
}

@Composable
fun ChatScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Chat screen")
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Profile screen")
    }
}

fun NavGraphBuilder.tapeScreen() {
    composable<Route.Tape> {
        TapeScreen()
    }
}

fun NavGraphBuilder.chatScreen() {
    composable<Route.Chat> {
        ChatScreen()
    }
}

fun NavGraphBuilder.profileScreen() {
    composable<Route.Profile> {
        ProfileScreen()
    }
}

