package com.fruits.profile

sealed interface ProfileEvent {
    data object OnSettingsClicked : ProfileEvent
    data object OnPreviewClicked : ProfileEvent

    data object OnLogoutClicked : ProfileEvent
}

