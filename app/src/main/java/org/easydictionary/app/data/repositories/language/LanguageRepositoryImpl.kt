package org.easydictionary.app.data.repositories.language

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.language.LanguageRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.language.LanguageApiService
import org.easydictionary.app.data.remote.language.LanguageStaticApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.language.LanguageListItem
import org.easydictionary.app.domain.repository.language.LanguageRepository
import javax.inject.Inject

class LanguageRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val languageStaticApiService: LanguageStaticApiService,
    private val languageApiService: LanguageApiService,
) : LanguageRepository {
    override suspend fun getAllLanguages(lang: String): Flow<DomainResult<List<LanguageListItem>>> {
        return flowOf(
            when (val result = wrapApi {
                languageStaticApiService.getAll(lang)
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

    override suspend fun getAllUserLanguages(): Flow<DomainResult<List<Language>>> {
        return flowOf(
            when (val result = wrapApi {
                languageApiService.getAll().body().orEmpty()
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

    override suspend fun addUserLanguage(code: String?, name: String): Flow<DomainResult<Language>> {
        return flowOf(
            when (val result = wrapApi {
                languageApiService.add(LanguageRequest(
                    code = code,
                    name = name
                ))
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.toDomain())
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