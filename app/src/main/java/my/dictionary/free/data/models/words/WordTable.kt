package my.dictionary.free.data.models.words

import com.google.firebase.database.Exclude
import my.dictionary.free.data.models.words.variants.TranslationVariantTable
import my.dictionary.free.data.models.words.verb_tense.WordVerbTenseTable

data class WordTable(
    val _id: String? = null,
    val dictionaryId: String,
    val original: String,
    val type: Int,
    val phonetic: String? = null,
    val wordTagsIds: ArrayList<String> = arrayListOf(),
    val translations: ArrayList<TranslationVariantTable> = arrayListOf(),
    val verbTenses: ArrayList<WordVerbTenseTable> = arrayListOf()
) {
    companion object {
        const val _NAME = "word"
        const val _ID = "id"
        const val DICTIONARY_ID = "dictionaryId"
        const val ORIGINAL = "original"
        const val TYPE = "type"
        const val PHONETIC = "phonetic"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            DICTIONARY_ID to dictionaryId,
            ORIGINAL to original,
            TYPE to type,
            PHONETIC to phonetic,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is WordTable && _id == other._id && dictionaryId == other.dictionaryId && original == other.original && phonetic == other.phonetic && type == other.type
    }
}