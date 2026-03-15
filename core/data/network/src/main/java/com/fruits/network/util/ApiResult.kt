package com.fruits.network.util

import com.fruits.logger.Log
import io.ktor.client.call.*
import io.ktor.client.statement.*

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int) : ApiResult<Nothing>()
}

suspend inline fun <reified T> HttpResponse.toApiResult(
    vararg errorMap: Pair<Int, String>
): ApiResult<T> {
    val errors = mapOf(*errorMap)
    return if (status.value in 200..299) {
        Log.d("ApiResult", "Successfully request")
        ApiResult.Success(body<T>())
    } else {
        val message = errors[status.value] ?: "Неизвестная ошибка (${status.value})"
        Log.d("ApiResult", "Error request: $message, request body: ${bodyAsText()}")
        ApiResult.Error(message, status.value)
    }
}