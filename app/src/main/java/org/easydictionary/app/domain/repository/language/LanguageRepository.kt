package org.easydictionary.app.domain.repository.language

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.language.LanguageListItem
import org.easydictionary.app.domain.repository.BaseRepository

interface LanguageRepository: BaseRepository {
    suspend fun getAllLanguages(lang: String): Flow<DomainResult<List<LanguageListItem>>>
    suspend fun getAllUserLanguages(): Flow<DomainResult<List<Language>>>
    suspend fun addUserLanguage(code: String?, name: String): Flow<DomainResult<Language>>
}