package my.dictionary.free.domain.models.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizResult(
    val _id: String? = null,
    val quizId: String,
    val wordsCount: Int,
    val rightAnswers: Int,
    val dateTime: String,
    val langPair: String,
    val quizName: String,
) : Parcelable {

    override fun toString(): String {
        return "id = $_id | quizId = $quizId | wordsCount = $wordsCount | rightAnswers = $rightAnswers | dateTime = $dateTime | langPair = $langPair | quizName = $quizName"
    }
}