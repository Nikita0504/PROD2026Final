package com.fruits.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Auth : Route

    @Serializable
    data object Register : Route
}

