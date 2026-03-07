package com.fruits.register

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.fruits.navigation.Route

fun NavGraphBuilder.registerScreen(
    onNavigateBackToAuth: () -> Unit,
) {
    composable<Route.Register> {
        RegisterRoute(
            onNavigateBackToAuth = onNavigateBackToAuth,
        )
    }
}

