package org.easydictionary.app.data.repositories.category

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.category.CategoryRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.category.CategoryApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.repository.category.CategoryRepository
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val categoryApiService: CategoryApiService,
) : CategoryRepository {
    override suspend fun getAllCategories(): Flow<DomainResult<List<Category>>> {
        return flowOf(
            when (val result = wrapApi {
                categoryApiService.getAll()
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain() }.sortedBy {
                        it.name
                    })
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                is ApiResult.NetworkError -> {
                    DomainResult.Error(resources.getString(R.string.network_error))
                }

                is ApiResult.UnknownError -> {
                    DomainResult.Error(resources.getString(R.string.unknown_error))
                }
            }
        )
    }

    override suspend fun getAllCategoriesForDictionary(dictionaryId: Int): Flow<DomainResult<List<Category>>> {
        return flowOf(
            when (val result = wrapApi {
                categoryApiService.getAllForDictionary(dictionaryId)
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain() }.sortedBy {
                        it.name
                    })
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                is ApiResult.NetworkError -> {
                    DomainResult.Error(resources.getString(R.string.network_error))
                }

                is ApiResult.UnknownError -> {
                    DomainResult.Error(resources.getString(R.string.unknown_error))
                }
            }
        )
    }

    override suspend fun addCategory(
        dictionaryId: Int,
        name: String
    ): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                categoryApiService.add(
                    CategoryRequest(
                        dictionaryId = dictionaryId,
                        name = name
                    )
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(Unit)
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                is ApiResult.NetworkError -> {
                    DomainResult.Error(resources.getString(R.string.network_error))
                }

                is ApiResult.UnknownError -> {
                    DomainResult.Error(resources.getString(R.string.unknown_error))
                }
            }
        )
    }
}