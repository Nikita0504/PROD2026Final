package com.fruits.audit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.auditScreen(
    onShowSnackbar: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable<Route.Audit> {
        AuditRoute(
            onShowSnackbar = onShowSnackbar,
            onNavigateBack = onNavigateBack,
        )
    }
}
