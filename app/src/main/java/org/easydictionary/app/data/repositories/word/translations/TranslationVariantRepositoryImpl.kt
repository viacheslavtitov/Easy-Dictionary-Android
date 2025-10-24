package org.easydictionary.app.data.repositories.word.translations

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.word.translation.EditTranslationRequest
import org.easydictionary.app.data.models.word.translation.TranslationRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.word.translations.TranslationVariantApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.translations.TranslationVariantRepository
import javax.inject.Inject

class TranslationVariantRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val translationVariantApiService: TranslationVariantApiService
) : TranslationVariantRepository {
    override suspend fun deleteTranslation(translationId: Int): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                translationVariantApiService.delete(translationId)
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

    override suspend fun editTranslation(
        wordId: Int,
        translationId: Int,
        translate: String,
        description: String?,
        categoryId: Int?
    ): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                translationVariantApiService.edit(
                    EditTranslationRequest(
                        categoryId = categoryId,
                        description = description,
                        translate = translate,
                        id = translationId,
                        wordId = wordId
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

    override suspend fun addTranslation(
        wordId: Int,
        translate: String,
        description: String?,
        categoryId: Int?
    ): Flow<DomainResult<Int>> {
        return flowOf(
            when (val result = wrapApi {
                translationVariantApiService.add(
                    TranslationRequest(
                        categoryId = categoryId,
                        description = description,
                        translate = translate,
                        wordId = wordId
                    )
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.id)
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