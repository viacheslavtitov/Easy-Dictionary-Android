package org.easydictionary.app.data.remote.language

import org.easydictionary.app.data.models.language.PhoneticsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PhoneticsStaticApiService {
    @GET(".")
    suspend fun getAll(@Query("type") type: String): PhoneticsResponse
}