package org.easydictionary.app.data.models.dictionary

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.data.models.category.CategoryDictionaryResponse
import org.easydictionary.app.data.models.language.LanguageResponse
import org.easydictionary.app.data.models.word.tags.WordTagEntity
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetail
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.models.dictionary.WordTypeSelectableItem

data class DictionaryResponse(
    @SerializedName("dialect") val dialect: String? = null,
    @SerializedName("id") val id: Int,
    @SerializedName("lang_from_id") val langFromId: Int,
    @SerializedName("lang_to_id") val langToId: Int
) {
    fun toDomain(): Dictionary {
        return Dictionary(
            id = id,
            dialect = dialect,
            langFromId = langFromId,
            langToId = langToId
        )
    }
}

data class DictionaryRequest(
    @SerializedName("dialect") val dialect: String? = null,
    @SerializedName("lang_from_id") val langFromId: Int,
    @SerializedName("lang_to_id") val langToId: Int
)

data class DictionaryEditRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("dialect") val dialect: String? = null
)

data class DictionaryDetailShortResponse(
    @SerializedName("dialect") val dialect: String? = null,
    @SerializedName("id") val id: Int,
    @SerializedName("lang_from") val langFrom: LanguageResponse,
    @SerializedName("lang_to") val langTo: LanguageResponse,
    @SerializedName("word_tags_count") val wordTagsCount: Int,
    @SerializedName("words_count") val wordsCount: Int,
    @SerializedName("quiz_count") val quizCount: Int,
) {
    fun toDomain(): DictionaryDetailShort {
        return DictionaryDetailShort(
            id = id,
            dialect = dialect,
            langFrom = langFrom.toDomain(),
            langTo = langTo.toDomain(),
            wordTagsCount = wordTagsCount,
            wordsCount = wordsCount,
            quizCount = quizCount
        )
    }
}

data class DictionaryDetailResponse(
    @SerializedName("dialect") val dialect: String? = null,
    @SerializedName("id") val id: Int,
    @SerializedName("lang_from") val langFrom: LanguageResponse,
    @SerializedName("lang_to") val langTo: LanguageResponse,
    @SerializedName("categories") val categories: List<CategoryDictionaryResponse> = emptyList(),
    @SerializedName("tags") val tags: List<WordTagEntity> = emptyList(),
    @SerializedName("word_types") val wordTypes: List<String> = emptyList(),
) {
    fun toDomain(): DictionaryDetail {
        return DictionaryDetail(
            id = id,
            dialect = dialect,
            langFrom = langFrom.toDomain(),
            langTo = langTo.toDomain(),
            categories = categories.map { it.toDomain() },
            tags = tags.map { it.toDomain() },
            wordTypes = wordTypes.map { WordTypeSelectableItem(it) }
        )
    }
}