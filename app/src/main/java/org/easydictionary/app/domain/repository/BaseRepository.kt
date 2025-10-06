package org.easydictionary.app.domain.repository

import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.safeApiCall

interface BaseRepository {
    suspend fun <T> wrapApi(block: suspend () -> T): ApiResult<T> =
        safeApiCall { block() }
}