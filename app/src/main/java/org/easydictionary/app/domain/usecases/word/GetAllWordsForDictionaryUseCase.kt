package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.word.WordsResponse
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class GetAllWordsForDictionaryParams(
    val lastPageId: Int,
    val pageSize: Int,
    val dictionaryId: Int
)

class GetAllWordsForDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<GetAllWordsForDictionaryParams, DomainResult<WordsResponse>> {
    override suspend fun invoke(params: GetAllWordsForDictionaryParams): Flow<DomainResult<WordsResponse>> {
        return wordRepository.getAllWordsForDictionary(
            params.dictionaryId,
            params.lastPageId,
            params.pageSize
        )
    }
}