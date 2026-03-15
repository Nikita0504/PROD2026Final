package com.fruits.onboarding

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.image.UploadImageUseCase
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

private const val MAX_IMAGES = 5
private const val MIN_IMAGE_SIZE = 256
private const val MAX_IMAGE_SIZE = 2048

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
        validateForm()
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OnDescriptionChanged -> {
                if (event.text.length <= 256) {
                    _state.update { it.copy(description = event.text) }
                    validateForm()
                }
            }
            is OnboardingEvent.PickImage -> validateAndStartUpload(event.uri)
            is OnboardingEvent.RemoveImage -> removeImage(event.uri)
            is OnboardingEvent.RetryUpload -> validateAndStartUpload(event.uri)
            is OnboardingEvent.SubmitForm -> submitForm()
        }
    }

    fun goBack() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }

    private fun validateAndStartUpload(uri: Uri) {
        val totalImages = _state.value.selectedImages.size + _state.value.uploadingImages.size
        if (totalImages >= MAX_IMAGES) {
            _effect.trySend(OnboardingEffect.ShowError("Максимум $MAX_IMAGES фотографий"))
            return
        }

        if (_state.value.selectedImages.any { it.uri == uri } ||
            _state.value.uploadingImages.any { it.uri == uri }) {
            return
        }

        viewModelScope.launch {
            val dimensions = getImageDimensions(uri)

            when {
                dimensions.first < MIN_IMAGE_SIZE || dimensions.second < MIN_IMAGE_SIZE -> {
                    _effect.trySend(OnboardingEffect.ShowError("Минимальный размер фото ${MIN_IMAGE_SIZE}x${MIN_IMAGE_SIZE}"))
                    return@launch
                }
                dimensions.first > MAX_IMAGE_SIZE || dimensions.second > MAX_IMAGE_SIZE -> {
                    _effect.trySend(OnboardingEffect.ShowError("Максимальный размер фото ${MAX_IMAGE_SIZE}x${MAX_IMAGE_SIZE}"))
                    return@launch
                }
                else -> startUpload(uri, dimensions)
            }
        }
    }

    private fun startUpload(uri: Uri, dimensions: Pair<Int, Int>) {
        _state.update {
            it.copy(
                uploadingImages = it.uploadingImages + UploadingImage(uri = uri, progress = 0f)
            )
        }

        viewModelScope.launch {
            var inputStream: java.io.InputStream? = null
            try {
                inputStream = application.contentResolver.openInputStream(uri)
                    ?: throw IOException("Failed to open input stream for URI: $uri")

                uploadImageUseCase(inputStream = inputStream, onProgress = { progress ->
                    _state.update { currentState ->
                        val updatedList = currentState.uploadingImages.map { img ->
                            if (img.uri == uri) img.copy(progress = progress) else img
                        }
                        currentState.copy(uploadingImages = updatedList)
                    }
                }).collect { result ->
                    result.onSuccess { serverId ->
                        val completedImage = OnboardingImage(
                            uri = uri,
                            serverId = serverId,
                            width = dimensions.first,
                            height = dimensions.second
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
                        markUploadError(uri, error)
                        validateForm()
                    }
                }
            } catch (e: Exception) {
                markUploadError(uri, e)
                validateForm()
            } finally {
                inputStream?.close()
            }
        }
    }

    private fun markUploadError(uri: Uri, error: Throwable) {
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
    }

    private fun removeImage(uri: Uri) {
        _state.update {
            it.copy(
                selectedImages = it.selectedImages.filter { img -> img.uri != uri },
                uploadingImages = it.uploadingImages.filter { img -> img.uri != uri }
            )
        }
        validateForm()
    }

    private fun submitForm() {
        if (!state.value.isFormValid) {
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val imageIds = _state.value.selectedImages.map { it.serverId }
                val description = _state.value.description

                val result = sessionManager.onboard(
                    description = description,
                    imageIds = imageIds
                )

                if (result.isFailure) {
                    val error = result.exceptionOrNull()?.message ?: "Не удалось завершить профиль"
                    _state.update { it.copy(isLoading = false, error = error) }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
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
        val isValid = currentState.selectedImages.isNotEmpty() &&
                currentState.selectedImages.size <= MAX_IMAGES &&
                currentState.description.isNotBlank() &&
                currentState.description.length <= 256 &&
                currentState.uploadingImages.none { it.errorMessage != null } &&
                currentState.uploadingImages.isEmpty()

        _state.update { it.copy(isFormValid = isValid) }
    }

    private fun getImageDimensions(uri: Uri): Pair<Int, Int> {
        return try {
            application.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                Pair(options.outWidth, options.outHeight)
            } ?: Pair(0, 0)
        } catch (e: Exception) {
            Pair(0, 0)
        }
    }
}