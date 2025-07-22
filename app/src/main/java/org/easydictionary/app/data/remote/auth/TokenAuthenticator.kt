package org.easydictionary.app.data.remote.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenRefresher: suspend () -> String?,
    private val preferenceUtils: PreferenceUtils
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val newToken = runBlocking { tokenRefresher() }

        return newToken?.let {
            preferenceUtils.putSecureString(ACCESS_TOKEN_KEY, it)
            response.request.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
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
