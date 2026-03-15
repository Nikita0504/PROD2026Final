package com.fruits.tape.components.swipe_card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val screenWidthPx  = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val rotation = (state.offset.value.x / screenWidthPx) * 30f
    val alpha = 1f - (abs(state.offset.value.x) / screenWidthPx).coerceIn(0f, 1f) * 0.6f

    val x = state.offset.value.x
    val y = state.offset.value.y
    val rightAlpha = (x / state.threshold).coerceIn(0f, 1f)
    val leftAlpha = (-x / state.threshold).coerceIn(0f, 1f)
    val upAlpha = (-y / state.upThreshold).coerceIn(0f, 1f)

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
                onSwiped = onSwiped
            ),
        elevation = CardDefaults.cardElevation(8.dp),
        content = {
            Box(Modifier.fillMaxSize()) {
                content()
                SwipeOverlay(
                    label = "Нет",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .alpha(leftAlpha),
                    color = Color(0xFFE53935),
                )
                SwipeOverlay(
                    label = "Да",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .alpha(rightAlpha),
                    color = Color(0xFF43A047),
                )
                SwipeOverlay(
                    label = "Пропустить",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .alpha(upAlpha),
                    color = Color(0xFF5C6BC0),
                )
            }
        }
    )
}

@Composable
private fun SwipeOverlay(
    label: String,
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .padding(20.dp)
            .border(4.dp, color, RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 28.sp,
        )
    }
}
