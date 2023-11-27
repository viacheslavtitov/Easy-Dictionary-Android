package my.dictionary.free.domain.models.dictionary

data class Dictionary(
    val _id: String? = null,
    val userUUID: String,
    val dictionaryFrom: DictionaryItem,
    val dictionaryTo: DictionaryItem,
    val dialect: String? = null
) {
    companion object {
        fun empty(): Dictionary = Dictionary(
            _id = null,
            userUUID = "",
            dictionaryFrom = DictionaryItem.empty(),
            dictionaryTo = DictionaryItem.empty(),
            dialect = null
        )
    }

    override fun toString(): String {
        return "id = $_id | userUUID = $userUUID | from = ${dictionaryFrom.lang} | to = ${dictionaryTo.lang} | dialect = $dialect"
    }
}

data class DictionaryItem(
    val lang: String,
    var langFull: String? = null,
    val flag: Flags? = null,
) {
    companion object {
        fun empty(): DictionaryItem = DictionaryItem(
            lang = "",
            langFull = null,
            flag = null,
        )
    }
}

data class Flags(
    val png: String,
    val svg: String
)