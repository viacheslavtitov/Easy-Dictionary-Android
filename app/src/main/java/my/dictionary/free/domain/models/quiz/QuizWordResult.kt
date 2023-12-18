package my.dictionary.free.domain.models.quiz

data class QuizWordResult(
    val _id: String? = null,
    val quizId: String,
    val wordId: String,
    val originalWord: String,
    val answer: String? = null,
) {

    override fun toString(): String {
        return "id = $_id | quizId = $quizId | wordId = $wordId | originalWord = $originalWord | answer = $answer"
    }
}