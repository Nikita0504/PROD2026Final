package com.fruits.debug

import android.util.Log as AndroidLog

object Log {

    fun v(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        if (BuildConfig.DEBUG) AndroidLog.v(tag, message)
    }

    fun d(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        if (BuildConfig.DEBUG) AndroidLog.d(tag, message)
    }

    fun i(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        if (BuildConfig.DEBUG) AndroidLog.i(tag, message)
    }

    fun w(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        if (BuildConfig.DEBUG) AndroidLog.w(tag, message)
    }

    fun e(tag: String, message: String) {
        DebugLogStorage.log(tag, message)
        if (BuildConfig.DEBUG) AndroidLog.e(tag, message)
    }

    fun e(tag: String, message: String, tr: Throwable) {
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")
        if (BuildConfig.DEBUG) AndroidLog.e(tag, message, tr)
    }

    fun w(tag: String, message: String, tr: Throwable) {
        DebugLogStorage.log(tag, "$message\n${tr.stackTraceToString()}")
        if (BuildConfig.DEBUG) AndroidLog.w(tag, message, tr)
    }
}
