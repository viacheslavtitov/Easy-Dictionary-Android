package my.dictionary.free.domain.viewmodels.quiz.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import javax.inject.Inject

@HiltViewModel
class QuizDetailTabsViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase,
    private val wordsUseCase: WordsUseCase
) : ViewModel() {
    companion object {
        private val TAG = QuizDetailTabsViewModel::class.simpleName
    }

    private val _displayErrorUIState = Channel<String>()
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _quizUIState = Channel<Quiz>()
    val quizUIState: StateFlow<Quiz> = _quizUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Quiz.empty())

    private var quizModel: Quiz? = null

    fun loadQuiz(context: Context?, quizId: String?) {
        if (context == null) return
        if (quizId == null) return
        Log.d(TAG, "loadQuizzes()")
        viewModelScope.launch {
            getCreateQuizUseCase.getQuiz(context, quizId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.send(
                        it.message ?: context.getString(R.string.unknown_error))
                }
                .onStart {
                    Log.d(TAG, "loadQuizzes onStart")
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "loadQuizzes onCompletion")
                    _loadingUIState.value = false
                }
                .collect { quiz ->
                    Log.d(TAG, "collect quiz $quiz")
                    loadWordsAndHistory(context, quiz)
                }
        }
    }

    private fun loadWordsAndHistory(context: Context, quiz: Quiz) {
        viewModelScope.launch {
            Log.d(TAG, "loadWords(${quiz.name})")
            getCreateQuizUseCase.getWordsIdsForQuiz(quiz._id ?: "").firstOrNull()
                ?.forEach { id ->
                    quiz.dictionary?._id?.let { dictionaryId ->
                        wordsUseCase.getWordById(dictionaryId, id)
                            .catch {
                                Log.d(TAG, "catch ${it.message}")
                                _displayErrorUIState.send(
                                    it.message ?: context.getString(R.string.unknown_error))
                            }
                            .onStart {
                                Log.d(TAG, "loadWords onStart")
                            }
                            .onCompletion {
                                Log.d(TAG, "loadWords onCompletion")
                            }
                            .collect { word ->
                                Log.d(TAG, "collect word $word, for ${quiz.name}")
                                quiz.words.add(word)
                            }
                    }
                }
            quiz.histories.addAll(getCreateQuizUseCase.getHistoriesOfQuiz(quiz))
            val quizWords = getCreateQuizUseCase.getWordsInQuiz(quiz._id ?: "").firstOrNull() ?: emptyList()
            quiz.quizWords.addAll((quizWords))
            Log.d(TAG, "emit quiz ${quiz.name}")
            quizModel = quiz
            _quizUIState.send(quiz)
        }
    }

    fun getQuiz() = quizModel

}