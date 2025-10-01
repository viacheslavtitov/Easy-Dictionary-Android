package org.easydictionary.app.domain.models.language

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Phonetic(
    val symbol: String,
    val name: String,
    val unicode: String,
) {
    fun toJson(): String = Json.encodeToString(this)
}