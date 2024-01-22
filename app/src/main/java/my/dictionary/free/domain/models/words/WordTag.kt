package my.dictionary.free.domain.models.words

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordTag(
    val _id: String? = null,
    val userUUID: String,
    val tagName: String
) : Parcelable {
    companion object {
        fun empty(): WordTag = WordTag(
            _id = null,
            userUUID = "",
            tagName = ""
        )
    }

    override fun toString(): String {
        return "id = $_id | userUUID = $userUUID | tagName = $tagName"
    }
}
