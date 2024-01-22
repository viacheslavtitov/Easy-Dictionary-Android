package my.dictionary.free.domain.models.dictionary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import my.dictionary.free.domain.models.words.WordTag

@Parcelize
data class Dictionary(
    val _id: String? = null,
    val userUUID: String,
    val dictionaryFrom: DictionaryItem,
    val dictionaryTo: DictionaryItem,
    val dialect: String? = null,
    val tags: MutableList<WordTag> = mutableListOf()
) : Parcelable {
    companion object {
        fun empty(): Dictionary = Dictionary(
            _id = null,
            userUUID = "",
            dictionaryFrom = DictionaryItem.empty(),
            dictionaryTo = DictionaryItem.empty(),
            dialect = null,
            tags = mutableListOf()
        )
    }

    override fun toString(): String {
        return "id = $_id | userUUID = $userUUID | from = ${dictionaryFrom.lang} | to = ${dictionaryTo.lang} | dialect = $dialect | tags = ${tags.size}"
    }
}
@Parcelize
data class DictionaryItem(
    val lang: String,
    var langFull: String? = null,
    val flag: Flags? = null,
) : Parcelable {
    companion object {
        fun empty(): DictionaryItem = DictionaryItem(
            lang = "",
            langFull = null,
            flag = null,
        )
    }

    override fun toString(): String {
        return "$langFull"
    }
}
@Parcelize
data class Flags(
    val png: String,
    val svg: String
) : Parcelable