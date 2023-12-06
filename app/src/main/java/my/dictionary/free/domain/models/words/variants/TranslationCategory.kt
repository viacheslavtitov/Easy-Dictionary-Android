package my.dictionary.free.domain.models.words.variants

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TranslationCategory(
    val _id: String? = null,
    val userUUID: String,
    val categoryName: String
) : Parcelable {
    companion object {
        fun empty(): TranslationCategory = TranslationCategory(
            _id = null,
            userUUID = "",
            categoryName = ""
        )
    }

    override fun toString(): String {
        return "id = $_id | userUUID = $userUUID | categoryName = $categoryName"
    }
}
