package org.easydictionary.app.data.remote.auth

import org.easydictionary.app.data.models.auth.AuthRequest
import org.easydictionary.app.data.models.auth.AuthResponse
import org.easydictionary.app.data.models.auth.refresh_token.RefreshTokenRequest
import org.easydictionary.app.data.models.auth.refresh_token.RefreshTokenResponse
import org.easydictionary.app.data.remote.ApiResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("signin")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResult<RefreshTokenResponse>

}