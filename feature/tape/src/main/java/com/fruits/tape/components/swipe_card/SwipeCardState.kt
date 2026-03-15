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
    /** Порог для свайпа вверх (пропустить). Сделан выше, чтобы реже срабатывал случайно. */
    var upThreshold = 520f

    suspend fun drag(delta: Offset) {
        // Жёстко блокируем вертикальный скролл — двигаем карту только по X
        if (lockedAxis == null) {
            lockedAxis = LockedAxis.HORIZONTAL
        }

        val constrainedDelta = Offset(delta.x, 0f)
        offset.snapTo(offset.value + constrainedDelta)
    }

    suspend fun settle(screenWidth: Float, screenHeight: Float) {
        lockedAxis = null // сброс блокировки оси
        val x = offset.value.x

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
