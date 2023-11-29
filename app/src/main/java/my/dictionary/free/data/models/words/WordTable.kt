package my.dictionary.free.data.models.words

import com.google.firebase.database.Exclude

data class WordTable(
    val _id: String? = null,
    val dictionaryId: String,
    val original: String,
    val transcription: String? = null
) {
    companion object {
        const val _NAME = "word"
        const val _ID = "id"
        const val DICTIONARY_ID = "dictionaryId"
        const val ORIGINAL = "original"
        const val TRANSCRIPTION = "transcription"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            DICTIONARY_ID to dictionaryId,
            ORIGINAL to original,
            TRANSCRIPTION to transcription,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is WordTable && _id == other._id && dictionaryId == other.dictionaryId && original == other.original && transcription == other.transcription
    }
}