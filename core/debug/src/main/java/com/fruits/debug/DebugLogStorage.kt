package com.fruits.debug

import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object DebugLogStorage {
    private const val MAX = 200
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    private val _idCounter = AtomicLong(0L)

    fun log(tag: String, message: String) {
        val id = _idCounter.getAndIncrement()
        _logs.update { current ->
            val mutable = current.toMutableList()
            if (mutable.size >= MAX) mutable.removeAt(0)
            mutable.add(LogEntry(id, tag, message, System.currentTimeMillis()))
            mutable
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }

    data class LogEntry(
        val id: Long,
        val tag: String,
        val message: String,
        val timestamp: Long
    )
}
