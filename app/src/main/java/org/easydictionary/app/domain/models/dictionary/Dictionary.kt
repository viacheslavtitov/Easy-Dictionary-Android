package org.easydictionary.app.domain.models.dictionary

import org.easydictionary.app.domain.models.language.Language

data class Dictionary(
    val id: Int,
    val dialect: String? = null,
    val langFromId: Int,
    val langToId: Int,
)

data class DictionaryDetailShort(
    val id: Int,
    val dialect: String? = null,
    val langFrom: Language? = null,
    val langTo: Language? = null,
    val wordTagsCount: Int,
    val wordsCount: Int,
    val quizCount: Int,
)