package my.dictionary.free.domain.models.words.tags

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordTag(
    val _id: String? = null,
    val userUUID: String, var tag: String
) : Parcelable, Tag(tag, _id ?: "") {
    companion object {
        fun empty(): WordTag = WordTag(
            _id = null,
            userUUID = "",
            tag = ""
        )
    }

    override fun toString(): String {
        return "id = $_id | userUUID = $userUUID | tagName = $tag"
    }
}
