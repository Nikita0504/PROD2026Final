package com.fruits.network.util

interface NetworkEventLogger {
    fun log(tag: String, message: String)
}

object NoOpNetworkLogger : NetworkEventLogger {
    override fun log(tag: String, message: String) = Unit
}