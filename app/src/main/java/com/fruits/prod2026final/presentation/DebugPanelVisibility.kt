package com.fruits.prod2026final.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит состояние видимости Debug-панели отдельно от session state.
 * Позволяет показывать панель как оверлей поверх текущего графа, не выкидывая его из композиции —
 * при закрытии панели граф и все запросы остаются без рекомпозиции.
 */
class DebugPanelVisibility {
    private val _visible = MutableStateFlow(false)
    val visible: StateFlow<Boolean> = _visible.asStateFlow()

    fun toggle() {
        _visible.value = !_visible.value
    }
}
