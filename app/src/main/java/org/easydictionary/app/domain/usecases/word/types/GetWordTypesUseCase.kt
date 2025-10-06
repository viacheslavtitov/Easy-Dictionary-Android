package org.easydictionary.app.domain.usecases.word.types

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import java.util.Locale
import javax.inject.Inject

class GetWordTypesUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<Unit, DomainResult<List<String>>> {
    override suspend fun invoke(params: Unit): Flow<DomainResult<List<String>>> {
        return wordRepository.getWordTypes(Locale.getDefault().language)
    }

}