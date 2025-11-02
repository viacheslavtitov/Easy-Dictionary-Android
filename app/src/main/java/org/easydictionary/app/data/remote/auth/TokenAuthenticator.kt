package org.easydictionary.app.data.remote.auth

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.easydictionary.app.data.remote.errors.AuthEventsWritable
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRefresher: suspend () -> String?,
    private val preferenceUtils: PreferenceUtils,
    @AuthEventsWritable private val authEventsEmitter: MutableSharedFlow<GlobalErrorEvent>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val newToken = runBlocking { tokenRefresher() }

        return if(newToken != null) {
            preferenceUtils.putSecureString(ACCESS_TOKEN_KEY, newToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            Log.e("AuthInterceptor", "Got error when try to refresh. Response code is ${response.code}")
            preferenceUtils.clear()
            authEventsEmitter.tryEmit(GlobalErrorEvent.Unauthorized)
            return null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
