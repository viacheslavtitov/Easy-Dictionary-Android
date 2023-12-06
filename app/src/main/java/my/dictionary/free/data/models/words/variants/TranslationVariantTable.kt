package my.dictionary.free.data.models.words.variants

import com.google.firebase.database.Exclude

data class TranslationVariantTable(
    val _id: String? = null,
    val wordId: String,
    val translate: String,
    val categoryId: String? = null,
    val description: String? = null
) {
    companion object {
        const val _NAME = "translation"
        const val _ID = "id"
        const val WORD_ID = "wordId"
        const val CATEGORY_ID = "categoryId"
        const val TRANSLATE = "translate"
        const val DESCRIPTION = "description"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            WORD_ID to wordId,
            CATEGORY_ID to categoryId,
            TRANSLATE to translate,
            DESCRIPTION to description,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is TranslationVariantTable && _id == other._id && wordId == other.wordId && categoryId == other.categoryId && translate == other.translate && description == other.description
    }
}