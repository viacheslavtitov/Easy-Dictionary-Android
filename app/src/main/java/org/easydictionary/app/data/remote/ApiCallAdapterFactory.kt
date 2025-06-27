package org.easydictionary.app.data.remote

import kotlinx.coroutines.Deferred
import okhttp3.Request
import okio.Timeout
import org.easydictionary.app.data.remote.errors.ErrorParser
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Deferred::class.java &&
            getRawType(returnType) != ApiResult::class.java) {
            return null
        }

        val responseType = getParameterUpperBound(0, returnType as ParameterizedType)
        return ApiCallAdapter<Any>(responseType)
    }

    private class ApiCallAdapter<R>(
        private val responseType: Type
    ) : CallAdapter<R, suspend () -> ApiResult<R>> {

        override fun responseType(): Type = responseType

        override fun adapt(call: Call<R>): suspend () -> ApiResult<R> = {
            try {
                val response = call.execute()
                if (response.isSuccessful && response.body() != null) {
                    ApiResult.Success(response.body()!!)
                } else {
                    val errorMsg = ErrorParser.parse(response.errorBody())
                    ApiResult.ApiError(errorMsg, response.code())
                }
            } catch (e: IOException) {
                ApiResult.NetworkError(e)
            } catch (e: Exception) {
                ApiResult.UnknownError(e)
            }
        }
    }
}

