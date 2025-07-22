package org.easydictionary.app.data.models.language

import org.easydictionary.app.domain.models.language.Language

data class LanguageResponse(
    val id: Int,
    val name: String,
    val code: String
) {
    fun toDomain(): Language {
        return Language(
            id = id,
            name = name,
            code = code
        )
    }
}