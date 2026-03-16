package com.fruits.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.domain.usecase.auth.UpdateProfileUseCase
import com.fruits.domain.usecase.image.BatchDownloadImagesUseCase
import com.fruits.domain.usecase.image.UploadImageUseCase
import com.fruits.logger.Log
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

class ProfileSettingsViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val batchDownloadImagesUseCase: BatchDownloadImagesUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProfileSettingsState(
            isLoading = true,
            isImagesLoading = true,
            error = null,
        )
    )
    val state: StateFlow<ProfileSettingsState> = _state.asStateFlow()

    private val _effect = Channel<ProfileSettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileSettingsEvent) {
        when (event) {
            is ProfileSettingsEvent.OnDescriptionChanged -> {
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
            is ProfileSettingsEvent.SaveChanges -> saveProfile()
            is ProfileSettingsEvent.DismissError -> _state.update { it.copy(error = null) }
            is ProfileSettingsEvent.PickImage -> startUpload(event.uri)
            is ProfileSettingsEvent.RemoveImage -> removeImage(event.imageId)
            is ProfileSettingsEvent.RetryUpload -> retryUpload(event.imageId)
        }
    }

    private fun hasChanges(state: ProfileSettingsState, newDescription: String): Boolean {
        val initialDescription = state.user?.description ?: ""
        val descriptionChanged = newDescription != initialDescription

        // Проверяем изменения в картинках:
        // 1. Есть новые (не серверные)
        // 2. Есть удаленные (помеченные флагом)
        // 3. Количество активных картинок отличается от исходного
        val activeImages = state.images.filter { !it.isDeleted }
        val newImagesCount = activeImages.count { !it.isServerImage && it.errorMessage == null }
        val deletedServerImagesCount = state.images.count { it.isServerImage && it.isDeleted }

        val imagesChanged = newImagesCount > 0 || deletedServerImagesCount > 0

        return descriptionChanged || imagesChanged
    }

    private fun validateForm() {
        val currentState = _state.value
        val activeImages = currentState.images.filter { !it.isDeleted && it.errorMessage == null }
        val totalImages = activeImages.size

        // Валидация:
        // 1. Кол-во фото в диапазоне
        // 2. Описание не пустое
        // 3. Нет ошибок загрузки/аплоада
        // 4. Нет процессов загрузки прямо сейчас
        val hasValidImages = totalImages in MIN_IMAGES..MAX_IMAGES
        val hasValidDescription = currentState.editedDescription.isNotBlank() &&
                currentState.editedDescription.length <= MAX_DESCRIPTION_LENGTH
        val noErrors = activeImages.none { it.errorMessage != null }
        val noLoading = activeImages.none { it.isLoading }

        val isValid = hasValidImages && hasValidDescription && noErrors && noLoading

        _state.update { it.copy(isFormValid = isValid) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isImagesLoading = true, error = null) }

            try {
                getProfileUseCase().collect { result ->
                    result.onSuccess { user ->
                        // 1. Обновляем данные юзера
                        _state.update {
                            it.copy(
                                user = user,
                                editedDescription = user.description ?: "",
                                isLoading = false,
                                error = null
                                // Картинки пока старые или пустые, ждем загрузки
                            )
                        }

                        // 2. Запускаем загрузку картинок
                        if (user.photoFileKeys.isNotEmpty()) {
                            downloadServerImages(user.photoFileKeys)
                        } else {
                            // Если фото нет, сразу снимаем флаг загрузки картинок
                            _state.update {
                                it.copy(
                                    images = emptyList(),
                                    isImagesLoading = false
                                )
                            }
                            validateForm()
                        }
                    }

                    result.onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось загрузить профиль"
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isImagesLoading = false,
                                error = errorMsg,
                                user = null
                            )
                        }
                        _effect.send(ProfileSettingsEffect.ShowErrorSnackbar(errorMsg))
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isImagesLoading = false,
                        error = e.message ?: "Неизвестная ошибка"
                    )
                }
            }
        }
    }

    private fun downloadServerImages(keys: List<String>) {
        viewModelScope.launch {
            try {
                // Создаем заглушки сразу, чтобы UI знал сколько будет элементов (опционально)
                // Но лучше дождаться реальных данных, чтобы не моргать скелетонами лишний раз,
                // если загрузка быстрая.

                batchDownloadImagesUseCase(keys).collect { result ->
                    result.onSuccess { batch ->
                        val loadedImages = batch.results.mapNotNull { (key, res) ->
                            res.imageData?.let { bytes ->
                                try {
                                    val uri = ImageUtils.createTempImageUri(application, bytes, key)
                                    ProfileImage(
                                        id = key,
                                        uri = uri,
                                        isLoading = false,
                                        isServerImage = true,
                                        isDeleted = false
                                    )
                                } catch (e: Exception) {
                                    Log.e("ProfileSettingsVM", "Failed to create URI for $key", e)
                                    null
                                }
                            }
                        }

                        // Атомарное обновление всего списка картинок
                        _state.update {
                            it.copy(
                                images = loadedImages,
                                isImagesLoading = false
                            )
                        }
                        validateForm()
                    }

                    result.onFailure { e ->
                        Log.e("ProfileSettingsVM", "Batch download failed", e)
                        // При ошибке загрузки фото мы всё равно снимаем лоадер,
                        // но показываем пустой список или снекбар.
                        // Пользователь сможет добавить фото заново.
                        _state.update {
                            it.copy(
                                images = emptyList(),
                                isImagesLoading = false
                            )
                        }
                        _effect.send(ProfileSettingsEffect.ShowErrorSnackbar("Не удалось загрузить фото"))
                        validateForm()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileSettingsVM", "Exception in downloadServerImages", e)
                _state.update {
                    it.copy(
                        images = emptyList(),
                        isImagesLoading = false
                    )
                }
                validateForm()
            }
        }
    }

    private fun startUpload(uri: Uri) {
        val currentState = _state.value
        val activeImages = currentState.images.filter { !it.isDeleted && it.errorMessage == null }

        if (activeImages.size >= MAX_IMAGES) {
            _effect.trySend(ProfileSettingsEffect.ShowErrorSnackbar("Максимум $MAX_IMAGES фотографий"))
            return
        }

        // Проверка на дубликаты по URI (если пользователь выбрал тот же файл)
        val alreadyExists = currentState.images.any { it.uri == uri && !it.isDeleted }
        if (alreadyExists) {
            return
        }

        val tempId = UUID.randomUUID().toString()
        val newImage = ProfileImage(
            id = tempId,
            uri = uri,
            isLoading = true,
            progress = 0f,
            isServerImage = false,
            isDeleted = false
        )

        // Добавляем в список сразу (оптимистичный UI)
        _state.update {
            it.copy(
                images = it.images + newImage,
                hasChanges = true
            )
        }
        validateForm()

        viewModelScope.launch {
            val inputStream = application.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                markImageError(tempId, "Не удалось открыть файл")
                validateForm()
                return@launch
            }

            try {
                uploadImageUseCase(inputStream = inputStream).collect { result ->
                    result.onSuccess { serverKey ->
                        // Успех: обновляем ID на серверный и снимаем лоадер
                        _state.update { currentState ->
                            val updatedList = currentState.images.map { img ->
                                if (img.id == tempId) {
                                    img.copy(
                                        id = serverKey, // Меняем временный ID на постоянный
                                        isLoading = false,
                                        progress = 1f,
                                        isServerImage = true // Теперь это серверное фото
                                    )
                                } else img
                            }
                            currentState.copy(images = updatedList, hasChanges = true)
                        }
                        validateForm()
                    }

                    result.onFailure { e ->
                        markImageError(tempId, e.message ?: "Ошибка загрузки")
                        validateForm()
                    }
                }
            } catch (e: Exception) {
                markImageError(tempId, e.message ?: "Неизвестная ошибка")
                validateForm()
            } finally {
                inputStream.close()
            }
        }
    }

    private fun retryUpload(imageId: String) {
        val image = _state.value.images.find { it.id == imageId }
        if (image != null && image.uri != null && !image.isServerImage) {
            _state.update {
                it.copy(images = it.images.filter { it.id != imageId })
            }
            startUpload(image.uri)
        }
    }

    private fun markImageError(id: String, msg: String) {
        _state.update { currentState ->
            val updated = currentState.images.map { img ->
                if (img.id == id) img.copy(isLoading = false, errorMessage = msg) else img
            }
            currentState.copy(images = updated)
        }
    }

    private fun removeImage(id: String) {
        val currentState = _state.value
        val image = currentState.images.find { it.id == id }
        if (image == null) return

        if (image.isServerImage) {
            _state.update {
                it.copy(
                    images = it.images.map {
                        if (it.id == id) it.copy(isDeleted = true) else it
                    },
                    hasChanges = true
                )
            }
        } else {
            _state.update {
                it.copy(
                    images = it.images.filter { it.id != id },
                    hasChanges = true
                )
            }
        }
        validateForm()
    }

    private fun saveProfile() {
        if (!_state.value.isFormValid) {
            _effect.trySend(ProfileSettingsEffect.ShowErrorSnackbar("Заполните все обязательные поля"))
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
                // Формируем финальный список ключей:
                // Берем все картинки, которые НЕ помечены на удаление и НЕ имеют ошибок
                val finalKeys = _state.value.images
                    .filter { !it.isDeleted && it.errorMessage == null }
                    .map { it.id }

                updateProfileUseCase(
                    description = _state.value.editedDescription,
                    photoFilesKeys = finalKeys
                )
                    .onSuccess {
                        _effect.send(ProfileSettingsEffect.ShowSuccessSnackbar)
                        _effect.send(ProfileSettingsEffect.NavigateBack)
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось сохранить"
                        _state.update { it.copy(isSaving = false, error = errorMsg) }
                        _effect.send(ProfileSettingsEffect.ShowErrorSnackbar(errorMsg))
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message ?: "Ошибка") }
            }
        }
    }
}