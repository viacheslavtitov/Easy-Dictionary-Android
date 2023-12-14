package my.dictionary.free.domain.usecases.quize

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.models.quiz.QuizTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetCreateQuizUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase,
    private val preferenceUtils: PreferenceUtils
) {
    companion object {
        private val TAG = GetCreateQuizUseCase::class.simpleName
    }

    suspend fun createQuiz(quiz: Quiz): Triple<Boolean, String?, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Triple(
                false,
                null,
                null
            )
        val dictionaryId = quiz.dictionary?._id ?: return Triple(false, null, null)
        return databaseRepository.createQuiz(
            userId, QuizTable(
                _id = null,
                userId = userId,
                dictionaryId = dictionaryId,
                name = quiz.name,
                timeInSeconds = quiz.timeInSeconds
            )
        )
    }

    suspend fun getQuizzes(context: Context): Flow<Quiz> {
        Log.d(TAG, "getQuizzes()")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getQuizzesByUserUUID(userId)
                .map {
                    return@map Pair(
                        it,
                        getCreateDictionaryUseCase.getDictionaryById(context, it.dictionaryId)
                            .firstOrNull()
                    )
                }
                .map { pair ->
                    val quize = pair.first
                    val dictionary = pair.second
                    return@map Quiz(
                        _id = quize._id,
                        userId = quize.userId,
                        dictionary = dictionary,
                        name = quize.name,
                        timeInSeconds = quize.timeInSeconds,
                    )
                }
        }
    }

    suspend fun getWordsIdsForQuiz(quizId: String): Flow<List<String>> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getQuizWords(userId, quizId).map { quizWord ->
                return@map quizWord.map {
                    it.wordId
                }
            }
        }
    }

    suspend fun addWordToQuiz(quizId: String, wordId: String): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.addWordToQuiz(userId, quizId, wordId)
    }

    suspend fun deleteQuizzes(quizzes: List<Quiz>): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        val requestDeleteQuizeIds = mutableListOf<String>()
        quizzes.forEach { if (it._id != null) requestDeleteQuizeIds.add(it._id) }
        return databaseRepository.deleteQuizzes(userId, requestDeleteQuizeIds)
    }

    suspend fun deleteQuiz(quiz: Quiz): Pair<Boolean, String?> {
        return deleteQuizzes(listOf(quiz))
    }
}