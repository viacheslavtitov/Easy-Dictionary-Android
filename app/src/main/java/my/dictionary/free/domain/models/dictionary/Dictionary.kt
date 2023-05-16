package my.dictionary.free.domain.models.dictionary

data class Dictionary(
    val _id: String? = null,
    val userUUID: String,
    val langFrom: String,
    val langTo: String,
    val dialect: String? = null
)