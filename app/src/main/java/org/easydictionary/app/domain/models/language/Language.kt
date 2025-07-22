package org.easydictionary.app.domain.models.language

data class Language(
    val id: Int,
    val name: String,
    val code: String
)

enum class LangType(type: Int) {
    FROM(0), TO(1)
}