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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
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
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(cardWidth, cardHeight)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Не удалось загрузить",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { onEvent(TapeEvent.OnRetry) }) {
                                Text("Повторить")
                            }
                        }
                    }
                    state.isEmpty -> {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Лента пуста",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Новые рекомендации появятся позже",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { onEvent(TapeEvent.OnRetry) }) {
                                Text("Обновить")
                            }
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
                            TapeCardContent(
                                name = state.currentCard.name,
                                age = state.currentCard.age,
                                city = state.currentCard.city,
                                imageUrl = state.currentCard.imageUrl,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TapeActionBar(
                onWhyClick = { onEvent(TapeEvent.OnWhyClicked) },
                onAboutClick = { onEvent(TapeEvent.OnAboutClicked) },
                enabled = state.currentCard != null && !state.isLoading,
            )

//            if (state.currentCard != null && !state.isLoading) {
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = "Влево — нет · Вправо — да · Вверх — пропустить",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                )
//            }

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
