package com.fruits.network.util

import kotlinx.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

suspend fun <T> safeCall(block: suspend () -> ApiResult<T>): ApiResult<T> =
    try {
        block()
    } catch (e: ConnectException) {
        ApiResult.Error("Сервер недоступен", -1)
    } catch (e: SocketTimeoutException) {
        ApiResult.Error("Превышено время ожидания", -1)
    } catch (e: IOException) {
        ApiResult.Error("Нет подключения к сети", -1)
    } catch (e: Exception) {
        ApiResult.Error("Неизвестная ошибка: ${e.message}", -1)
    }