package com.fruits.logger

private const val MAX_LOG_LENGTH = 3000

class DebugNetworkLogger : NetworkEventLogger {

    override fun log(tag: String, message: String) {
        if (message.length <= MAX_LOG_LENGTH) {
            Log.d(tag, message)
            return
        }
        var offset = 0
        while (offset < message.length) {
            val end = minOf(offset + MAX_LOG_LENGTH, message.length)
            Log.d(tag, message.substring(offset, end))
            offset = end
        }
    }
}