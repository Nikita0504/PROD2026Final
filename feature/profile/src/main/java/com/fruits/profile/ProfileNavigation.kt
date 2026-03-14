package com.fruits.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.profileScreen(
    onNavigateToSettings: () -> Unit = {},
) {
    composable<Route.Profile> {
        ProfileRoute(onNavigateToSettings = onNavigateToSettings)
    }
}