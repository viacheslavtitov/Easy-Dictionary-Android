package org.easydictionary.app.domain.models.word

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.easydictionary.app.domain.models.translation.Translation
import org.easydictionary.app.domain.models.translation.TranslationWithCategory

@Serializable
data class Word(
    val id: Int,
    val dictionaryId: Int,
    val original: String,
    val phonetic: String? = null,
    val type: String? = null,
    val translations: List<Translation> = emptyList()
) {
    fun toJson(): String = Json.encodeToString(this)
}

@Serializable
data class WordDetail(
    val id: Int,
    val dictionaryId: Int,
    val original: String,
    val phonetic: String? = null,
    val type: String? = null,
    val translations: List<TranslationWithCategory> = emptyList()
) {
    fun toJson(): String = Json.encodeToString(this)
}

@Serializable
data class WordsResponse(
    val latestId: Int,
    val words: List<WordDetail> = emptyList()
) {
    fun toJson(): String = Json.encodeToString(this)
}