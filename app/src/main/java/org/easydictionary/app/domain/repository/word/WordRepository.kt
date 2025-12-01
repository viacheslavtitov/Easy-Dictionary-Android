package org.easydictionary.app.domain.repository.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.translation.TranslationNotCreated
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.models.word.WordsResponse
import org.easydictionary.app.domain.repository.BaseRepository

interface WordRepository : BaseRepository {
    suspend fun getWordTypes(lang: String): Flow<DomainResult<List<String>>>
    suspend fun createWord(
        dictionaryId: Int,
        original: String,
        phonetic: String?,
        type: String?,
        translations: List<TranslationNotCreated>,
        tags: List<WordTag>
    ): Flow<DomainResult<Unit>>
    suspend fun updateWord(
        wordId: Int,
        dictionaryId: Int,
        original: String,
        phonetic: String?,
        type: String?,
        tags: List<Int>
    ): Flow<DomainResult<Unit>>

    suspend fun getAllWordsForDictionary(
        dictionaryId: Int,
        latestPagId: Int,
        pageSize: Int,
        query: String,
        categoryIds: List<Int> ,
        tagIds: List<Int>,
        wordTypes: List<String>,
        from: String,
        to: String
    ): Flow<DomainResult<WordsResponse>>

    suspend fun deleteWord(wordId: Int): Flow<DomainResult<Unit>>
}