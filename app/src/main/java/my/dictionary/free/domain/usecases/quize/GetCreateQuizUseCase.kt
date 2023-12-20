package my.dictionary.free.domain.usecases.quize

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.models.quiz.QuizResultTable
import my.dictionary.free.data.models.quiz.QuizTable
import my.dictionary.free.data.models.quiz.QuizWordResultTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.quiz.QuizResult
import my.dictionary.free.domain.models.quiz.QuizWordResult
import my.dictionary.free.domain.models.quiz.QuizWords
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import my.dictionary.free.domain.utils.hasOreo
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date
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

    suspend fun updateQuiz(quiz: Quiz): Boolean {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        val dictionaryId = quiz.dictionary?._id ?: return false
        return databaseRepository.updateQuiz(
            userId, QuizTable(
                _id = quiz._id,
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
                    val quiz = pair.first
                    val dictionary = pair.second
                    return@map Quiz(
                        _id = quiz._id,
                        userId = quiz.userId,
                        dictionary = dictionary,
                        name = quiz.name,
                        timeInSeconds = quiz.timeInSeconds,
                    )
                }
        }
    }

    suspend fun getQuiz(context: Context, quizId: String): Flow<Quiz> {
        Log.d(TAG, "getQuiz $quizId")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getQuizById(userId, quizId)
                .map {
                    return@map Pair(
                        it,
                        getCreateDictionaryUseCase.getDictionaryById(context, it.dictionaryId)
                            .firstOrNull()
                    )
                }
                .map { pair ->
                    val quiz = pair.first
                    val dictionary = pair.second
                    return@map Quiz(
                        _id = quiz._id,
                        userId = quiz.userId,
                        dictionary = dictionary,
                        name = quiz.name,
                        timeInSeconds = quiz.timeInSeconds,
                    )
                }
        }
    }

    suspend fun getHistoriesOfQuiz(quiz: Quiz): List<QuizResult> {
        Log.d(TAG, "getQuiz ${quiz._id}")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return arrayListOf()
        } else {
            return databaseRepository.getHistoriesOfQuiz(userId, quiz._id ?: "").firstOrNull()
                ?.map {
                    val dateTime = if (hasOreo()) {
                        DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(it.unixDateTimeStamp))
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        val date = Date(it.unixDateTimeStamp)
                        sdf.format(date)
                    }
                    val langPair =
                        "${quiz.dictionary?.dictionaryFrom?.langFull} - ${quiz.dictionary?.dictionaryTo?.langFull}"
                    return@map QuizResult(
                        _id = it._id,
                        quizId = it.quizId,
                        wordsCount = it.wordsCount,
                        rightAnswers = it.rightAnswers,
                        dateTime = dateTime,
                        langPair = langPair,
                        quizName = quiz.name
                    )
                } ?: arrayListOf()
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

    suspend fun getWordsInQuiz(quizId: String): Flow<List<QuizWords>> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getQuizWords(userId, quizId).map { quizWord ->
                return@map quizWord.map {
                    QuizWords(
                        _id = it._id,
                        quizId = it.quizId,
                        wordId = it.wordId,
                    )
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
        val requestDeleteQuizIds = mutableListOf<String>()
        quizzes.forEach { if (it._id != null) requestDeleteQuizIds.add(it._id) }
        return databaseRepository.deleteQuizzes(userId, requestDeleteQuizIds)
    }

    suspend fun deleteQuiz(quiz: Quiz): Pair<Boolean, String?> {
        return deleteQuizzes(listOf(quiz))
    }

    suspend fun deleteWordsFromQuiz(quizId: String, requestDeleteWordsIds: List<String>): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.deleteWordFromQuiz(userId, quizId, requestDeleteWordsIds)
    }

    suspend fun saveQuizResult(
        quizId: String,
        wordsCount: Int,
        rightAnswers: Int,
        unixDateTimeStamp: Long
    ): Triple<Boolean, String?, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Triple(
                false,
                null,
                null
            )
        return databaseRepository.saveQuizResults(
            userId, QuizResultTable(
                _id = null,
                quizId = quizId,
                wordsCount = wordsCount,
                rightAnswers = rightAnswers,
                unixDateTimeStamp = unixDateTimeStamp
            )
        )
    }

    suspend fun addWordToQuizResult(
        quizResultId: String,
        quizWordResult: QuizWordResult
    ): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.addWordToQuizResult(
            userId, quizResultId, QuizWordResultTable(
                _id = null,
                quizId = quizWordResult.quizId,
                wordId = quizWordResult.wordId,
                originalWord = quizWordResult.originalWord,
                answer = quizWordResult.answer
            )
        )
    }

    suspend fun deleteQuizResult(quizId: String, quizResultId: String): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.deleteQuizResult(userId, quizId, quizResultId)
    }
}