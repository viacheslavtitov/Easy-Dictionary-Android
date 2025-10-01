package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class DeleteDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<Int, DomainResult<Unit>> {
    override suspend fun invoke(dictionaryId: Int): Flow<DomainResult<Unit>> {
        return dictionaryRepository.deleteDictionary(dictionaryId)
    }
}