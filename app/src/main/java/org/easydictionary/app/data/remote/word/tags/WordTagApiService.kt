package org.easydictionary.app.data.remote.word.tags

import org.easydictionary.app.data.models.CreatedIdResponse
import org.easydictionary.app.data.models.word.tags.WordTagEntity
import org.easydictionary.app.data.models.word.tags.WordTagRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WordTagApiService {
    //    @DELETE("translation/{id}")
//    suspend fun delete(@Path("id") id: Int): Unit
//
//    @POST("translation/edit")
//    suspend fun edit(@Body translation: EditTranslationRequest): Unit
    @POST("word/tag/create")
    suspend fun create(@Body wordTag: WordTagRequest): CreatedIdResponse

    @GET("word/tag/word/all")
    suspend fun getAllForWord(
        @Query(
            value = "id",
            encoded = false
        ) wordId: Int
    ): List<WordTagEntity>

    @GET("word/tag/dictionary/all")
    suspend fun getAllForDictionary(
        @Query(
            value = "id",
            encoded = false
        ) wordId: Int
    ): List<WordTagEntity>
}