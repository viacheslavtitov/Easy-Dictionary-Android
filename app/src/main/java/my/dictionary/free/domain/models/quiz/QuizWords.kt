package my.dictionary.free.domain.models.quiz

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizWords(
    val _id: String? = null,
    val quizId: String,
    val wordId: String
) : Parcelable {
}