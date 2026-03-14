package com.fruits.tape.components.swipe_card

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

fun Modifier.swipeCard(
    state: SwipeCardState,
    screenWidth: Float,
    onSwiped: (SwipeDirection) -> Unit = {}
): Modifier = composed {
    val scope = rememberCoroutineScope()

    this.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                scope.launch {
                    state.drag(Offset(dragAmount.x, dragAmount.y))
                }
            },
            onDragEnd = {
                scope.launch {
                    state.settle(screenWidth)
                    if (state.swipedDirection != SwipeDirection.NONE) {
                        onSwiped(state.swipedDirection)
                    }
                }
            },
            onDragCancel = {
                scope.launch { state.settle(screenWidth) }
            }
        )
    }
}

