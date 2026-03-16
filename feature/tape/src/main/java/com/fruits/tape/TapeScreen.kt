package com.fruits.tape

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun TapeScreen(
    state: TapeState,
    onEvent: (TapeEvent) -> Unit,
    reasonSheet: List<String>?,
    aboutSheet: String?,
    onDismissReasonSheet: () -> Unit,
    onDismissAboutSheet: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val cardState = rememberSwipeCardState()
    val likedCardState = rememberSwipeCardState()
    val configuration = LocalConfiguration.current
    val cardWidth = (configuration.screenWidthDp - 48).dp.coerceAtMost(400.dp)
    val cardHeight = (configuration.screenHeightDp * 0.55f).toInt().dp

    LaunchedEffect(state.currentCard?.userId) {
        cardState.reset()
    }

    LaunchedEffect(state.likedCurrentCard?.userId) {
        likedCardState.reset()
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
            TapeTabSwitcher(
                selectedTab = state.selectedTab,
                onTabSelected = { onEvent(TapeEvent.OnTabSelected(it)) },
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("tape_tab_switcher"),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (state.selectedTab) {
                    TapeTab.Liked -> {
                        Box(
                            modifier = Modifier.size(cardWidth, cardHeight).testTag("liked_container"),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                state.likedIsLoading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.testTag("liked_progress"))
                                    }
                                }
                                state.likedError != null -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        TapeErrorTitle(Modifier.testTag("liked_error_title"))
                                        Spacer(Modifier.height(12.dp))
                                        TapeRetryButton(
                                            onRetryClick = { onEvent(TapeEvent.OnLikedRetry) },
                                            modifier = Modifier.testTag("liked_retry_button")
                                        )
                                    }
                                }
                                state.likedIsEmpty -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        TapeLikedEmptyTitle(Modifier.testTag("liked_empty_title"))
                                        Spacer(Modifier.height(8.dp))
                                        TapeLikedEmptySubtitle(Modifier.testTag("liked_empty_subtitle"))
                                    }
                                }
                                state.likedCurrentCard != null -> {
                                    SwipeCard(
                                        modifier = Modifier.fillMaxSize().testTag("liked_swipe_card"),
                                        state = likedCardState,
                                        onSwiped = { direction ->
                                            coroutineScope.launch {
                                                likedCardState.reset()
                                                onEvent(TapeEvent.OnLikedCardSwiped(direction))
                                            }
                                        },
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            TapeCardContent(
                                                name = state.likedCurrentCard.name,
                                                age = state.likedCurrentCard.age,
                                                city = state.likedCurrentCard.city,
                                                imageUrl = state.likedCurrentCard.imageUrl,
                                            )
                                            TapeCardOverlay(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth(),
                                                name = state.likedCurrentCard.name,
                                                age = state.likedCurrentCard.age,
                                                city = state.likedCurrentCard.city,
                                                tags = state.likedCurrentCard.tags,
                                                isActionsEnabled = !state.likedIsLoading,
                                                onAboutClick = { onEvent(TapeEvent.OnLikedAboutClicked) },
                                                showWhyButton = false,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TapeTab.Recommendations -> {
                        Box(
                            modifier = Modifier.size(cardWidth, cardHeight).testTag("recommendations_container"),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                state.isLoading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.testTag("recommendations_progress"))
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
                                        TapeErrorTitle(Modifier.testTag("recommendations_error_title"))
                                        Spacer(Modifier.height(12.dp))
                                        TapeRetryButton(
                                            onRetryClick = { onEvent(TapeEvent.OnRetry) },
                                            modifier = Modifier.testTag("recommendations_retry_button")
                                        )
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
                                        TapeEmptyTitle(Modifier.testTag("recommendations_empty_title"))
                                        Spacer(Modifier.height(8.dp))
                                        TapeEmptySubtitle(Modifier.testTag("recommendations_empty_subtitle"))
                                        Spacer(Modifier.height(16.dp))
                                        TapeRetryButton(
                                            onRetryClick = { onEvent(TapeEvent.OnRetry) },
                                            modifier = Modifier.testTag("recommendations_retry_button_empty")
                                        )
                                    }
                                }
                                state.currentCard != null -> {
                                    SwipeCard(
                                        modifier = Modifier.fillMaxSize().testTag("recommendations_swipe_card"),
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
                                                tags = state.currentCard.tags,
                                                isActionsEnabled = !state.isLoading,
                                                onAboutClick = { onEvent(TapeEvent.OnAboutClicked) },
                                                onWhyClick = { onEvent(TapeEvent.OnWhyClicked) },
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
            modifier = Modifier.testTag("reason_bottom_sheet")
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
                    modifier = Modifier.testTag("reason_sheet_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (reasonSheet.isEmpty()) {
                    Text(
                        text = "Причины не указаны",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    reasonSheet.forEachIndexed { index, reason ->
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
                                modifier = Modifier.weight(1f).testTag("reason_item_$index"),
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
            modifier = Modifier.testTag("about_bottom_sheet")
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
                    modifier = Modifier.testTag("about_sheet_title")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = aboutSheet,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .testTag("about_sheet_content"),
                )
            }
        }
    }
}

@Composable
private fun TapeTabSwitcher(
    selectedTab: TapeTab,
    onTabSelected: (TapeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(TapeTab.Recommendations, TapeTab.Liked)
    val selectedIndex = tabs.indexOf(selectedTab)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(4.dp),
        ) {
            // Animated sliding indicator
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val indicatorWidth = maxWidth / 2
                val indicatorOffset by animateDpAsState(
                    targetValue = indicatorWidth * selectedIndex,
                    animationSpec = tween(durationMillis = 250),
                    label = "tab_indicator",
                )
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .offset(x = indicatorOffset)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val label = when (tab) {
                        TapeTab.Recommendations -> "Рекомендации"
                        TapeTab.Liked -> "Лайкнутые"
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .selectable(
                                selected = isSelected,
                                onClick = { onTabSelected(tab) },
                                role = Role.Tab,
                            )
                            .testTag("tab_${tab.name.lowercase()}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            maxLines = 1,
                        )
                    }
                }
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
    tags: List<String>,
    isActionsEnabled: Boolean,
    onAboutClick: () -> Unit,
    onWhyClick: () -> Unit = {},
    showWhyButton: Boolean = true,
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.92f),
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "$name, $age",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("card_user_name")
                )
                Text(
                    text = city,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.80f),
                    modifier = Modifier.testTag("card_user_city")
                )
            }
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.18f),
                            tonalElevation = 0.dp,
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onAboutClick,
                    enabled = isActionsEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_about"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.03f),
                        disabledContentColor = Color.White.copy(alpha = 0.25f),
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "О себе",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (showWhyButton) {
                    Button(
                        onClick = onWhyClick,
                        enabled = isActionsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_why"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.06f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.03f),
                            disabledContentColor = Color.White.copy(alpha = 0.25f),
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QuestionMark,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Почему",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TapeErrorTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Не удалось загрузить",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
    )
}

@Composable
private fun TapeEmptyTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Лента пуста",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun TapeEmptySubtitle(modifier: Modifier = Modifier) {
    Text(
        text = "Новые рекомендации появятся позже",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun TapeLikedEmptyTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Лайкнутых пока нет",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun TapeLikedEmptySubtitle(modifier: Modifier = Modifier) {
    Text(
        text = "Здесь появятся анкеты, которые вы лайкнули",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun TapeRetryButton(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(onClick = onRetryClick, shape = RoundedCornerShape(999.dp), modifier = modifier) {
        Text("Обновить")
    }
}
