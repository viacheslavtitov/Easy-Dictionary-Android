package org.easydictionary.app.domain.viewmodels.user.dictionary.words.add

import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.language.Phonetic
import org.easydictionary.app.domain.models.translation.ComposedTranslation
import org.easydictionary.app.domain.models.word.WordDetail
import org.easydictionary.app.domain.models.word.WordTag

data class AddDictionaryWordUiState(
    val translations: List<ComposedTranslation> = emptyList(),
    val wordTypes: List<String> = emptyList(),
    val phonetics: List<Phonetic> = emptyList(),
    val tags: List<WordTag> = emptyList(),
    val newTag: String? = null,
    val isLoading: Boolean = false,
    val dictionary: DictionaryDetailShort? = null,
    val editWord: WordDetail? = null,
    val original: String? = null,
    val phonetic: String? = null,
    val wordType: String? = null
)