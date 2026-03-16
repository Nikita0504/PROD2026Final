package com.fruits.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.repository.ImageUploadUrlRepository
import com.fruits.domain.usecase.auth.GetProfileUseCase
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
            ProfileEvent.Logout -> logout()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val hasUser = _state.value.user != null
            if (hasUser) {
                _state.update { it.copy(isRefreshing = true, error = null) }
            } else {
                _state.update { it.copy(isLoading = true, error = null) }
            }

            try {
                getProfileUseCase().collect { result ->
                    result.onSuccess { user ->
                        val avatarUrl = resolveAvatarUrl(user.avatarFileKey)
                        _state.update {
                            it.copy(
                                user = user,
                                avatarUrl = avatarUrl,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

                    result.onFailure { e ->
                        val errorMsg = e.message ?: "Не удалось загрузить профиль"
                        _state.update {
                            it.copy(
                                user = if (hasUser) it.user else null,
                                avatarUrl = if (hasUser) it.avatarUrl else null,
                                isLoading = false,
                                isRefreshing = false,
                                error = if (hasUser) null else errorMsg
                            )
                        }
                        if (!hasUser) {
                            _effect.send(ProfileEffect.ShowErrorSnackbar(errorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        user = if (hasUser) it.user else null,
                        avatarUrl = if (hasUser) it.avatarUrl else null,
                        isLoading = false,
                        isRefreshing = false,
                        error = if (hasUser) null else (e.message ?: "Неизвестная ошибка")
                    )
                }
                if (!hasUser) {
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