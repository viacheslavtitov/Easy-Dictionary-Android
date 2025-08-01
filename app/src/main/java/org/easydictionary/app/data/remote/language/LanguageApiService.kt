package org.easydictionary.app.data.remote.language

import org.easydictionary.app.data.models.language.LanguageRequest
import org.easydictionary.app.data.models.language.LanguageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LanguageApiService {
    @GET("languages/all")
    suspend fun getAll(): List<LanguageResponse>
    @POST("languages/create")
    suspend fun add(@Body language: LanguageRequest): LanguageResponse
}