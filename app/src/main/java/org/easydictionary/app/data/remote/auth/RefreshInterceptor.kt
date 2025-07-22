package org.easydictionary.app.data.remote.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.utils.PreferenceUtils

class RefreshInterceptor(
    private val preferenceUtils: PreferenceUtils,
    private val authEvents: MutableSharedFlow<GlobalErrorEvent>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val request = requestBuilder.build()
        val response = try {
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            throw e
        }
        if (request.url.encodedPath.contains("refresh")) {
            preferenceUtils.clear()
            authEvents.tryEmit(GlobalErrorEvent.Unauthorized)
        }

        return response
    }
}