package org.easydictionary.app.data.models.word

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.data.models.word.tags.WordTagToWordRequest
import org.easydictionary.app.data.models.word.translation.Translation
import org.easydictionary.app.data.models.word.translation.TranslationRequest
import org.easydictionary.app.data.models.word.translation.TranslationWithCategory

data class WordRequest(
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("original") val original: String,
    @SerializedName("phonetic") val phonetic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("translations") val translations: List<TranslationRequest>,
    @SerializedName("tags") val tags: List<WordTagToWordRequest>,
)

data class WordUpdateRequest(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("original") val original: String,
    @SerializedName("phonetic") val phonetic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("tags") val tags: List<Int>,
)

data class Word(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("original") val original: String,
    @SerializedName("phonetic") val phonetic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("translations") val translations: List<Translation>
) {
    fun toDomain() = org.easydictionary.app.domain.models.word.Word(
        id = id,
        dictionaryId = dictionaryId,
        original = original,
        phonetic = phonetic,
        type = type,
        translations = translations.map { it.toDomain() }
    )
}

data class WordDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("original") val original: String,
    @SerializedName("phonetic") val phonetic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("translations") val translations: List<TranslationWithCategory>? = null
) {
    fun toDomain() = org.easydictionary.app.domain.models.word.WordDetail(
        id = id,
        dictionaryId = dictionaryId,
        original = original,
        phonetic = phonetic,
        type = type,
        translations = translations?.map { it.toDomain() } ?: emptyList()
    )
}

data class WordsResponse(
    @SerializedName("next_last_id") val latestId: Int,
    @SerializedName("has_more") val hasMore: Boolean,
    @SerializedName("words") val words: List<WordDetail>? = emptyList()
)