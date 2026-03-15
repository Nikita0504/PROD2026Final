package com.fruits.prod2026final.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fruits.chat.chatScreen
import com.fruits.chatlist.chatListScreen
import com.fruits.navigation.Route
import com.fruits.navigation.TopLevelRoutes
import com.fruits.profile.profileScreen
import com.fruits.tape.tapeScreen
import kotlinx.coroutines.launch

@Composable
fun MainNavGraph() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val showBottomBar = TopLevelRoutes.routes.any { route ->
            navBackStackEntry?.destination?.hasRoute(route::class) == true
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    NavigationBar {
                        TopLevelRoutes.routes.forEach { route ->
                            val selected = navBackStackEntry?.destination
                                ?.hierarchy
                                ?.any { it.hasRoute(route::class) } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    val (label, letter) = when (route) {
                                        Route.Tape -> "Лента" to "L"
                                        Route.Chat -> "Чат" to "C"
                                        Route.Profile -> "Профиль" to "P"
                                        else -> "" to "?"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(
                                                if (selected) Color(0xFF4CAF50) else Color(
                                                    0xFF9E9E9E
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                },
                                label = {
                                    val label = when (route) {
                                        Route.Tape -> "Лента"
                                        Route.ChatList -> "Чат"
                                        Route.Profile -> "Профиль"
                                        else -> ""
                                    }
                                    Text(text = label)
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Route.Tape,
                modifier = Modifier.padding(
                    bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp
                )
            ) {
                tapeScreen()
                chatListScreen(
                    onNavigateToChatDetail = { chatId ->
                        navController.navigate(Route.Chat(chatId = chatId))
                    }
                )
                chatScreen(
                    onBack = { navController.popBackStack() }
                )
                profileScreen(
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}
