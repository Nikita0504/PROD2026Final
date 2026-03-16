package com.fruits.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.model.user.User
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.domain.usecase.image.BatchDownloadImagesUseCase
import com.fruits.logger.Log
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val sessionManager: SessionManager,
    private val userLocalRepository: UserLocalRepository,
    private val batchDownloadImagesUseCase: BatchDownloadImagesUseCase,
    private val application: Application
) : ViewModel() {
    private val avatarUriCache = mutableMapOf<String, String>()


    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadCachedUser()
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.Refresh -> loadProfile()
            ProfileEvent.Logout -> logout()
        }
    }

    private fun loadCachedUser() {
        viewModelScope.launch {
            val cachedUser = userLocalRepository.getCachedUser()
            Log.d("ProfileViewModel", "Cached user: $cachedUser")
            if (cachedUser != null) {
                val avatarUri = resolveAvatarUri(cachedUser)
                _state.update {
                    it.copy(
                        user = cachedUser,
                        avatarUri = avatarUri,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }

            try {
                getProfileUseCase().collect { result ->
                    result.onSuccess { user ->
                        val avatarUri = resolveAvatarUri(user)
                        _state.update {
                            it.copy(
                                user = user,
                                avatarUri = avatarUri, // ← Обновляем URI
                                error = null,
                                isRefreshing = false
                            )
                        }
                    }

                    result.onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось загрузить профиль"
                        _state.update { currentState ->
                            currentState.copy(
                                error = if (currentState.user == null) errorMsg else null,
                                isRefreshing = false
                            )
                        }
                        if (_state.value.user == null) {
                            _effect.send(ProfileEffect.ShowErrorSnackbar(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { currentState ->
                    currentState.copy(
                        error = if (currentState.user == null) e.message else null,
                        isRefreshing = false
                    )
                }
                if (_state.value.user == null) {
                    _effect.send(ProfileEffect.ShowErrorSnackbar(e.message ?: "Неизвестная ошибка"))
                }
            }
        }
    }


    private suspend fun resolveAvatarUri(user: User): String? {
        val fileKey = user.photoFileKeys.firstOrNull() ?: user.avatarFileKey
        if (fileKey == null) return null

        avatarUriCache[fileKey]?.let { cachedUri ->
            return cachedUri
        }

        Log.d("ProfileViewModel", "Downloading avatar for key: $fileKey")

        try {
            val result = batchDownloadImagesUseCase(listOf(fileKey)).firstOrNull()
            result?.getOrNull()?.let { batchResult ->
                batchResult.results[fileKey]?.imageData?.let { bytes ->
                    val newUri = createTempImageUri(bytes, fileKey)
                    avatarUriCache[fileKey] = newUri
                    return newUri
                }
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Failed to download avatar", e)
        }
        return null
    }

    private fun createTempImageUri(bytes: ByteArray, key: String): String {
        return ImageUtils.createTempImageUri(application, bytes, key).toString()
    }

    private fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}
