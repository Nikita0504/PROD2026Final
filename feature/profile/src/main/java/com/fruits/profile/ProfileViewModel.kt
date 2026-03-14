package com.fruits.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.session.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ProfileEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ProfileEffect> = _effects.asSharedFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.OnSettingsClicked -> {
                viewModelScope.launch {
                    _effects.emit(ProfileEffect.NavigateToSettings)
                }
            }
            ProfileEvent.OnPreviewClicked -> {
                viewModelScope.launch {
                    _effects.emit(ProfileEffect.ShowPreviewDialog)
                }
            }
            ProfileEvent.OnLogoutClicked -> viewModelScope.launch {
                println("logout")
                sessionManager.logout()
            }
        }
    }

    private fun loadProfile() {
        _state.update {
            it.copy(
                avatarUrl = "https://profil.adu.by/pluginfile.php/4384/mod_book/chapter/12298/52.6.jpga",
                name = "Имя Пользователя",
                description = "Краткое описание о себе: интересы, чем занимаюсь, что ищу. Пока данные шаблонные, пока нет бэка.",
                isLoading = false,
            )
        }
    }
}
