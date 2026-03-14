package com.fruits.onboarding

import android.net.Uri

data class OnboardingState(
    val description: String = "",
    val selectedImages: List<OnboardingImage> = emptyList(),
    val uploadingImages: List<UploadingImage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false
)

data class OnboardingImage(
    val uri: Uri,
    val serverId: String,
    val width: Int,
    val height: Int
)

data class UploadingImage(
    val uri: Uri,
    val progress: Float = 0f,
    val errorMessage: String? = null
)

sealed interface OnboardingEvent {
    data class OnDescriptionChanged(val text: String) : OnboardingEvent
    data class PickImage(val uri: Uri) : OnboardingEvent
    data class RemoveImage(val uri: Uri) : OnboardingEvent
    data class RetryUpload(val uri: Uri) : OnboardingEvent
    data object SubmitForm : OnboardingEvent
}

sealed interface OnboardingEffect {
    data class ShowError(val message: String) : OnboardingEffect
}