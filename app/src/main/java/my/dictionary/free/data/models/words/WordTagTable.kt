package my.dictionary.free.data.models.words

import com.google.firebase.database.Exclude

class WordTagTable(
    val _id: String? = null,
    val userUUID: String,
    val tagName: String
) {
    companion object {
        const val _NAME = "wordTag"
        const val _ID = "id"
        const val USER_UUID = "userUUID"
        const val TAG_NAME = "tagName"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            USER_UUID to userUUID,
            TAG_NAME to tagName
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is WordTagTable && _id == other._id && userUUID == other.userUUID && tagName == other.tagName
    }
}