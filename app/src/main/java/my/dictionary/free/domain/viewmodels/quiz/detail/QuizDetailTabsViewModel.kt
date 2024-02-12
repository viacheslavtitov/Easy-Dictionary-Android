package my.dictionary.free.domain.viewmodels.quiz.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class QuizDetailTabsViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase,
    private val wordsUseCase: WordsUseCase,
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase
) : ViewModel() {
    companion object {
        private val TAG = QuizDetailTabsViewModel::class.simpleName
    }

    private var quizModel: Quiz? = null

    fun loadQuiz(context: Context?, quizId: String?) = flow<FetchDataState<Quiz>> {
        if (context == null) return@flow
        if (quizId == null) return@flow
        Log.d(TAG, "loadQuiz($quizId)")
        getCreateQuizUseCase.getQuiz(context, quizId)
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "loadQuizzes onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .map {
                Pair(it, getCreateQuizUseCase.getWordsIdsForQuiz(it._id ?: "").firstOrNull())
            }.collect { pair ->
                val quiz = pair.first
                val wordIds = pair.second
                wordIds?.forEach { wordId ->
                    quiz.dictionary?._id?.let { dictionaryId ->
                        wordsUseCase.getWordById(dictionaryId, wordId)
                            .catch {
                                Log.d(TAG, "catch ${it.message}")
                                emit(FetchDataState.ErrorState(it))
                            }
                            .collect { word ->
                                Log.d(TAG, "collect word $word, for ${quiz.name}")
                                for (translation in word.translates) {
                                    if(translation.categoryId != null) {
                                        translation.category = getCreateTranslationCategoriesUseCase.getDirectCategoryById(translation.categoryId)
                                    }
                                }
                                quiz.histories.addAll(getCreateQuizUseCase.getHistoriesOfQuiz(quiz))
                                quiz.words.add(word)
                                val quizWords = getCreateQuizUseCase.getWordsInQuiz(quiz._id ?: "")
                                    .firstOrNull() ?: emptyList()
                                quiz.quizWords.addAll(quizWords)
                                Log.d(TAG, "emit quiz ${quiz.name}")
                            }
                    }
                }
                quizModel = quiz
                emit(FetchDataState.DataState(quiz))
            }
    }

    fun getQuiz() = quizModel

}