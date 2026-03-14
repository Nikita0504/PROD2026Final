package com.fruits.profile

sealed interface ProfileEffect {
    data object ShowPreviewDialog : ProfileEffect
    data object NavigateToSettings : ProfileEffect
}