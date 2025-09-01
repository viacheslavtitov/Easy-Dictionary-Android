package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.repository.word.WordRepository
import javax.inject.Inject

class AddWordToDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(
        dictionaryId: Int,
        original: String,
        phonetic: String?,
        type: String?,
        translations: List<TranslationNotCreated>
    ): Flow<DomainResult<Unit>> {
        return wordRepository.createWord(dictionaryId, original, phonetic, type, translations)
    }
}