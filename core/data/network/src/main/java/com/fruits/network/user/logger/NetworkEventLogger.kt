package com.fruits.network.user.logger

interface NetworkEventLogger {
    fun log(tag: String, message: String)
}

object NoOpNetworkLogger : NetworkEventLogger {
    override fun log(tag: String, message: String) = Unit
}