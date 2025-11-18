package org.easydictionary.app.data.repositories.word.tags

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.word.tags.WordTagRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.word.tags.WordTagApiService
import org.easydictionary.app.data.repositories.handleApiErrors
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.repository.word.tags.TagRepository
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val wordTagApiService: WordTagApiService,
) : TagRepository {
    override suspend fun createTag(
        dictionaryId: Int,
        name: String
    ): Flow<DomainResult<Int>> {
        return flowOf(
            when (val result = wrapApi {
                wordTagApiService.create(
                    WordTagRequest(
                        dictionaryId = dictionaryId,
                        name = name
                    )
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.id)
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                else -> {
                    handleApiErrors(resources, result)
                }
            }
        )
    }

    override suspend fun getAllForDictionary(dictionaryId: Int): Flow<DomainResult<List<WordTag>>> {
        return flowOf(
            when (val result = wrapApi {
                wordTagApiService.getAllForDictionary(dictionaryId)
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain() })
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                else -> {
                    handleApiErrors(resources, result)
                }
            }
        )
    }

    override suspend fun getAllForWord(wordId: Int): Flow<DomainResult<List<WordTag>>> {
        return flowOf(
            when (val result = wrapApi {
                wordTagApiService.getAllForWord(wordId)
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.map { it.toDomain(wordId) })
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                else -> {
                    handleApiErrors(resources, result)
                }
            }
        )
    }
}