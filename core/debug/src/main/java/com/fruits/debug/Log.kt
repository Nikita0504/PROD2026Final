package com.fruits.debug

import okhttp3.internal.platform.android.AndroidLog
import com.fruits.debug.DebugLogStorage

object Log {
    fun v(tag: String, message: String) = DebugLogStorage.log(tag, message)
    fun d(tag: String, message: String) = DebugLogStorage.log(tag, message)
    fun i(tag: String, message: String) = DebugLogStorage.log(tag, message)
    fun w(tag: String, message: String) = DebugLogStorage.log(tag, message)
    fun e(tag: String, message: String) = DebugLogStorage.log(tag, message)

    // Перегрузки с Throwable — как в стандартном API
    fun e(tag: String, message: String, tr: Throwable) =
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")

    fun w(tag: String, message: String, tr: Throwable) =
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")
}