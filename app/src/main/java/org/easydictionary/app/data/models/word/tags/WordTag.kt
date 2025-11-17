package org.easydictionary.app.data.models.word.tags

import com.google.gson.annotations.SerializedName

data class WordTagRequest(
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("name") val name: String
)

data class WordTagToWordRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("name") val name: String
)

data class WordTagEntity(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("name") val name: String
) {
    fun toDomain(wordId: Int? = null) = org.easydictionary.app.domain.models.word.WordTag(
        id = id,
        dictionaryId = dictionaryId,
        name = name,
        wordId = wordId
    )
}