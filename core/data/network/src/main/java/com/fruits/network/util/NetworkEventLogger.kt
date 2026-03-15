package com.fruits.network.util

import android.util.Log

interface NetworkEventLogger {
    fun log(tag: String, message: String)
}

object NoOpNetworkLogger : NetworkEventLogger {
    override fun log(tag: String, message: String) = Unit
}

object AndroidNetworkLogger : NetworkEventLogger {
    private const val MAX_LOG_LENGTH = 3000

    override fun log(tag: String, message: String) {
        if (message.length <= MAX_LOG_LENGTH) {
            Log.d(tag, message)
            return
        }
        // Logcat обрезает длинные строки — делим на части
        var offset = 0
        while (offset < message.length) {
            val end = minOf(offset + MAX_LOG_LENGTH, message.length)
            Log.d(tag, message.substring(offset, end))
            offset = end
        }
    }
}