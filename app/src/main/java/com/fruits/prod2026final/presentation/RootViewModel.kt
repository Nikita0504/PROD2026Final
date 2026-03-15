package com.fruits.prod2026final.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fruits.domain.model.SessionState
import com.fruits.prod2026final.BuildConfig
import com.fruits.prod2026final.util.ShakeDetector
import com.fruits.session.SessionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RootViewModel(
    private val sessionManager: SessionManager,
    private val shakeDetector: ShakeDetector
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = sessionManager.sessionState

    init {
        viewModelScope.launch { sessionManager.restoreSession() }
        if (BuildConfig.DEBUG) shakeDetector.start()
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stop()
    }
}
