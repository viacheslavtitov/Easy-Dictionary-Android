package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.word.WordsResponse
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class SearchWordsForDictionaryParams(
    val query: String,
    val lastPageId: Int,
    val pageSize: Int,
    val dictionaryId: Int
)

class SearchWordsForDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<SearchWordsForDictionaryParams, DomainResult<WordsResponse>> {
    override suspend fun invoke(params: SearchWordsForDictionaryParams): Flow<DomainResult<WordsResponse>> {
        return wordRepository.searchWordsForDictionary(
            params.query,
            params.dictionaryId,
            params.lastPageId,
            params.pageSize
        )
    }
}