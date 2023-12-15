package my.dictionary.free.domain.viewmodels.quiz.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import javax.inject.Inject

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase,
    private val wordsUseCase: WordsUseCase
) : ViewModel() {
    companion object {
        private val TAG = QuizDetailViewModel::class.simpleName
    }

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _shouldClearWordsUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val shouldClearWordsUIState: StateFlow<Boolean> = _shouldClearWordsUIState.asStateFlow()

    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    private val _durationUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val durationUIState: StateFlow<String> = _durationUIState.asStateFlow()

    private val _dictionaryUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val dictionaryUIState: StateFlow<String> = _dictionaryUIState.asStateFlow()

    val wordUIState: MutableSharedFlow<Word> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var quizModel: Quiz? = null

    fun loadQuiz(context: Context?, quizId: String?) {
        if (context == null) return
        if (quizId == null) return
        Log.d(TAG, "loadQuizzes()")
        viewModelScope.launch {
            getCreateQuizUseCase.getQuiz(context, quizId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.value =
                        it.message ?: context.getString(R.string.unknown_error)
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
                    loadWords(context, quiz)
                }
        }
    }

    private fun loadWords(context: Context, quiz: Quiz) {
        Log.d(TAG, "loadWords(${quiz.name})")
        viewModelScope.launch {
            _shouldClearWordsUIState.value = true
            getCreateQuizUseCase.getWordsIdsForQuiz(quiz._id ?: "").firstOrNull()
                ?.forEach { id ->
                    quiz.dictionary?._id?.let { dictionaryId ->
                        wordsUseCase.getWordById(dictionaryId, id)
                            .catch {
                                Log.d(TAG, "catch ${it.message}")
                                _displayErrorUIState.value =
                                    it.message ?: context.getString(R.string.unknown_error)
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
                                wordUIState.tryEmit(word)
                            }
                    }
                }
            Log.d(TAG, "emit quiz ${quiz.name}")
            quizModel = quiz
            _shouldClearWordsUIState.value = false
            _nameUIState.value = quiz.name
            _durationUIState.value = context.getString(R.string.seconds_value, quiz.timeInSeconds)
            quiz.dictionary?.let {dict ->
                _dictionaryUIState.value = "${dict.dictionaryFrom.langFull} - ${dict.dictionaryTo.langFull}"
            }
        }
    }

}