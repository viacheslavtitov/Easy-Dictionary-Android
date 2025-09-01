package org.easydictionary.app.domain.repository.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.repository.BaseRepository

interface WordRepository : BaseRepository {
    suspend fun getWordTypes(lang: String): Flow<DomainResult<List<String>>>
    suspend fun createWord(
        dictionaryId: Int,
        original: String,
        phonetic: String?,
        type: String?,
        translations: List<TranslationNotCreated>
    ): Flow<DomainResult<Unit>>
}