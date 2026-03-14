package com.fruits.prod2026final.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.fruits.auth.authScreen
import com.fruits.navigation.Route

@Composable
fun AuthNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Auth,
    ) {
        authScreen()
    }
}
