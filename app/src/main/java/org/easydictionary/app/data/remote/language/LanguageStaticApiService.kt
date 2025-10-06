package org.easydictionary.app.data.remote.language

import org.easydictionary.app.data.models.language.LanguageListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LanguageStaticApiService {
    @GET(".")
    suspend fun getAll(@Query("lang") lang: String): List<LanguageListResponse>
}