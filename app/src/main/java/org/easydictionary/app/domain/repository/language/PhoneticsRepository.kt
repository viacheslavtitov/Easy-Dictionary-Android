package org.easydictionary.app.domain.repository.language

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.language.Phonetic
import org.easydictionary.app.domain.repository.BaseRepository

interface PhoneticsRepository: BaseRepository {
    suspend fun getPhonetics(type: String): Flow<DomainResult<List<Phonetic>>>
}