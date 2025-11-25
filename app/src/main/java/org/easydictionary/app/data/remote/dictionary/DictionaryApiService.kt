package org.easydictionary.app.data.remote.dictionary

import org.easydictionary.app.data.models.dictionary.DictionaryDetailResponse
import org.easydictionary.app.data.models.dictionary.DictionaryDetailShortResponse
import org.easydictionary.app.data.models.dictionary.DictionaryEditRequest
import org.easydictionary.app.data.models.dictionary.DictionaryRequest
import org.easydictionary.app.data.models.dictionary.DictionaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DictionaryApiService {
    @GET("dictionary/all")
    suspend fun getAll(): Response<List<DictionaryResponse>>

    @GET("dictionary/{id}")
    suspend fun getDetailDictionary(@Path("id") id: Int): DictionaryDetailResponse
    @GET("dictionary/all/short")
    suspend fun getAllDetailShort(): Response<List<DictionaryDetailShortResponse>>

    @POST("dictionary/create")
    suspend fun create(@Body dictionary: DictionaryRequest): Unit

    @DELETE("dictionary/{id}")
    suspend fun delete(@Path("id") id: Int): Unit

    @POST("dictionary/edit")
    suspend fun edit(@Body dictionary: DictionaryEditRequest): Unit
}