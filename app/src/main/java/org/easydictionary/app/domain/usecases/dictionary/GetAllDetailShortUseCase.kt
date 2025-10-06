package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class GetAllDetailShortUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<Unit, DomainResult<List<DictionaryDetailShort>>>{
    override suspend fun invoke(params: Unit): Flow<DomainResult<List<DictionaryDetailShort>>> {
        return dictionaryRepository.getAllDictionariesDetailShort()
    }
}