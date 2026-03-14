package com.fruits.prod2026final.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fruits.domain.model.SessionState
import com.fruits.navigation.TopLevelRoutes
import com.fruits.onboarding.OnboardingRoute
import com.fruits.prod2026final.navigation.AuthNavGraph
import com.fruits.prod2026final.navigation.DebugNavGraph
import com.fruits.prod2026final.navigation.MainNavGraph
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRoot(
    rootViewModel: RootViewModel = koinViewModel()
) {
    val sessionState by rootViewModel.sessionState.collectAsStateWithLifecycle()

    when (sessionState) {
        SessionState.Loading -> SplashScreen()
        is SessionState.Unauthorized -> AuthNavGraph()
        is SessionState.Onboarding -> OnboardingRoute()
        SessionState.Authorized -> MainNavGraph()
        SessionState.Debug -> DebugNavGraph()
    }
}
