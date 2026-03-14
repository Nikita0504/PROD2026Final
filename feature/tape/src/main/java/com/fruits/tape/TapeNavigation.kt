package com.fruits.tape

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fruits.navigation.Route

fun NavGraphBuilder.tapeScreen() {
    composable<Route.Tape> {
        TapeRoute()
    }
}