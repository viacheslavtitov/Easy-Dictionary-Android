package org.easydictionary.app.domain.usecases.languages

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.repository.language.LanguageRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class GetLanguagesUserUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) : BaseUseCase<Unit, DomainResult<List<Language>>> {
    override suspend fun invoke(params: Unit): Flow<DomainResult<List<Language>>> {
        return languageRepository.getAllUserLanguages()
    }

}