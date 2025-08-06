package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import javax.inject.Inject

class UpdateDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) {
    suspend operator fun invoke(id: Int, dialect: String?): Flow<DomainResult<Unit>> {
        return dictionaryRepository.updateDictionary(id, dialect)
    }
}