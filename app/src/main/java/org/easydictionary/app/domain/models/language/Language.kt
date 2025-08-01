package org.easydictionary.app.domain.models.language

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Language(
    val id: Int,
    val name: String,
    val code: String? = null
) {
    fun toJson(): String = Json.encodeToString(this)
}

enum class LangType(val type: Int) {
    FROM(0), TO(1);
    companion object {
        fun fromInt(value: Int?): LangType? = entries.find { it.type == value }
    }
}

@Serializable
data class LanguageListItem(
    val flags: Flag?,
    val code: String,
    val name: String,
    val id: Int?
) {
    fun toJson(): String = Json.encodeToString(this)
}

@Serializable
data class Flag(
    val png: String,
    val svg: String
)