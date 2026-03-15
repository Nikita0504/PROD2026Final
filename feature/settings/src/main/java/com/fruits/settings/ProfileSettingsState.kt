package com.fruits.settings

import android.net.Uri
import com.fruits.domain.model.user.User

data class ProfileSettingsState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val editedDescription: String = "",
    val hasChanges: Boolean = false,
    val serverPhotoKeys: List<String> = emptyList(),
    val displayedImages: List<ProfileImage> = emptyList(),
    val pendingImages: List<ProfileImage> = emptyList(),
    val isFormValid: Boolean = false,
)

data class ProfileImage(
    val id: String,
    val uri: Uri?,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val isPending: Boolean = true,
)

sealed interface ProfileSettingsEvent {
    data object SaveChanges : ProfileSettingsEvent
    data object DismissError : ProfileSettingsEvent
    data class OnDescriptionChanged(val text: String) : ProfileSettingsEvent
    data class PickImage(val uri: Uri) : ProfileSettingsEvent
    data class RemoveImage(val imageId: String) : ProfileSettingsEvent
    data class RetryUpload(val imageId: String) : ProfileSettingsEvent
}

sealed interface ProfileSettingsEffect {
    data class ShowErrorSnackbar(val message: String) : ProfileSettingsEffect
    data object ShowSuccessSnackbar : ProfileSettingsEffect
    data object NavigateBack : ProfileSettingsEffect
}

