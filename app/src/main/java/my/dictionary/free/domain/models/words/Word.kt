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
    val translates: List<TranslationVariant>
) : Parcelable {
    companion object {
        fun empty() = Word(
            _id = null,
            dictionaryId = "",
            type = 0,
            original = "",
            phonetic = null,
            translates = emptyList()
        )
    }

    fun copyWithNewTranslations(translateList: List<TranslationVariant>) = Word(
        _id = _id,
        dictionaryId = dictionaryId,
        original = original,
        type = type,
        phonetic = phonetic,
        translates = translateList
    )

    override fun toString(): String {
        return "$original | $phonetic"
    }
}