package com.fruits.prod2026final.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruits.domain.model.SessionState
import com.fruits.prod2026final.navigation.AuthNavGraph
import com.fruits.prod2026final.navigation.MainNavGraph
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRoot(
    rootViewModel: RootViewModel = koinViewModel()
) {
    val sessionState by rootViewModel.sessionState.collectAsStateWithLifecycle()

    when (sessionState) {
        SessionState.Loading -> SplashScreen()
        SessionState.Unauthorized -> AuthNavGraph()
        SessionState.Authorized -> MainNavGraph()
    }
}
