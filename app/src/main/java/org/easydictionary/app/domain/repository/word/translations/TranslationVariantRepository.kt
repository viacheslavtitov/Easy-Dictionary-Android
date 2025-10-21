package org.easydictionary.app.domain.repository.word.translations

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.BaseRepository

interface TranslationVariantRepository : BaseRepository {
    suspend fun deleteTranslation(translationId: Int): Flow<DomainResult<Unit>>

    suspend fun editTranslation(
        wordId: Int,
        translationId: Int,
        translate: String,
        description: String?,
        categoryId: Int?
    ): Flow<DomainResult<Unit>>
}