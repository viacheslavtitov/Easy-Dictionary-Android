package org.easydictionary.app.domain.repository.word.tags

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.repository.BaseRepository

interface TagRepository : BaseRepository {
    suspend fun createTag(
        dictionaryId: Int,
        name: String
    ): Flow<DomainResult<Int>>

    suspend fun getAllForDictionary(
        dictionaryId: Int
    ): Flow<DomainResult<List<WordTag>>>

    suspend fun getAllForWord(
        wordId: Int,
    ): Flow<DomainResult<List<WordTag>>>
}