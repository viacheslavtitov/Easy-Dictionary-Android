package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class UpdateWordUseCaseParams(
    val wordId: Int,
    val dictionaryId: Int,
    val original: String,
    val wordType: String? = null,
    val phonetic: String? = null,
    val tagIds: List<Int> = emptyList()
)

class UpdateWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<UpdateWordUseCaseParams, DomainResult<Unit>> {
    override suspend fun invoke(params: UpdateWordUseCaseParams): Flow<DomainResult<Unit>> {
        return wordRepository.updateWord(
            wordId = params.wordId,
            dictionaryId = params.dictionaryId,
            original = params.original,
            type = params.wordType,
            phonetic = params.phonetic,
            tags = params.tagIds
        )
    }
}