package org.easydictionary.app.domain.models.dictionary

import kotlinx.serialization.Serializable
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.category.CategoryDictionary
import org.easydictionary.app.domain.models.language.Language
import org.easydictionary.app.domain.models.word.WordTag

@Serializable
data class Dictionary(
    val id: Int,
    val dialect: String? = null,
    val langFromId: Int,
    val langToId: Int,
)

@Serializable
data class DictionaryDetailShort(
    val id: Int,
    val dialect: String? = null,
    val langFrom: Language? = null,
    val langTo: Language? = null,
    val wordTagsCount: Int,
    val wordsCount: Int,
    val quizCount: Int,
)

@Serializable
data class WordTypeSelectableItem(
    val name: String,
    val selected: Boolean = false
)

@Serializable
data class DictionaryDetail(
    val id: Int,
    val dialect: String? = null,
    val langFrom: Language,
    val langTo: Language,
    val categories: List<CategoryDictionary> = emptyList(),
    val tags: List<WordTag> = emptyList(),
    val wordTypes: List<WordTypeSelectableItem> = emptyList()
)