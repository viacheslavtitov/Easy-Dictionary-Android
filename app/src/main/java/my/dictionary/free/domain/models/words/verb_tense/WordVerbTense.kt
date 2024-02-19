package my.dictionary.free.domain.models.words.verb_tense

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordVerbTense(
    val _id: String? = null,
    val tenseId: String,
    val wordId: String,
    val value: String
) : Parcelable {

    companion object {
        fun empty() = WordVerbTense(
            _id = null,
            tenseId = "",
            wordId = "",
            value = ""
        )
    }

    override fun toString(): String {
        return "_id = $_id | tenseId = $tenseId | wordId = $wordId | value = $value"
    }
}