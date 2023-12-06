package my.dictionary.free.domain.models.words.variants

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class TranslationVariant(
    val _id: String? = null,
    val wordId: String,
    val categoryId: String? = null,
    val translation: String,
    val example: String? = null,
) : Parcelable {

    @IgnoredOnParcel
    var category: TranslationCategory? = null

    companion object {
        fun empty(): TranslationVariant = TranslationVariant(
            _id = null,
            wordId = "",
            categoryId = null,
            example = null,
            translation = ""
        )
    }

    fun copyWithNewWordId(_wordId: String) = TranslationVariant(
        _id = _id,
        wordId = _wordId,
        categoryId = categoryId,
        example = example,
        translation = translation
    )

    override fun toString(): String {
        return "id = $_id | wordId = $wordId | categoryId = $categoryId | example = $example | translation = $translation"
    }
}
