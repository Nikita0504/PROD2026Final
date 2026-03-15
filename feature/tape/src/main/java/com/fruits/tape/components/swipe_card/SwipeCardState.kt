package com.fruits.tape.components.swipe_card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

private enum class LockedAxis { HORIZONTAL, VERTICAL }

class SwipeCardState {
    val offset = Animatable(Offset.Zero, Offset.VectorConverter)

    var swipedDirection by mutableStateOf(SwipeDirection.NONE)
        private set

    // Фиксируется при первом drag-событии, сбрасывается после settle/reset
    private var lockedAxis: LockedAxis? = null

    var threshold = 400f
    var upThreshold = 300f

    suspend fun drag(delta: Offset) {
        // Определяем ось один раз — по первому значимому движению пальца
        if (lockedAxis == null) {
            lockedAxis = if (abs(delta.x) >= abs(delta.y)) {
                LockedAxis.HORIZONTAL
            } else {
                LockedAxis.VERTICAL
            }
        }

        val constrainedDelta = when (lockedAxis) {
            LockedAxis.HORIZONTAL -> Offset(delta.x, 0f)
            // Только вверх (отрицательный Y), вниз не тянем
            LockedAxis.VERTICAL   -> Offset(0f, delta.y.coerceAtMost(0f))
            null                  -> delta
        }
        offset.snapTo(offset.value + constrainedDelta)
    }

    suspend fun settle(screenWidth: Float, screenHeight: Float) {
        lockedAxis = null // сброс блокировки оси
        val x = offset.value.x
        val y = offset.value.y

        when {
            x > threshold -> {
                swipedDirection = SwipeDirection.RIGHT
                offset.animateTo(
                    Offset(screenWidth * 1.5f, offset.value.y),
                    spring(dampingRatio = 0.8f, stiffness = 200f)
                )
            }
            x < -threshold -> {
                swipedDirection = SwipeDirection.LEFT
                offset.animateTo(
                    Offset(-screenWidth * 1.5f, offset.value.y),
                    spring(dampingRatio = 0.8f, stiffness = 200f)
                )
            }
            y < -upThreshold -> {
                swipedDirection = SwipeDirection.UP
                offset.animateTo(
                    Offset(0f, -screenHeight * 1.5f),
                    spring(dampingRatio = 0.8f, stiffness = 200f)
                )
            }
            else -> {
                offset.animateTo(
                    Offset.Zero,
                    spring(dampingRatio = 0.6f, stiffness = 400f)
                )
            }
        }
    }

    suspend fun reset() {
        swipedDirection = SwipeDirection.NONE
        lockedAxis = null
        offset.snapTo(Offset.Zero)
    }
}

@Composable
fun rememberSwipeCardState() = remember { SwipeCardState() }
