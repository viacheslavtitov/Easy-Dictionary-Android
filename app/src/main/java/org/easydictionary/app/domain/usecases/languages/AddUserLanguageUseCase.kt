package org.easydictionary.app.domain.usecases.languages

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.repository.language.LanguageRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class AddUserLanguageParams(
    val code: String?,
    val name: String
)

class AddUserLanguageUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) : BaseUseCase<AddUserLanguageParams, DomainResult<Language>>{
    override suspend fun invoke(params: AddUserLanguageParams): Flow<DomainResult<Language>> {
        return languageRepository.addUserLanguage(params.code, params.name)
    }

}