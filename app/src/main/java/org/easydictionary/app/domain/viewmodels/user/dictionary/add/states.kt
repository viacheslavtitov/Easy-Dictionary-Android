package org.easydictionary.app.domain.viewmodels.user.dictionary.add

import kotlinx.coroutines.sync.Semaphore
import org.easydictionary.app.domain.models.category.CategoryDictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetail
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.dictionary.WordTypeSelectableItem
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.models.word.WordTag

data class WordsFilterUIState(
    val query: String? = "",
    val categories: List<CategoryDictionary> = emptyList(),
    val tags: List<WordTag> = emptyList(),
    val wordTypes: List<WordTypeSelectableItem> = emptyList(),
    val dateFrom: String? = "",
    val dateTo: String? = ""
) {
    fun toCategoryIds() = categories.filter { it.selected }.map { it.id }
    fun toTagIds() = tags.filter { it.selected }.map { it.id }
    fun toWordTypes() = wordTypes.filter { it.selected }.map { it.name }
}

data class AddOrEditUserDictionaryUiState(
    val words: List<WordDetail> = emptyList(),
    val filter: WordsFilterUIState = WordsFilterUIState(),
    val latestAppliedFilter: WordsFilterUIState = WordsFilterUIState(),
    val isLoading: Boolean = false,
    val dialect: String? = null,
    val latestWordsPagId: Int = 0,
    val editDictionary: DictionaryDetailShort? = null,
    val detailDictionary: DictionaryDetail? = null,
    val hasMore: Boolean = true,
    val selectedLanguageFrom: Language? = null,
    val selectedLanguageTo: Language? = null
)