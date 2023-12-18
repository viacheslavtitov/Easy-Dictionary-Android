package my.dictionary.free.data.models.quiz

import com.google.firebase.database.Exclude

class QuizResultTable(
    val _id: String? = null,
    val quizId: String,
    val wordsCount: Int,
    val rightAnswers: Int,
    val unixDateTimeStamp: Long,
) {

    companion object {
        const val _NAME = "result"
        const val _ID = "id"
        const val QUIZ_ID = "quizId"
        const val WORDS_COUNT = "wordsCount"
        const val RIGHT_ANSWERS = "rightAnswers"
        const val UNIX_DATE_TIME_STAMP = "unixDateTimeStamp"
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            _ID to _id,
            QUIZ_ID to quizId,
            WORDS_COUNT to wordsCount,
            RIGHT_ANSWERS to rightAnswers,
            UNIX_DATE_TIME_STAMP to unixDateTimeStamp,
        )
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is QuizResultTable && _id == other._id && quizId == other.quizId && wordsCount == other.wordsCount && rightAnswers == other.rightAnswers && unixDateTimeStamp == other.unixDateTimeStamp
    }
}