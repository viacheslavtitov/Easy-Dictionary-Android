package my.dictionary.free.domain.models.words

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.models.words.verb_tense.WordVerbTense

@Parcelize
data class Word(
    val _id: String? = null,
    val dictionaryId: String,
    val original: String,
    val type: Int,
    val phonetic: String? = null,
    val translates: List<TranslationVariant>,
    val tags: ArrayList<WordTag>,
    val tenses: ArrayList<WordVerbTense>,
) : Parcelable {
    companion object {
        fun empty() = Word(
            _id = null,
            dictionaryId = "",
            type = 0,
            original = "",
            phonetic = null,
            translates = emptyList(),
            tags = arrayListOf(),
            tenses = arrayListOf()
        )
    }

    override fun toString(): String {
        return "original = $original | phonetic = $phonetic | dictionaryId = $dictionaryId | type = $type | translates = ${translates.size} | tags = ${tags.size} | tags = ${tenses.size}"
    }
}