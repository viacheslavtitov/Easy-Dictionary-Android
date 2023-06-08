package my.dictionary.free.domain.models.dictionary

data class Dictionary(
    val _id: String? = null,
    val userUUID: String,
    val dictionaryFrom: DictionaryItem,
    val dictionaryTo: DictionaryItem,
    val dialect: String? = null
)

data class DictionaryItem(
    val lang: String,
    var langFull: String? = null,
    val flag: Flags? = null,
)

data class Flags(
    val png: String,
    val svg: String
)