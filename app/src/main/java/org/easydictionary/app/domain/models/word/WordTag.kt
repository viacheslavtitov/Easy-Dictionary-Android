package org.easydictionary.app.domain.models.word

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WordTag(
    val id: Int,
    val dictionaryId: Int,
    val wordId: Int?,
    val name: String,
    val selected: Boolean = false
) {
    fun toJson(): String = Json.encodeToString(this)
}