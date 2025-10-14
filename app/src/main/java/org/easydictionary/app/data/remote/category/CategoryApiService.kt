package org.easydictionary.app.data.remote.category

import org.easydictionary.app.data.models.category.CategoryRequest
import org.easydictionary.app.data.models.category.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoryApiService {
    @GET("translation/category/all")
    suspend fun getAll(): Response<List<CategoryResponse>>

    @GET("translation/category/all/{id}")
    suspend fun getAllForDictionary(@Path("id") id: Int): Response<List<CategoryResponse>>

    @POST("translation/category/create")
    suspend fun add(@Body category: CategoryRequest)
}