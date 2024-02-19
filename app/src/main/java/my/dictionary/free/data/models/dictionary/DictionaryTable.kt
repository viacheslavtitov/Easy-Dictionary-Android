package my.dictionary.free.data.models.dictionary

import com.google.firebase.database.Exclude

class DictionaryTable(
    val _id: String? = null,
    val userUUID: String,
    val langFrom: String,
    val langTo: String,
    val tenses: List<VerbTenseTable>,
    val dialect: String? = null
) {
    companion object {
        const val _NAME = "dictionary"
        const val _ID = "id"
        const val USER_UUID = "userUUID"
        const val LANG_FROM = "langFrom"
        const val LANG_TO = "langTo"
        const val DIALECT = "dialect"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            USER_UUID to userUUID,
            LANG_FROM to langFrom,
            LANG_TO to langTo,
            DIALECT to dialect,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is DictionaryTable && _id == other._id && userUUID == other.userUUID && langFrom == other.langFrom && langTo == other.langTo && dialect == other.dialect
    }
}