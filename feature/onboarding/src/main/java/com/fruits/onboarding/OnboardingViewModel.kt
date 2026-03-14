package com.fruits.onboarding

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.uploading.UploadImageUseCase
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "OnboardingVM"

class OnboardingViewModel(
    private val uploadImageUseCase: UploadImageUseCase,
    private val sessionManager: SessionManager,
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effect = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        Log.d(TAG, "ViewModel initialized")
        validateForm()
    }

    fun onEvent(event: OnboardingEvent) {
        Log.d(TAG, "Event received: $event")
        when (event) {
            is OnboardingEvent.OnDescriptionChanged -> {
                if (event.text.length <= 256) {
                    _state.update { it.copy(description = event.text) }
                    validateForm()
                }
            }

            is OnboardingEvent.PickImage -> {
                validateAndStartUpload(event.uri)
            }

            is OnboardingEvent.RemoveImage -> {
                _state.update {
                    it.copy(
                        selectedImages = it.selectedImages.filter { img -> img.uri != event.uri },
                        uploadingImages = it.uploadingImages.filter { img -> img.uri != event.uri }
                    )
                }
                validateForm()
            }

            is OnboardingEvent.RetryUpload -> {
                validateAndStartUpload(event.uri)
            }

            is OnboardingEvent.SubmitForm -> {
                submitForm()
            }
        }
    }

    fun goBack() {
        Log.d(TAG, "Navigating back, logging out")
        viewModelScope.launch {
            sessionManager.logout()
        }
    }

    private fun validateAndStartUpload(uri: Uri) {
        Log.d(TAG, "Validating image: $uri")

        if (_state.value.selectedImages.any { it.uri == uri } ||
            _state.value.uploadingImages.any { it.uri == uri }) {
            Log.w(TAG, "Image already exists in list, ignoring")
            return
        }

        viewModelScope.launch {
            val dims = getImageDimensions(uri)
            Log.d(TAG, "Image dimensions: ${dims.first}x${dims.second}")

            if (dims.first < 256 || dims.second < 256) {
                Log.e(TAG, "Image too small")
                _effect.send(OnboardingEffect.ShowError("Минимальный размер фото 256x256"))
                return@launch
            }

            if (dims.first > 2048 || dims.second > 2048) {
                Log.e(TAG, "Image too large")
                _effect.send(OnboardingEffect.ShowError("Максимальный размер фото 2048x2048"))
                return@launch
            }

            startUpload(uri)
        }
    }

    private fun startUpload(uri: Uri) {
        Log.d(TAG, "Starting upload process for: $uri")

        _state.update {
            it.copy(
                uploadingImages = it.uploadingImages + UploadingImage(uri = uri, progress = 0f)
            )
        }

        viewModelScope.launch {
            var inputStream = null as java.io.InputStream?
            try {
                inputStream = application.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    throw IOException("Failed to open input stream for URI: $uri")
                }

                Log.d(TAG, "Input stream opened. Invoking UploadImageUseCase...")

                uploadImageUseCase(inputStream = inputStream, onProgress = { progress ->
                    Log.d(TAG, "Upload progress callback: $progress")
                    _state.update { currentState ->
                        val updatedList = currentState.uploadingImages.map { img ->
                            if (img.uri == uri) img.copy(progress = progress) else img
                        }
                        currentState.copy(uploadingImages = updatedList)
                    }
                }).collect { result ->
                    result.onSuccess { serverId ->
                        Log.d(TAG, "Upload successful. Server ID: $serverId")

                        val dims = getImageDimensions(uri)
                        val completedImage = OnboardingImage(
                            uri = uri,
                            serverId = serverId,
                            width = dims.first,
                            height = dims.second
                        )

                        _state.update { currentState ->
                            currentState.copy(
                                selectedImages = currentState.selectedImages + completedImage,
                                uploadingImages = currentState.uploadingImages.filter { it.uri != uri }
                            )
                        }
                        validateForm()
                    }

                    result.onFailure { error ->
                        Log.e(TAG, "Upload failed via UseCase", error)
                        val errorMsg = when (error) {
                            is IOException -> "Ошибка сети: ${error.message}"
                            else -> "Ошибка загрузки: ${error.message ?: "Неизвестная ошибка"}"
                        }

                        _state.update { currentState ->
                            val updatedList = currentState.uploadingImages.map { img ->
                                if (img.uri == uri) img.copy(errorMessage = errorMsg) else img
                            }
                            currentState.copy(uploadingImages = updatedList)
                        }
                        validateForm()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during upload setup", e)
                val errorMsg = e.message ?: "Неизвестная ошибка"
                _state.update { currentState ->
                    val updatedList = currentState.uploadingImages.map { img ->
                        if (img.uri == uri) img.copy(errorMessage = errorMsg) else img
                    }
                    currentState.copy(uploadingImages = updatedList)
                }
                validateForm()
            } finally {
                inputStream?.close()
                Log.d(TAG, "Input stream closed for: $uri")
            }
        }
    }

    private fun submitForm() {
        Log.d(TAG, "Submitting form")
        if (!state.value.isFormValid) {
            Log.w(TAG, "Form not valid, cannot submit")
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val imageIds = _state.value.selectedImages.map { it.serverId }
                val description = _state.value.description

                Log.d(TAG, "Calling sessionManager.onboard with ${imageIds.size} images")

                val result = sessionManager.onboard(
                    description = description,
                    imageIds = imageIds
                )

                if (result.isSuccess) {
                    Log.d(TAG, "Onboarding successful. Session state updated.")
                    _state.update { it.copy(isLoading = false) }
                    // Effect could be added here to trigger navigation if needed,
                    // but SessionManager state change usually drives navigation in App level
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Не удалось завершить профиль"
                    Log.e(TAG, "Onboarding failed: $error")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during form submission", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось сохранить профиль"
                    )
                }
            }
        }
    }

    private fun validateForm() {
        val currentState = _state.value
        val hasValidImages = currentState.selectedImages.isNotEmpty()
        val hasValidDescription = currentState.description.isNotBlank() && currentState.description.length <= 256
        val noErrorsUploading = currentState.uploadingImages.none { it.errorMessage != null }
        val noUploadingInProgress = currentState.uploadingImages.isEmpty()

        val isValid = hasValidImages && hasValidDescription && noErrorsUploading && noUploadingInProgress

        Log.d(TAG, "Form validation: images=$hasValidImages, desc=$hasValidDescription, errors=$noErrorsUploading, uploading=$noUploadingInProgress -> valid=$isValid")

        _state.update {
            it.copy(isFormValid = isValid)
        }
    }

    private fun getImageDimensions(uri: Uri): Pair<Int, Int> {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            inputStream?.let {
                BitmapFactory.decodeStream(it, null, options)
                it.close()
            }
            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get image dimensions", e)
            Pair(0, 0)
        }
    }
}