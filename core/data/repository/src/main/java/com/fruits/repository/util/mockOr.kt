package com.fruits.repository.util

import com.fruits.debug.MockStorage

suspend fun <T> mockOr(
    mockStorage: MockStorage,
    mockValue: () -> T,
    realCall: suspend () -> Result<T>
): Result<T> = if (mockStorage.enabled) Result.success(mockValue())
               else realCall()
