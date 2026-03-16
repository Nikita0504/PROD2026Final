package com.fruits.repository.util

import com.fruits.network.util.ApiResult

inline fun <T, R> ApiResult<T>.mapResult(
    crossinline transform: (T) -> R
): Result<R> = when (this) {
    is ApiResult.Success -> runCatching { transform(data) }
    is ApiResult.Error   -> Result.failure(Exception(message))
}