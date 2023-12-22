package my.dictionary.free.data.models.quiz

import com.google.firebase.database.Exclude

class QuizTable(
    val _id: String? = null,
    val userId: String,
    val dictionaryId: String,
    val name: String,
    val reversed: Boolean,
    val timeInSeconds: Int,
) {
    companion object {
        const val _NAME = "quiz"
        const val _ID = "id"
        const val USER_ID = "userId"
        const val DICTIONARY_ID = "dictionaryId"
        const val NAME = "name"
        const val TIME_IN_SECONDS = "timeInSeconds"
        const val REVERSED = "reversed"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            USER_ID to userId,
            DICTIONARY_ID to dictionaryId,
            NAME to name,
            REVERSED to reversed,
            TIME_IN_SECONDS to timeInSeconds,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is QuizTable && _id == other._id && dictionaryId == other.dictionaryId && name == other.name && timeInSeconds == other.timeInSeconds && userId == other.userId && reversed == other.reversed
    }
}