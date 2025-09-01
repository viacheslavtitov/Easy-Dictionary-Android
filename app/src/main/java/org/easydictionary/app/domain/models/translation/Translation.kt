package org.easydictionary.app.domain.models.translation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.easydictionary.app.data.models.word.translation.TranslationRequest
import org.easydictionary.app.domain.models.category.Category

@Serializable
data class ComposedTranslation(
    val category: Category? = null,
    val wordId: Int? = null,
    val translate: String,
    val description: String? = null,
    val id: Int? = null
) {
    fun toRequest(): TranslationRequest = TranslationRequest(
        categoryId = category?.id,
        description = description,
        translate = translate
    )
}

@Serializable
data class TranslationNotCreated(
    val category: Category? = null,
    val translate: String,
    val description: String? = null,
) {
    fun toJson(): String = Json.encodeToString(this)
    fun toRequest(): TranslationRequest = TranslationRequest(
        categoryId = category?.id,
        description = description,
        translate = translate
    )
}

@Serializable
data class Translation(
    val id: Int,
    val categoryId: Int,
    val wordId: Int,
    val translate: String,
    val description: String? = null,
) {
    fun toJson(): String = Json.encodeToString(this)
}