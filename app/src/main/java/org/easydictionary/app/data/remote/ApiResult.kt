package org.easydictionary.app.data.remote

import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class ApiError(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    data class ServiceUnavailable(val code: Int? = null) : ApiResult<Nothing>()
    data class NetworkError(val exception: IOException) : ApiResult<Nothing>()
    data class UnknownError(val exception: Throwable) : ApiResult<Nothing>()
}

