package org.easydictionary.app.data.remote.dictionary

import org.easydictionary.app.data.models.dictionary.DictionaryDetailShortResponse
import org.easydictionary.app.data.models.dictionary.DictionaryRequest
import org.easydictionary.app.data.models.dictionary.DictionaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface DictionaryApiService {
    @GET("dictionary/all")
    suspend fun getAll(): List<DictionaryResponse>

    @GET("dictionary/all/short")
    suspend fun getAllDetailShort(): List<DictionaryDetailShortResponse>

    @POST("dictionary/create")
    suspend fun create(@Body dictionary: DictionaryRequest): Unit
}