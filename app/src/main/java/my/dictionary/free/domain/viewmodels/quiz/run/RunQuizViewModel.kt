package my.dictionary.free.domain.viewmodels.quiz.run

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import javax.inject.Inject

@HiltViewModel
class RunQuizViewModel @Inject constructor() : ViewModel() {
    companion object {
        private val TAG = RunQuizViewModel::class.simpleName
    }

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    val nextWordUIState: MutableSharedFlow<Word> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val validationErrorUIState: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _validationSuccessUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val validationSuccessUIState: StateFlow<Boolean> = _validationSuccessUIState.asStateFlow()

    private val _quizEndedUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val quizEndedUIState: StateFlow<Boolean> = _quizEndedUIState.asStateFlow()

    private var quiz: Quiz? = null
    private var currentStep: Int = -1
    private var currentWord: Word? = null

    fun setQuiz(quiz: Quiz?) {
        this.quiz = quiz
        Log.d(TAG, "--- before shuffle:")
        this.quiz?.words?.forEach {
            Log.d(TAG, it.original)
        }
        this.quiz?.words?.shuffle()
        Log.d(TAG, "--- after shuffle:")
        this.quiz?.words?.forEach {
            Log.d(TAG, it.original)
        }
        currentStep = -1
        _quizEndedUIState.value = false
        provideNextWord()
    }

    private fun provideNextWord() {
        currentStep += 1
        Log.d(TAG, "current step is $currentStep")
        quiz?.words?.let {
            if (currentStep >= 0 && currentStep <= it.size) {
                currentWord = it[currentStep]
                nextWordUIState.tryEmit(currentWord!!)
            } else {
                _quizEndedUIState.value = true
            }
        }
    }

    fun checkAnswer(context: Context?, answer: String?) {
        _validationSuccessUIState.value = false
        if(context == null) return
        if(answer.isNullOrEmpty()) {
            validationErrorUIState.tryEmit(context.getString(R.string.error_validation))
            return
        }
        val result = currentWord?.translates?.find {
            it.translation == answer
        }
        if(result == null) {
            validationErrorUIState.tryEmit(context.getString(R.string.error_validation))
            return
        }
        validationErrorUIState.tryEmit("")
        _validationSuccessUIState.value = true
    }

    fun skipAnswer() {
        provideNextWord()
    }

    fun nextWord() {
        provideNextWord()
    }

}