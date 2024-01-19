package my.dictionary.free.domain.viewmodels.quiz

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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

    private val _clearActionModeUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val clearActionModeUIState: StateFlow<Boolean> = _clearActionModeUIState.asStateFlow()

    private val _shouldClearQuizzesUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val shouldClearQuizzesUIState: StateFlow<Boolean> = _shouldClearQuizzesUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState = Channel<String>()
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    fun loadQuizzes(context: Context?) {
        if (context == null) return
        Log.d(TAG, "loadQuizzes()")
        viewModelScope.launch {
            getCreateQuizUseCase.getQuizzes(context)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.send(
                        it.message ?: context.getString(R.string.unknown_error))
                }
                .onStart {
                    Log.d(TAG, "loadQuizzes onStart")
                    _shouldClearQuizzesUIState.value = true
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "loadQuizzes onCompletion")
                    _shouldClearQuizzesUIState.value = false
                    _loadingUIState.value = false
                }
                .collect { quiz ->
                    Log.d(TAG, "collect quiz $quiz")
                    loadWords(context, quiz)
                }
        }
    }

    private fun loadWords(context: Context, quiz: Quiz) {
        Log.d(TAG, "loadWords(${quiz.name})")
        viewModelScope.launch {
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
            val quizWords =
                getCreateQuizUseCase.getWordsInQuiz(quiz._id ?: "").firstOrNull() ?: emptyList()
            quiz.quizWords.addAll((quizWords))
            Log.d(TAG, "emit quiz ${quiz.name}")
            _quizzesUIState.send(quiz)
        }
    }

    fun deleteQuizzes(context: Context?, list: List<Quiz>?) {
        if (context == null || list.isNullOrEmpty()) return
        Log.d(TAG, "deleteQuizzes(${list.size})")
        viewModelScope.launch {
            _loadingUIState.value = true
            val result = getCreateQuizUseCase.deleteQuizzes(list)
            _clearActionModeUIState.value = true
            _loadingUIState.value = false
            Log.d(TAG, "delete result is ${result.first}")
            if (!result.first) {
                val error =
                    result.second ?: context.getString(R.string.error_delete_dictionary)
                _displayErrorUIState.send(error)
            } else {
                _shouldClearQuizzesUIState.value = true
            }
        }.invokeOnCompletion {
            loadQuizzes(context)
        }
    }
}