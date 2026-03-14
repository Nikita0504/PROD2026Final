package com.fruits.debug_panel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.debugPanelScreen() {
    composable<Route.Debug> {
        DebugPanelScreen()
    }
}