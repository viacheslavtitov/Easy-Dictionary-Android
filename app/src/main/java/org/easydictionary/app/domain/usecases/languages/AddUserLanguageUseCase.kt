package org.easydictionary.app.domain.usecases.languages

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.repository.language.LanguageRepository
import javax.inject.Inject

class AddUserLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(code: String?, name: String): Flow<DomainResult<Language>> {
        return languageRepository.addUserLanguage(code, name)
    }
}