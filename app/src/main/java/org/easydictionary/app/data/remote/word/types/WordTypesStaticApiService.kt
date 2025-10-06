package org.easydictionary.app.data.remote.word.types

import retrofit2.http.GET
import retrofit2.http.Query

interface WordTypesStaticApiService {
    @GET(".")
    suspend fun getAll(@Query("lang") lang: String): List<String>
}