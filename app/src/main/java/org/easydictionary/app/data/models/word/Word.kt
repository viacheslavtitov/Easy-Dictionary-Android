package org.easydictionary.app.data.models.word

import com.google.gson.annotations.SerializedName
import org.easydictionary.app.data.models.word.translation.TranslationRequest

data class WordRequest(
    @SerializedName("dictionary_id") val dictionaryId: Int,
    @SerializedName("original") val original: String,
    @SerializedName("phonetic") val phonetic: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("translations") val translations: List<TranslationRequest>
)