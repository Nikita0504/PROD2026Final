package com.fruits.debugPanel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.debugPanelScreen(onClose: (() -> Unit)? = null) {
    composable<Route.Debug> {
        DebugPanelScreen(onClose = onClose)
    }
}