package my.dictionary.free.data.models.words.verb_tense

import com.google.firebase.database.Exclude

data class WordVerbTenseTable(
    val _id: String? = null,
    val tenseId: String,
    val wordId: String,
    val value: String
) {
    companion object {
        const val _NAME = "tenses"
        const val _ID = "id"
        const val TENSE_ID = "tenseId"
        const val WORD_ID = "wordId"
        const val VALUE = "value"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            TENSE_ID to tenseId,
            WORD_ID to wordId,
            VALUE to value
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is WordVerbTenseTable && _id == other._id && tenseId == other.tenseId && wordId == other.wordId && value == other.value
    }
}