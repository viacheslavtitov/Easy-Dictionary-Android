package org.easydictionary.app.domain.usecases.languages

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.LanguageListItem
import org.easydictionary.app.domain.repository.language.LanguageRepository
import java.util.Locale
import javax.inject.Inject

class GetLanguagesStaticUseCase @Inject constructor(
    private val languageRepository: LanguageRepository
) {
    suspend operator fun invoke(): Flow<DomainResult<List<LanguageListItem>>> {
        return languageRepository.getAllLanguages(Locale.getDefault().language)
    }
}