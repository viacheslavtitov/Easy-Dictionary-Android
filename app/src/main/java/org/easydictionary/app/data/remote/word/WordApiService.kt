package org.easydictionary.app.data.remote.word

import org.easydictionary.app.data.models.word.WordRequest
import org.easydictionary.app.data.models.word.WordUpdateRequest
import org.easydictionary.app.data.models.word.WordsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WordApiService {
    @POST("word/create/translations")
    suspend fun create(@Body word: WordRequest): Unit

    @POST("word/edit")
    suspend fun update(@Body word: WordUpdateRequest): Unit

    @GET("word/all")
    suspend fun getAllForDictionary(
        @Query(value = "dictionaryId", encoded = false) dictionaryId: Int,
        @Query(value = "lastId", encoded = false) lastId: Int,
        @Query(value = "pageSize", encoded = false) pageSize: Int,
        @Query(value = "query", encoded = true) query: String,
        @Query(value = "categoryIds", encoded = false) categoryIds: List<Int>,
        @Query(value = "tagIds", encoded = false) tagIds: List<Int>,
        @Query(value = "wordTypes", encoded = true) wordTypes: List<String>,
        @Query(value = "from", encoded = true) from: String,
        @Query(value = "to", encoded = true) to: String
    ): WordsResponse

    @DELETE("word/{id}")
    suspend fun delete(@Path("id") id: Int): Unit
}