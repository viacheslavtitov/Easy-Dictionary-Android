package org.easydictionary.app.domain.usecases.languages

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Phonetic
import org.easydictionary.app.domain.repository.language.PhoneticsRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import java.util.Locale
import javax.inject.Inject

class GetPhoneticsUseCase @Inject constructor(
    private val phoneticsRepository: PhoneticsRepository
) : BaseUseCase<Unit, DomainResult<List<Phonetic>>> {
    override suspend fun invoke(params: Unit): Flow<DomainResult<List<Phonetic>>> {
        return phoneticsRepository.getPhonetics(Locale.getDefault().language)
    }
}