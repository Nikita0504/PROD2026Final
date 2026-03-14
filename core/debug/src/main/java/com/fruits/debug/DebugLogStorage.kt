package com.fruits.debug


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DebugLogStorage {
    private const val MAX = 200
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String) {
        val updated = _logs.value.toMutableList().also {
            if (it.size >= MAX) it.removeAt(0)
            it.add(LogEntry(tag, message, System.currentTimeMillis()))
        }
        _logs.value = updated
    }

    fun clear() { _logs.value = emptyList() }

    data class LogEntry(val tag: String, val message: String, val timestamp: Long)
}