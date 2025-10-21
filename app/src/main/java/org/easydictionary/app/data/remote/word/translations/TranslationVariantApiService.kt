package org.easydictionary.app.data.remote.word.translations

import retrofit2.http.DELETE
import retrofit2.http.Path

interface TranslationVariantApiService {
    @DELETE("translation/{id}")
    suspend fun delete(@Path("id") id: Int): Unit
}