package my.dictionary.free.domain.viewmodels.quiz

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class UserQuizzesViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase,
    private val wordsUseCase: WordsUseCase
) : ViewModel() {

    companion object {
        private val TAG = UserQuizzesViewModel::class.simpleName
    }

    private val _quizzesUIState = Channel<Quiz>()
    val quizzesUIState: StateFlow<Quiz> = _quizzesUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Quiz.empty())

    fun loadQuizzes(context: Context?) = flow<FetchDataState<Quiz>> {
        if (context == null) return@flow
        Log.d(TAG, "loadQuizzes()")
        emit(FetchDataState.StartLoadingState)
        getCreateQuizUseCase.getQuizzes(context)
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
                                quiz.words.add(word)
                                val quizWords = getCreateQuizUseCase.getWordsInQuiz(quiz._id ?: "")
                                    .firstOrNull() ?: emptyList()
                                quiz.quizWords.addAll(quizWords)
                                Log.d(TAG, "emit quiz ${quiz.name}")
                            }
                    }
                }
                emit(FetchDataState.DataState(quiz))
            }
    }

    fun deleteQuizzes(context: Context?, list: List<Quiz>?) = flow<FetchDataState<Nothing>> {
        if (context == null || list.isNullOrEmpty()) return@flow
        Log.d(TAG, "deleteQuizzes(${list.size})")
        emit(FetchDataState.StartLoadingState)
        val result = getCreateQuizUseCase.deleteQuizzes(list)
        emit(FetchDataState.FinishLoadingState)
        Log.d(TAG, "delete result is ${result.first}")
        if (!result.first) {
            val error =
                result.second ?: context.getString(R.string.error_delete_quiz)
            emit(FetchDataState.ErrorStateString(error))
        }
    }
}