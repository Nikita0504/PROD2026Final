package com.fruits.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.authScreen() {
    composable<Route.Auth> {
        AuthRoute()
    }
}

