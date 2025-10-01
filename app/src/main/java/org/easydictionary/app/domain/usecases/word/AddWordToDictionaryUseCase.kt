package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class AddWordToDictionaryParams(
    val dictionaryId: Int,
    val original: String,
    val phonetic: String?,
    val type: String?,
    val translations: List<TranslationNotCreated>
)

class AddWordToDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<AddWordToDictionaryParams, DomainResult<Unit>> {
    override suspend fun invoke(params: AddWordToDictionaryParams): Flow<DomainResult<Unit>> {
        return wordRepository.createWord(
            params.dictionaryId,
            params.original,
            params.phonetic,
            params.type,
            params.translations
        )
    }
}