package org.easydictionary.app.domain.models.category

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Category(
    val id: Int,
    val dictionaryId: Int,
    val name: String
) {
    fun toJson(): String = Json.encodeToString(this)
}