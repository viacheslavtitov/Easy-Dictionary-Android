package org.easydictionary.app.data.remote.auth

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import org.easydictionary.app.data.remote.errors.AuthEventsWritable
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.utils.PreferenceUtils

class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val preferenceUtils: PreferenceUtils,
    @AuthEventsWritable private val authEventsEmitter: MutableSharedFlow<GlobalErrorEvent>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        tokenProvider()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
//        return chain.proceed(requestBuilder.build())
        val request = requestBuilder.build()
        val response = try {
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Got exception when try to refresh", e)
            throw e
        }
        if (response.code != 200 && request.url.encodedPath.contains("refresh")) {
            Log.e("AuthInterceptor", "Got error when try to refresh. Response code is ${response.code}")
            preferenceUtils.clear()
            authEventsEmitter.tryEmit(GlobalErrorEvent.Unauthorized)
        }

        return response
    }
}
