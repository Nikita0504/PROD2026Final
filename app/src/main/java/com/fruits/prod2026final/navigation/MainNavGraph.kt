package com.fruits.prod2026final.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fruits.chat.chatScreen
import com.fruits.chatlist.chatListScreen
import com.fruits.navigation.Route
import com.fruits.navigation.TopLevelRoutes
import com.fruits.profile.profileScreen
import com.fruits.settings.ProfileSettingsRoute
import com.fruits.tape.tapeScreen
import kotlinx.coroutines.launch

private data class NavItemConfig(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun MainNavGraph() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = TopLevelRoutes.routes.any { route ->
        navBackStackEntry?.destination?.hasRoute(route::class) == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }) +
                        fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }) +
                        fadeOut(animationSpec = tween(300)),
            ) {
                NavigationBar(
                    tonalElevation = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    TopLevelRoutes.routes.forEach { route ->
                        val selected = navBackStackEntry?.destination
                            ?.hierarchy
                            ?.any { it.hasRoute(route::class) } == true

                        val itemConfig = when (route) {
                            Route.Tape -> NavItemConfig(
                                label = "Лента",
                                selectedIcon = Icons.Filled.DynamicFeed,
                                unselectedIcon = Icons.Outlined.DynamicFeed,
                            )
                            Route.ChatList -> NavItemConfig(
                                label = "Чаты",
                                selectedIcon = Icons.Filled.Forum,
                                unselectedIcon = Icons.Outlined.Forum,
                            )
                            Route.Profile -> NavItemConfig(
                                label = "Профиль",
                                selectedIcon = Icons.Filled.Person,
                                unselectedIcon = Icons.Outlined.Person,
                            )
                            else -> null
                        } ?: return@forEach

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
                                Icon(
                                    imageVector = if (selected) itemConfig.selectedIcon else itemConfig.unselectedIcon,
                                    contentDescription = itemConfig.label,
                                )
                            },
                            label = {
                                Text(
                                    text = itemConfig.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                            alwaysShowLabel = true,
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
                },
                onNavigateToProfileSettings = {
                    navController.navigate(Route.ProfileSettings)
                }
            )
            composable<Route.ProfileSettings> {
                ProfileSettingsRoute(
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
