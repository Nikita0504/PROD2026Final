package com.fruits.tape.components.swipe_card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun SwipeCard(
    modifier: Modifier = Modifier,
    state: SwipeCardState = rememberSwipeCardState(),
    onSwiped: (SwipeDirection) -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val rotation = (state.offset.value.x / screenWidthPx) * 30f
    val alpha = 1f - (abs(state.offset.value.x) / screenWidthPx).coerceIn(0f, 1f) * 0.6f

    Card(
        modifier = modifier
            .graphicsLayer {
                translationX = state.offset.value.x
                translationY = state.offset.value.y
                rotationZ = rotation
                this.alpha = alpha
            }
            .swipeCard(
                state = state,
                screenWidth = screenWidthPx,
                onSwiped = onSwiped
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        content = { Box(Modifier.fillMaxSize(), content = content) }
    )
}
