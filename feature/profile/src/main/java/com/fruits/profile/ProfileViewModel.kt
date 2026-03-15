package com.fruits.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.image.BatchDownloadImagesUseCase
import com.fruits.domain.usecase.image.UploadImageUseCase
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val MAX_IMAGES = 5
private const val MIN_IMAGES = 1
private const val MAX_DESCRIPTION_LENGTH = 256

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val batchDownloadImagesUseCase: BatchDownloadImagesUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val application: Application,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProfileState(
            isLoading = true,
            error = null,
            user = null
        )
    )
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Refresh -> loadProfile()
            is ProfileEvent.OnDescriptionChanged -> {
                if (event.text.length <= MAX_DESCRIPTION_LENGTH) {
                    _state.update {
                        it.copy(
                            editedDescription = event.text,
                            hasChanges = hasChanges(it, event.text)
                        )
                    }
                    validateForm()
                }
            }
            is ProfileEvent.SaveChanges -> saveProfile()
            is ProfileEvent.DismissError -> _state.update { it.copy(error = null) }
            is ProfileEvent.PickImage -> startUpload(event.uri)
            is ProfileEvent.RemoveImage -> removeImage(event.imageId)
            is ProfileEvent.RetryUpload -> retryUpload(event.imageId)
            ProfileEvent.Logout -> logout()
        }
    }

    private fun hasChanges(state: ProfileState, newDescription: String): Boolean {
        val descriptionChanged = newDescription != (state.user?.description ?: "")
        val imagesChanged = state.pendingImages.isNotEmpty() ||
                state.displayedImages.size != state.serverPhotoKeys.size
        return descriptionChanged || imagesChanged
    }

    private fun validateForm() {
        val currentState = _state.value
        val totalImages = currentState.displayedImages.size + currentState.pendingImages.size
        val hasValidImages = totalImages in MIN_IMAGES..MAX_IMAGES
        val hasValidDescription = currentState.editedDescription.isNotBlank() &&
                currentState.editedDescription.length <= MAX_DESCRIPTION_LENGTH
        val noErrorsUploading = currentState.pendingImages.none { it.errorMessage != null }
        val noUploadingInProgress = currentState.pendingImages.none { it.isLoading }

        val isValid = hasValidImages && hasValidDescription && noErrorsUploading && noUploadingInProgress

        _state.update { it.copy(isFormValid = isValid) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                getProfileUseCase().collect { result ->
                    result.onSuccess { user ->
                        _state.update {
                            it.copy(
                                user = user,
                                editedDescription = user.description ?: "",
                                serverPhotoKeys = user.photoFileKeys,
                                isLoading = false,
                                error = null,
                                hasChanges = false
                            )
                        }

                        if (user.photoFileKeys.isNotEmpty()) {
                            downloadServerImages(user.photoFileKeys)
                        } else {
                            _state.update { it.copy(displayedImages = emptyList()) }
                        }
                        validateForm()
                    }

                    result.onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось загрузить профиль"
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = errorMsg,
                                user = null
                            )
                        }
                        _effect.send(ProfileEffect.ShowErrorSnackbar(errorMsg))
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    private fun downloadServerImages(keys: List<String>) {
        viewModelScope.launch {
            try {
                batchDownloadImagesUseCase(keys).collect { result ->
                    result.onSuccess { batch ->
                        val images = batch.results.mapNotNull { (key, res) ->
                            res.imageData?.let { bytes ->
                                try {
                                    val uri = ImageUtils.createTempImageUri(application, bytes, key)
                                    ProfileImage(id = key, uri = uri, isLoading = false, isPending = false)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                        _state.update { it.copy(displayedImages = images, isLoading = false) }
                        validateForm()
                    }

                    result.onFailure {
                        _state.update { it.copy(displayedImages = emptyList()) }
                        _effect.send(ProfileEffect.ShowErrorSnackbar("Не удалось загрузить фото"))
                        validateForm()
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(displayedImages = emptyList()) }
                validateForm()
            }
        }
    }

    private fun startUpload(uri: Uri) {
        val totalImages = _state.value.displayedImages.size + _state.value.pendingImages.size
        if (totalImages >= MAX_IMAGES) {
            _effect.trySend(ProfileEffect.ShowErrorSnackbar("Максимум $MAX_IMAGES фотографий"))
            return
        }

        val alreadyExists = _state.value.pendingImages.any { it.uri == uri } ||
                _state.value.displayedImages.any { it.uri == uri }
        if (alreadyExists) {
            return
        }

        val tempId = UUID.randomUUID().toString()
        val newPending = ProfileImage(id = tempId, uri = uri, isLoading = true, isPending = true, progress = 0f)

        _state.update {
            it.copy(
                pendingImages = it.pendingImages + newPending,
                hasChanges = true
            )
        }

        viewModelScope.launch {
            val inputStream = application.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                markUploadError(tempId, "Не удалось открыть файл")
                validateForm()
                return@launch
            }

            try {
                uploadImageUseCase(inputStream = inputStream, onProgress = { prog ->
                    _state.update { currentState ->
                        val updated = currentState.pendingImages.map { img ->
                            if (img.id == tempId) img.copy(progress = prog) else img
                        }
                        currentState.copy(pendingImages = updated)
                    }
                }).collect { result ->
                    result.onSuccess { serverKey ->
                        val completedImage = ProfileImage(
                            id = serverKey,
                            uri = uri,
                            isLoading = false,
                            isPending = false
                        )
                        _state.update { currentState ->
                            currentState.copy(
                                pendingImages = currentState.pendingImages.filter { it.id != tempId },
                                displayedImages = currentState.displayedImages + completedImage,
                                hasChanges = true
                            )
                        }
                        validateForm()
                    }

                    result.onFailure { e ->
                        markUploadError(tempId, e.message ?: "Ошибка загрузки")
                        validateForm()
                    }
                }
            } catch (e: Exception) {
                markUploadError(tempId, e.message ?: "Неизвестная ошибка")
                validateForm()
            } finally {
                inputStream.close()
            }
        }
    }

    private fun retryUpload(imageId: String) {
        val image = _state.value.pendingImages.find { it.id == imageId }
        if (image != null && image.uri != null) {
            _state.update {
                it.copy(pendingImages = it.pendingImages.filter { it.id != imageId })
            }
            startUpload(image.uri)
        }
    }

    private fun markUploadError(id: String, msg: String) {
        _state.update { currentState ->
            val updated = currentState.pendingImages.map { img ->
                if (img.id == id) img.copy(isLoading = false, errorMessage = msg) else img
            }
            currentState.copy(pendingImages = updated)
        }
    }

    private fun removeImage(id: String) {
        val isPending = _state.value.pendingImages.any { it.id == id }
        if (isPending) {
            _state.update {
                it.copy(
                    pendingImages = it.pendingImages.filter { it.id != id },
                    hasChanges = true
                )
            }
            validateForm()
            return
        }

        val isServer = _state.value.displayedImages.any { it.id == id }
        if (isServer) {
            _state.update {
                it.copy(
                    displayedImages = it.displayedImages.filter { it.id != id },
                    serverPhotoKeys = it.serverPhotoKeys.filter { it != id },
                    hasChanges = true
                )
            }
            validateForm()
        }
    }

    private fun saveProfile() {
        if (!_state.value.isFormValid) {
            _effect.trySend(ProfileEffect.ShowErrorSnackbar("Заполните все обязательные поля"))
            return
        }

        val currentUser = _state.value.user
        if (currentUser == null) {
            _state.update { it.copy(error = "Ошибка: данные пользователя не загружены") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val currentDisplayedKeys = _state.value.displayedImages.map { it.id }
                val newUploadedKeys = _state.value.pendingImages.filter { !it.isPending }.map { it.id }
                val finalKeys = currentDisplayedKeys + newUploadedKeys

                updateProfileUseCase(
                    description = _state.value.editedDescription,
                    photoFilesKeys = finalKeys
                )
                    .onSuccess { updatedUser ->
                        _state.update {
                            it.copy(
                                user = updatedUser,
                                serverPhotoKeys = updatedUser.photoFileKeys,
                                pendingImages = emptyList(),
                                displayedImages = emptyList(),
                                hasChanges = false,
                                isSaving = false,
                                isLoading = true,
                                isFormValid = false
                            )
                        }

                        _effect.send(ProfileEffect.ShowSuccessSnackbar)
                        loadProfile()
                    }

                    .onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось сохранить"
                        _state.update { it.copy(isSaving = false, error = errorMsg) }
                        _effect.send(ProfileEffect.ShowErrorSnackbar(errorMsg))
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Ошибка") }
            }
        }
    }

    private fun logout() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}