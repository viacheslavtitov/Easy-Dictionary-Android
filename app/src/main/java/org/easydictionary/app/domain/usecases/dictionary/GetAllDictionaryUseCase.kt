package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class GetAllDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<Unit, DomainResult<List<Dictionary>>>{
    override suspend fun invoke(params: Unit): Flow<DomainResult<List<Dictionary>>> {
        return dictionaryRepository.getAllDictionaries()
    }

}