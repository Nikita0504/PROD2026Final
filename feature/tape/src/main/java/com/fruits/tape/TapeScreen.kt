package com.fruits.tape

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fruits.tape.components.*
import com.fruits.tape.components.swipe_card.SwipeCard
import com.fruits.tape.components.swipe_card.SwipeDirection
import com.fruits.tape.components.swipe_card.rememberSwipeCardState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TapeRoute(
    viewModel: TapeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effects = viewModel.effects

    var reasonSheet by remember { mutableStateOf<List<String>?>(null) }
    var aboutSheet by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                is TapeEffect.ShowReasonSheet -> reasonSheet = effect.reasons
                is TapeEffect.ShowAboutSheet -> aboutSheet = effect.about
            }
        }
    }

    TapeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        reasonSheet = reasonSheet,
        aboutSheet = aboutSheet,
        onDismissReasonSheet = { reasonSheet = null },
        onDismissAboutSheet = { aboutSheet = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TapeScreen(
    state: TapeState,
    onEvent: (TapeEvent) -> Unit,
    reasonSheet: List<String>?,
    aboutSheet: String?,
    onDismissReasonSheet: () -> Unit,
    onDismissAboutSheet: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val cardState = rememberSwipeCardState()
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp - 48).dp.coerceAtMost(400.dp)
    val cardHeight = (configuration.screenHeightDp * 0.55f).toInt().dp

    LaunchedEffect(state.currentCard?.userId) {
        cardState.reset()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TabRow(
                selectedTabIndex = when (state.selectedTab) {
                    TapeTab.Recommendations -> 0
                    TapeTab.Liked -> 1
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab(
                    selected = state.selectedTab == TapeTab.Recommendations,
                    onClick = { onEvent(TapeEvent.OnTabSelected(TapeTab.Recommendations)) },
                    text = { Text("Рекомендации") },
                )
                Tab(
                    selected = state.selectedTab == TapeTab.Liked,
                    onClick = { onEvent(TapeEvent.OnTabSelected(TapeTab.Liked)) },
                    text = { Text("Лайкнутые") },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state.selectedTab) {
                    TapeTab.Liked -> {
                        if (state.likedCards.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                TapeLikedEmptyTitle()
                                Spacer(Modifier.height(8.dp))
                                TapeLikedEmptySubtitle()
                            }
                        } else {
                            // TODO: когда будет бэк — показывать ленту лайкнутых
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                TapeLikedEmptyTitle()
                                Spacer(Modifier.height(8.dp))
                                TapeLikedEmptySubtitle()
                            }
                        }
                    }
                    TapeTab.Recommendations -> {
                        Box(
                            modifier = Modifier.size(cardWidth, cardHeight),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                state.isLoading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                state.error != null -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        TapeErrorTitle()
                                        Spacer(Modifier.height(12.dp))
                                        TapeRetryButton(onRetryClick = { onEvent(TapeEvent.OnRetry) })
                                    }
                                }
                                state.isEmpty -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        TapeEmptyTitle()
                                        Spacer(Modifier.height(8.dp))
                                        TapeEmptySubtitle()
                                        Spacer(Modifier.height(16.dp))
                                        TapeRetryButton(onRetryClick = { onEvent(TapeEvent.OnRetry) })
                                    }
                                }
                                state.currentCard != null -> {
                                    SwipeCard(
                                        modifier = Modifier.fillMaxSize(),
                                        state = cardState,
                                        onSwiped = { direction ->
                                            coroutineScope.launch {
                                                cardState.reset()
                                                onEvent(TapeEvent.OnCardSwiped(direction))
                                            }
                                        },
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            TapeCardContent(
                                                name = state.currentCard.name,
                                                age = state.currentCard.age,
                                                city = state.currentCard.city,
                                                imageUrl = state.currentCard.imageUrl,
                                            )
                                            TapeCardOverlay(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth(),
                                                name = state.currentCard.name,
                                                age = state.currentCard.age,
                                                city = state.currentCard.city,
                                                isActionsEnabled = !state.isLoading,
                                                onWhyClick = { onEvent(TapeEvent.OnWhyClicked) },
                                                onAboutClick = { onEvent(TapeEvent.OnAboutClicked) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (reasonSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissReasonSheet,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Почему в ленте",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (reasonSheet.isEmpty()) {
                    Text(
                        text = "Причины не указаны",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    reasonSheet.forEach { reason ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }

    if (aboutSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissAboutSheet,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
            ) {
                Text(
                    text = "О себе",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = aboutSheet,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

@Composable
private fun TapeCardOverlay(
    modifier: Modifier,
    name: String,
    age: Int,
    city: String,
    isActionsEnabled: Boolean,
    onWhyClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.85f),
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onAboutClick,
                    enabled = isActionsEnabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(text = "О себе")
                }
                OutlinedButton(
                    onClick = onWhyClick,
                    enabled = isActionsEnabled,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(text = "Почему")
                }
            }
            Text(
                text = "$name, $age",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(
                text = city,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun TapeErrorTitle() {
    Text(
        text = "Не удалось загрузить",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
    )
}

@Composable
private fun TapeEmptyTitle() {
    Text(
        text = "Лента пуста",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TapeEmptySubtitle() {
    Text(
        text = "Новые рекомендации появятся позже",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TapeLikedEmptyTitle() {
    Text(
        text = "Лайкнутых пока нет",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TapeLikedEmptySubtitle() {
    Text(
        text = "Здесь появятся анкеты, которые вы лайкнули",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TapeRetryButton(
    onRetryClick: () -> Unit,
) {
    OutlinedButton(onClick = onRetryClick, shape = RoundedCornerShape(999.dp)) {
        Text("Обновить")
    }
}
