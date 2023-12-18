package my.dictionary.free.data.models.quiz

import com.google.firebase.database.Exclude

class QuizWordResultTable(
    val _id: String? = null,
    val quizId: String,
    val wordId: String,
    val originalWord: String,
    val answer: String? = null,
) {

    companion object {
        const val _NAME = "answers"
        const val _ID = "id"
        const val QUIZ_ID = "quizId"
        const val WORD_ID = "wordId"
        const val ORIGINAL_WORD = "originalWord"
        const val ANSWER = "answer"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            QUIZ_ID to quizId,
            WORD_ID to wordId,
            ORIGINAL_WORD to originalWord,
            ANSWER to answer,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is QuizWordResultTable && _id == other._id && quizId == other.quizId && wordId == other.wordId && originalWord == other.originalWord && answer == other.answer
    }
}