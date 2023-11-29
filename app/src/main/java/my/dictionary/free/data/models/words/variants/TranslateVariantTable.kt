package my.dictionary.free.data.models.words.variants

import com.google.firebase.database.Exclude

data class TranslateVariantTable(
    val _id: String? = null,
    val wordId: String,
    val translate: String,
    val description: String? = null
) {
    companion object {
        const val _NAME = "translate"
        const val _ID = "id"
        const val WORD_ID = "wordId"
        const val TRANSLATE = "translate"
        const val DESCRIPTION = "description"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            WORD_ID to wordId,
            TRANSLATE to translate,
            DESCRIPTION to description,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is TranslateVariantTable && _id == other._id && wordId == other.wordId && translate == other.translate && description == other.description
    }
}