package org.easydictionary.app.data.models.category

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.models.category.CategoryDictionary

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

data class CategoryDictionaryResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
) {
    fun toDomain(): CategoryDictionary {
        return CategoryDictionary(
            id = id,
            name = name,
        )
    }
}