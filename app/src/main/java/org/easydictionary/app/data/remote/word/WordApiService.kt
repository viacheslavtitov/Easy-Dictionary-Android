package org.easydictionary.app.data.remote.word

import org.easydictionary.app.data.models.word.WordRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface WordApiService {
    @POST("word/create/translations")
    suspend fun create(@Body word: WordRequest): Unit
}