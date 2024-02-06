package my.dictionary.free.domain.models.words.tags

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import my.dictionary.free.domain.models.words.variants.TranslationCategory

@Parcelize
data class CategoryTag(
    val translationCategory: TranslationCategory, var tag: String
) : Parcelable, Tag(tag, translationCategory._id ?: "")