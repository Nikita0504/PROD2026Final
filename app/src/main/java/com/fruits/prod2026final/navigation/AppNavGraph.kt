package com.fruits.prod2026final.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.fruits.auth.authScreen
import com.fruits.navigation.Route
import com.fruits.register.registerScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Auth,
    ) {
        authScreen(
            onNavigateToRegister = {
                navController.navigate(Route.Register)
            },
        )

        registerScreen(
            onNavigateBackToAuth = {
                navController.popBackStack()
            },
        )
    }
}

