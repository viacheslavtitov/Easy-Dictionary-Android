package my.dictionary.free.data.models.quiz

import com.google.firebase.database.Exclude

class QuizWordsTable(
    val _id: String? = null,
    val quizId: String,
    val wordId: String
) {
    companion object {
        const val _NAME = "words"
        const val _ID = "id"
        const val QUIZ_ID = "quizId"
        const val WORD_ID = "wordId"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            QUIZ_ID to quizId,
            WORD_ID to wordId
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is QuizWordsTable && _id == other._id && quizId == other.quizId && wordId == other.wordId
    }
}