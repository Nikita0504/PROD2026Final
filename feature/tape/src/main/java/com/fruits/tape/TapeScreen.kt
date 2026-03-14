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
import com.fruits.tape.components.swipe_card.rememberSwipeCardState
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TapeRoute(
    viewModel: TapeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effects = viewModel.effects

    var sheetContent by remember { mutableStateOf<Pair<String, String>?>(null) }
    LaunchedEffect(Unit) {
        effects.collect { effect ->
            when (effect) {
                is TapeEffect.ShowReasonSheet -> {
                    sheetContent = "Почему в ленте" to effect.reason
                }
                is TapeEffect.ShowAboutSheet -> {
                    sheetContent = "О себе" to effect.about
                }
            }
        }
    }

    TapeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        sheetContent = sheetContent,
        onDismissSheet = { sheetContent = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TapeScreen(
    state: TapeState,
    onEvent: (TapeEvent) -> Unit,
    sheetContent: Pair<String, String>?,
    onDismissSheet: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val cardState = rememberSwipeCardState()
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp - 48).dp.coerceAtMost(400.dp)
    val cardHeight = (configuration.screenHeightDp * 0.55f).toInt().dp

    LaunchedEffect(state.currentCard?.id) {
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
                    state.currentCard != null -> {
                        SwipeCard(
                            modifier = Modifier.fillMaxSize(),
                            state = cardState,
                            onSwiped = {
                                coroutineScope.launch {
                                    cardState.reset()
                                    onEvent(TapeEvent.OnCardSwiped)
                                }
                            },
                        ) {
                            TapeCardContent(
                                name = state.currentCard.name,
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (sheetContent != null) {
        val (title, text) = sheetContent
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissSheet,
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text,
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

