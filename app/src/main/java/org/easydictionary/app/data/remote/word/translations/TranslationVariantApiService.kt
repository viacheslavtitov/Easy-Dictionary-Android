package org.easydictionary.app.data.remote.word.translations

import org.easydictionary.app.data.models.CreatedIdResponse
import org.easydictionary.app.data.models.word.translation.EditTranslationRequest
import org.easydictionary.app.data.models.word.translation.TranslationRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface TranslationVariantApiService {
    @DELETE("translation/{id}")
    suspend fun delete(@Path("id") id: Int): Unit

    @POST("translation/edit")
    suspend fun edit(@Body translation: EditTranslationRequest): Unit
    @POST("translation/create")
    suspend fun add(@Body translation: TranslationRequest): CreatedIdResponse
}