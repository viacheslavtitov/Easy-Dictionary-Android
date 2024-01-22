package my.dictionary.free.domain.models.words

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import my.dictionary.free.domain.models.words.variants.TranslationVariant

@Parcelize
data class Word(
    val _id: String? = null,
    val dictionaryId: String,
    val original: String,
    val type: Int,
    val phonetic: String? = null,
    val translates: List<TranslationVariant>,
    val tags: List<WordTag>
) : Parcelable {
    companion object {
        fun empty() = Word(
            _id = null,
            dictionaryId = "",
            type = 0,
            original = "",
            phonetic = null,
            translates = emptyList(),
            tags = emptyList()
        )
    }

    override fun toString(): String {
        return "original = $original | phonetic = $phonetic | dictionaryId = $dictionaryId | type = $type | translates = ${translates.size} | tags = ${tags.size}"
    }
}