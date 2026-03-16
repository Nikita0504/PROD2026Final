package com.fruits.logger

import android.util.Log as AndroidLog

object Log {

    fun v(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        AndroidLog.v(tag, message)
    }

    fun d(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        AndroidLog.d(tag, message)
    }

    fun i(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        AndroidLog.i(tag, message)
    }

    fun w(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        AndroidLog.w(tag, message)
    }

    fun e(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        AndroidLog.e(tag, message)
    }

    fun e(tag: String, message: String, tr: Throwable) {
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")
        AndroidLog.e(tag, message, tr)
    }

    fun w(tag: String, message: String, tr: Throwable) {
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")
        AndroidLog.w(tag, message, tr)
    }
}
