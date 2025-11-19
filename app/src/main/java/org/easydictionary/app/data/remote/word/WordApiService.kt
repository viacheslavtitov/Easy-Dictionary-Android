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
        @Query(
            value = "dictionaryId",
            encoded = false
        ) dictionaryId: Int,
        @Query(value = "lastId", encoded = false) lastId: Int,
        @Query(value = "pageSize", encoded = false) pageSize: Int
    ): WordsResponse

    @GET("word/search")
    suspend fun searchWordsForDictionary(
        @Query(
            value = "dictionaryId",
            encoded = false
        ) dictionaryId: Int,
        @Query(value = "lastId", encoded = false) lastId: Int,
        @Query(value = "pageSize", encoded = false) pageSize: Int,
        @Query(value = "query", encoded = true) query: String
    ): WordsResponse

    @DELETE("word/{id}")
    suspend fun delete(@Path("id") id: Int): Unit
}