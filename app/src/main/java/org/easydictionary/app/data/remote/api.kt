package org.easydictionary.app.data.remote

import org.easydictionary.app.data.remote.errors.ErrorParser
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): ApiResult<T> {
    return try {
        val result = apiCall()
        ApiResult.Success(result)
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string()
        if(e.code() != 404) {
            ApiResult.ApiError(ErrorParser.parse(errorMsg), e.code())
        } else {
            ApiResult.ApiError(errorMsg ?: e.message(), e.code())
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    } catch (e: Exception) {
        ApiResult.UnknownError(e)
    }
}