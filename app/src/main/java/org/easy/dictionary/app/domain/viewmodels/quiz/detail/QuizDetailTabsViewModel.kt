package org.easy.dictionary.app.domain.viewmodels.quiz.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.easy.dictionary.app.domain.models.quiz.Quiz
import org.easy.dictionary.app.domain.usecases.quize.GetCreateQuizUseCase
import org.easy.dictionary.app.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import org.easy.dictionary.app.domain.usecases.words.WordsUseCase
import org.easy.dictionary.app.view.FetchDataState
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
        emit(FetchDataState.StartLoadingState)
        getCreateQuizUseCase.getQuiz(context, quizId)
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
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
                                quiz.words.add(word)
                                Log.d(TAG, "emit quiz ${quiz.name}")
                            }
                    }
                }
                quiz.histories.addAll(getCreateQuizUseCase.getHistoriesOfQuiz(quiz))
                val quizWords = getCreateQuizUseCase.getWordsInQuiz(quiz._id ?: "")
                    .firstOrNull() ?: emptyList()
                quiz.quizWords.addAll(quizWords)
                quizModel = quiz
                emit(FetchDataState.DataState(quiz))
                emit(FetchDataState.FinishLoadingState)
            }
    }

    fun getQuiz() = quizModel

}