package my.dictionary.free.domain.models.dictionary

data class Dictionary(
    val _id: String? = null,
    val userUUID: String,
    val langFrom: String,
    var langFromFull: String? = null,
    val langTo: String,
    var langToFull: String? = null,
    val dialect: String? = null
)