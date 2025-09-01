package org.easydictionary.app.data.models.word.translation

import com.google.gson.annotations.SerializedName

data class TranslationRequest(
    @SerializedName("category_id") val categoryId: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("translate") val translate: String
)