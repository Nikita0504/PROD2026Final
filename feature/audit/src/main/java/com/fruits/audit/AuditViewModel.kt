package com.fruits.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.usecase.interactions.GetAuditEventsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuditViewModel(
    private val getAuditEventsUseCase: GetAuditEventsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuditState())
    val state: StateFlow<AuditState> = _state.asStateFlow()

    private val _effect = Channel<AuditEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadAudit()
    }

    fun onEvent(intent: AuditIntent) {
        when (intent) {
            AuditIntent.Refresh -> loadAudit()
        }
    }

    private fun loadAudit() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getAuditEventsUseCase()
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            events = events,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                .onFailure { e ->
                    val message = e.message ?: "Не удалось загрузить историю"
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = message,
                        )
                    }
                    _effect.send(AuditEffect.ShowErrorSnackbar(message))
                }
        }
    }
}
