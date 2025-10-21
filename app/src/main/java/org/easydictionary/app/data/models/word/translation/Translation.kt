package org.easydictionary.app.data.models.word.translation

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.data.models.category.CategoryResponse
import org.easydictionary.app.domain.models.translation.Translation

data class TranslationRequest(
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("translate") val translate: String
)
data class EditTranslationRequest(
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("translate") val translate: String,
    @SerializedName("id") val id: Int,
    @SerializedName("word_id") val wordId: Int
)

data class Translation(
    @SerializedName("id") val id: Int,
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("translate") val translate: String
) {
    fun toDomain() = Translation(
        id = id,
        categoryId = categoryId,
        description = description,
        translate = translate
    )
}

data class TranslationWithCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("category") val category: CategoryResponse? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("translate") val translate: String
) {
    fun toDomain() = org.easydictionary.app.domain.models.translation.TranslationWithCategory(
        id = id,
        category = category?.toDomain(),
        description = description,
        translate = translate
    )
}