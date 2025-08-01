package org.easydictionary.app.data.repositories.dictionary

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.dictionary.DictionaryRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.dictionary.DictionaryApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import javax.inject.Inject

class DictionaryRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val dictionaryApiService: DictionaryApiService
) : DictionaryRepository {
    override suspend fun getAllDictionaries(): Flow<DomainResult<List<Dictionary>>> {
        return flowOf(
            when (val result = wrapApi {
                dictionaryApiService.getAll()
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain() })
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

    override suspend fun getAllDictionariesDetailShort(): Flow<DomainResult<List<DictionaryDetailShort>>> {
        return flowOf(
            when (val result = wrapApi {
                dictionaryApiService.getAllDetailShort()
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain() })
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

    override suspend fun createDictionary(
        dialect: String?,
        langFromId: Int,
        langToId: Int
    ): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                dictionaryApiService.create(
                    DictionaryRequest(
                        dialect = dialect,
                        langFromId = langFromId,
                        langToId = langToId
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

    override suspend fun deleteDictionary(dictionaryId: Int): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                dictionaryApiService.delete(dictionaryId)
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