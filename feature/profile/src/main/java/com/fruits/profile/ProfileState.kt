package com.fruits.profile

import android.net.Uri
import com.fruits.domain.model.user.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val editedDescription: String = "",
    val hasChanges: Boolean = false,

    val serverPhotoKeys: List<String> = emptyList(),

    val displayedImages: List<ProfileImage> = emptyList(),

    val pendingImages: List<ProfileImage> = emptyList(),

    val isFormValid: Boolean = false
)

data class ProfileImage(
    val id: String,
    val uri: Uri?,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val isPending: Boolean = true
)


sealed interface ProfileEvent {
    data object Refresh : ProfileEvent
    data class OnDescriptionChanged(val text: String) : ProfileEvent
    data object SaveChanges : ProfileEvent
    data object DismissError : ProfileEvent

    data class PickImage(val uri: Uri) : ProfileEvent
    data class RemoveImage(val imageId: String) : ProfileEvent
    data class RetryUpload(val imageId: String) : ProfileEvent

    data object Logout : ProfileEvent
}

sealed interface ProfileEffect {
    data object ShowSuccessSnackbar : ProfileEffect

    data class ShowErrorSnackbar(val message: String) : ProfileEffect
}

