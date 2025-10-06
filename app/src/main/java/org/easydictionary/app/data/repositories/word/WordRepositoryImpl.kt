package org.easydictionary.app.data.repositories.word

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.dictionary.DictionaryRequest
import org.easydictionary.app.data.models.word.WordRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.word.WordApiService
import org.easydictionary.app.data.remote.word.types.WordTypesStaticApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.models.word.Word
import org.easydictionary.app.domain.models.word.WordsResponse
import org.easydictionary.app.domain.repository.word.WordRepository
import javax.inject.Inject

class WordRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val wordTypesStaticApiService: WordTypesStaticApiService,
    private val wordApiService: WordApiService,
) : WordRepository {
    override suspend fun getWordTypes(lang: String): Flow<DomainResult<List<String>>> {
        return flowOf(
            when (val result = wrapApi {
                wordTypesStaticApiService.getAll(lang)
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data)
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

    override suspend fun createWord(
        dictionaryId: Int,
        original: String,
        phonetic: String?,
        type: String?,
        translations: List<TranslationNotCreated>
    ): Flow<DomainResult<Unit>> {
        return flowOf(
            when (val result = wrapApi {
                wordApiService.create(
                    WordRequest(
                        dictionaryId = dictionaryId,
                        original = original,
                        phonetic = phonetic,
                        type = type,
                        translations.map { it.toRequest() }
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

    override suspend fun getAllWordsForDictionary(
        dictionaryId: Int,
        latestPagId: Int,
        pageSize: Int
    ): Flow<DomainResult<WordsResponse>> {
        return flowOf(
            when (val result = wrapApi {
                wordApiService.getAllForDictionary(
                    dictionaryId = dictionaryId, lastId = latestPagId, pageSize = pageSize
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(
                        WordsResponse(
                            latestId = result.data.latestId,
                            words = result.data.words.map { it.toDomain() }
                        ))
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

    override suspend fun searchWordsForDictionary(
        query: String,
        dictionaryId: Int,
        latestPagId: Int,
        pageSize: Int
    ): Flow<DomainResult<WordsResponse>> {
        return flowOf(
            when (val result = wrapApi {
                wordApiService.searchWordsForDictionary(
                    dictionaryId = dictionaryId,
                    lastId = latestPagId,
                    pageSize = pageSize,
                    query = query
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(
                        WordsResponse(
                            latestId = result.data.latestId,
                            words = result.data.words.map { it.toDomain() }
                        ))
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