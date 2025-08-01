package org.easydictionary.app.domain.repository.dictionary

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.repository.BaseRepository

interface DictionaryRepository: BaseRepository {
    suspend fun getAllDictionaries(): Flow<DomainResult<List<Dictionary>>>
    suspend fun getAllDictionariesDetailShort(): Flow<DomainResult<List<DictionaryDetailShort>>>
    suspend fun createDictionary(dialect: String?, langFromId: Int, langToId: Int): Flow<DomainResult<Unit>>
    suspend fun deleteDictionary(dictionaryId: Int): Flow<DomainResult<Unit>>
}