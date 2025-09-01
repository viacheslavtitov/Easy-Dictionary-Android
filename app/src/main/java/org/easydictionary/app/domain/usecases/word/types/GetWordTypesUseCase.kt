package org.easydictionary.app.domain.usecases.word.types

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.WordRepository
import java.util.Locale
import javax.inject.Inject

class GetWordTypesUseCase @Inject constructor(
    private val wordRepository: WordRepository
) {
    suspend operator fun invoke(): Flow<DomainResult<List<String>>> {
        return wordRepository.getWordTypes(Locale.getDefault().language)
    }
}