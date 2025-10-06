package org.easydictionary.app.domain.usecases.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class CreateDictionaryParams(
    val dialect: String?,
    val langFromId: Int,
    val langToId: Int
)

class CreateDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : BaseUseCase<CreateDictionaryParams, DomainResult<Unit>> {
    override suspend fun invoke(params: CreateDictionaryParams): Flow<DomainResult<Unit>> {
        return dictionaryRepository.createDictionary(
            dialect = params.dialect,
            langFromId = params.langFromId,
            langToId = params.langToId
        )
    }
}