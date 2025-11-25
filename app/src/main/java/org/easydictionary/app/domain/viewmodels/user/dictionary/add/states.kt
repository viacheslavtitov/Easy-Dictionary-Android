package org.easydictionary.app.domain.viewmodels.user.dictionary.add

import kotlinx.coroutines.sync.Semaphore
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.dictionary.DictionaryDetail
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.models.word.WordTag

data class WordsFilterUIState(
    val query: String? = "",
    val categories: List<Category> = emptyList(),
    val tags: List<WordTag> = emptyList(),
    val wordTypes: List<String> = emptyList(),
) {
    fun toCategoryIds() = categories.map { it.id }
    fun toTagIds() = tags.map { it.id }
}

data class AddOrEditUserDictionaryUiState(
    val words: List<WordDetail> = emptyList(),
    val filter: WordsFilterUIState = WordsFilterUIState(),
    val isLoading: Boolean = false,
    val dialect: String? = null,
    val latestWordsPagId: Int = 0,
    val latestSearchWordsPagId: Int = 0,
    val editDictionary: DictionaryDetailShort? = null,
    val detailDictionary: DictionaryDetail? = null,
    val hasMore: Boolean = true,
    val latestFetchType: FetchWordsType = FetchWordsType.All,
    val lockLoadWords: Semaphore = Semaphore(1, acquiredPermits = 0),
    val selectedLanguageFrom: Language? = null,
    val selectedLanguageTo: Language? = null
)