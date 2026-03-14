package com.fruits.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Auth : Route
    @Serializable
    data object Onboarding : Route
    @Serializable
    data object Settings : Route
    @Serializable
    data object Tape : Route
    @Serializable
    data object Chat : Route
    @Serializable
    data object Profile : Route
    @Serializable
    data object ChatList : Route
}

