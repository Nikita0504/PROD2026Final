package com.fruits.tape.components.swipe_card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val rotation = (state.offset.value.x / state.threshold) * 12f
    val alpha = 1f - (abs(state.offset.value.x) / state.threshold).coerceIn(0f, 1f) * 0.3f

    val x = state.offset.value.x
    val y = state.offset.value.y
    val rightAlpha = (x / state.threshold).coerceIn(0f, 1f)
    val leftAlpha = (-x / state.threshold).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .graphicsLayer {
                translationX = x
                translationY = y
                rotationZ = rotation
                this.alpha = alpha
            }
            .swipeCard(
                state = state,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx,
                onSwiped = onSwiped,
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        content = {
            Box(Modifier.fillMaxSize()) {
                content()
                EdgeGradientOverlay(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .alpha(leftAlpha),
                    width = 72.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE53935).copy(alpha = 0.65f),
                            Color.Transparent,
                        )
                    ),
                )
                EdgeGradientOverlay(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .alpha(rightAlpha),
                    width = 72.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF43A047).copy(alpha = 0.65f),
                        )
                    ),
                )
                SwipeSideLabel(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .alpha(leftAlpha),
                    label = "Не нравится",
                    color = Color(0xFFE53935),
                    isOnRightEdge = true,
                )
                SwipeSideLabel(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .alpha(rightAlpha),
                    label = "Нравится",
                    color = Color(0xFF43A047),
                    isOnRightEdge = false,
                )
            }
        }
    )
}

@Composable
private fun EdgeGradientOverlay(
    modifier: Modifier,
    width: Dp? = null,
    height: Dp? = null,
    brush: Brush,
) {
    val baseModifier = when {
        width != null -> Modifier
            .fillMaxHeight()
            .then(Modifier.width(width))
        height != null -> Modifier
            .fillMaxWidth()
            .then(Modifier.height(height))
        else -> Modifier
    }

    Box(
        modifier = modifier
            .then(baseModifier)
            .background(brush),
    )
}

@Composable
private fun SwipeSideLabel(
    modifier: Modifier,
    label: String,
    color: Color,
    isOnRightEdge: Boolean,
) {
    val colors = if (isOnRightEdge) {
        listOf(
            Color.Transparent,
            color.copy(alpha = 0.35f),
            color.copy(alpha = 0.9f),
        )
    } else {
        listOf(
            color.copy(alpha = 0.9f),
            color.copy(alpha = 0.35f),
            Color.Transparent,
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(120.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = colors,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = label,
            color = Color.White,
        )
    }
}

