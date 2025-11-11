package org.easydictionary.app.data.remote.signup

import org.easydictionary.app.data.models.signup.SignUpRequest
import org.easydictionary.app.data.models.signup.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SignUpApiService {
    @POST("signup")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse
}