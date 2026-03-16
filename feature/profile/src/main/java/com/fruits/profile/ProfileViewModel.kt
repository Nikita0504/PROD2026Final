package com.fruits.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.repository.UserLocalRepository
import com.fruits.domain.usecase.auth.GetProfileUseCase
import com.fruits.logger.Log
import com.fruits.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val sessionManager: SessionManager,
    private val imageUploadUrlRepository: ImageUploadUrlRepository,
    private val userLocalRepository: UserLocalRepository,
) : ViewModel() {

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
                val avatarUrl = resolveAvatarUrl(cachedUser.avatarFileKey)
                _state.update {
                    it.copy(
                        user = cachedUser,
                        avatarUrl = avatarUrl,
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
                        val avatarUrl = resolveAvatarUrl(user.avatarFileKey)
                        _state.update {
                            it.copy(
                                user = user,
                                avatarUrl = avatarUrl,
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

    private suspend fun resolveAvatarUrl(avatarFileKey: String?): String? {
        if (avatarFileKey == null) return null
        return imageUploadUrlRepository.getDownloadUrls(listOf(avatarFileKey))
            .getOrNull()
            ?.firstOrNull()
            ?.url
    }

    private fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}
