package com.fruits.tape.components.swipe_card

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset

class SwipeCardState {
    val offset = Animatable(Offset.Zero, Offset.VectorConverter)
    var swipedDirection by mutableStateOf(SwipeDirection.NONE)
        private set

    var threshold = 400f

    suspend fun drag(delta: Offset) {
        offset.snapTo(offset.value + delta)
    }

    suspend fun settle(screenWidth: Float) {
        val currentX = offset.value.x
        when {
            currentX > threshold -> {
                swipedDirection = SwipeDirection.RIGHT
                offset.animateTo(
                    Offset(screenWidth * 1.5f, offset.value.y),
                    spring(dampingRatio = 0.8f, stiffness = 200f)
                )
            }
            currentX < -threshold -> {
                swipedDirection = SwipeDirection.LEFT
                offset.animateTo(
                    Offset(-screenWidth * 1.5f, offset.value.y),
                    spring(dampingRatio = 0.8f, stiffness = 200f)
                )
            }
            else -> {
                offset.animateTo(Offset.Zero, spring(dampingRatio = 0.6f, stiffness = 400f))
            }
        }
    }

    suspend fun reset() {
        swipedDirection = SwipeDirection.NONE
        offset.snapTo(Offset.Zero)
    }
}

@Composable
fun rememberSwipeCardState() = remember { SwipeCardState() }
