package org.easydictionary.app.data.remote.register

import org.easydictionary.app.data.models.register.SignUpRequest
import org.easydictionary.app.data.models.register.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpApiService {
    @POST("signup")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse
}