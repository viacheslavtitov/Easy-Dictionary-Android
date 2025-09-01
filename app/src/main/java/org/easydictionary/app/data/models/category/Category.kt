package org.easydictionary.app.data.models.category

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.domain.models.category.Category

data class CategoryRequest(
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("name") val name: String
)

data class CategoryResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("name") val name: String
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            dictionaryId = dictionaryId,
            name = name,
        )
    }
}