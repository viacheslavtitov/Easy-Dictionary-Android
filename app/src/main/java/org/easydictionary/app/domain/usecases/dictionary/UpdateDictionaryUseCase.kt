package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class UpdateDictionaryParams(
    val id: Int,
    val dialect: String?
)

class UpdateDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<UpdateDictionaryParams, DomainResult<Unit>> {
    override suspend fun invoke(params: UpdateDictionaryParams): Flow<DomainResult<Unit>> {
        return dictionaryRepository.updateDictionary(params.id, params.dialect)
    }
}
