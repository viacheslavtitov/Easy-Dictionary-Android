package my.dictionary.free.data.models.dictionary

import com.google.firebase.database.Exclude

class VerbTenseTable(
    val _id: String? = null,
    val name: String,
) {
    companion object {
        const val _NAME = "tenses"
        const val NAME = "name"
        const val _ID = "id"
    }
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            NAME to name
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is VerbTenseTable && _id == other._id && name == other.name
    }
}