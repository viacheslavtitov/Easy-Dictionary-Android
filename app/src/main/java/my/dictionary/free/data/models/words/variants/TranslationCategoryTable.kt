package my.dictionary.free.data.models.words.variants

import com.google.firebase.database.Exclude

class TranslationCategoryTable(
    val _id: String? = null,
    val userUUID: String,
    val categoryName: String
) {
    companion object {
        const val _NAME = "translationCategory"
        const val _ID = "id"
        const val USER_UUID = "userUUID"
        const val CATEGORY_NAME = "categoryName"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            USER_UUID to userUUID,
            CATEGORY_NAME to categoryName
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is TranslationCategoryTable && _id == other._id && userUUID == other.userUUID && categoryName == other.categoryName
    }
}