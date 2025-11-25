package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.DictionaryDetail
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class GetDetailDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<Int, DomainResult<DictionaryDetail>>{
    override suspend fun invoke(params: Int): Flow<DomainResult<DictionaryDetail>> {
        return dictionaryRepository.getDetailDictionary(params)
    }
}