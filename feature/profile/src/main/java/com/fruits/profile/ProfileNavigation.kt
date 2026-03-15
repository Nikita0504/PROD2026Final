package com.fruits.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.profileScreen(
    onShowSnackbar: (String) -> Unit,
    onNavigateToProfileSettings: () -> Unit,
) {
    composable<Route.Profile> {
        ProfileRoute(
            onShowSnackbar = onShowSnackbar,
            onEditClick = onNavigateToProfileSettings,
        )
    }
}