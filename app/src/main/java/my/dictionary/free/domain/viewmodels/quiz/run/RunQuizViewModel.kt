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
import my.dictionary.free.domain.models.quiz.QuizWordResult
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

    private val _quizEndedUIState: MutableStateFlow<Triple<Boolean, Int, Int>> =
        MutableStateFlow(Triple(false, 0, 0))
    val quizEndedUIState: StateFlow<Triple<Boolean, Int, Int>> = _quizEndedUIState.asStateFlow()

    private val _titleQuizUIState: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(0, 0))
    val titleQuizUIState: StateFlow<Pair<Int, Int>> = _titleQuizUIState.asStateFlow()

    private var quiz: Quiz? = null
    private var currentStep: Int = -1
    private var currentWord: Word? = null
    private var result: ArrayList<QuizWordResult> = arrayListOf()

    fun setQuiz(quiz: Quiz?) {
        this.quiz = quiz
        this.quiz?.words?.shuffle()
        result = arrayListOf()
        currentStep = -1
        provideNextWord()
    }

    private fun provideNextWord() {
        currentStep += 1
        Log.d(TAG, "current step is $currentStep")
        quiz?.words?.let {
            if (currentStep >= 0 && currentStep < it.size) {
                currentWord = it[currentStep]
                nextWordUIState.tryEmit(currentWord!!)
                _titleQuizUIState.value= Pair(currentStep + 1, it.size)
            } else {
                val countWords = result.size
                val countAnswers = result.filter { it.answer?.isNotEmpty() == true }.size
                Log.d(TAG, "quiz is ended. Passed $countWords and right answers is $countAnswers")
                _titleQuizUIState.value= Pair(currentStep, it.size)
                _quizEndedUIState.value = Triple(true, countAnswers, countWords)
            }
        }
    }

    fun checkAnswer(context: Context?, answer: String?) {
        _validationSuccessUIState.value = false
        if (context == null) return
        if (answer.isNullOrEmpty()) {
            validationErrorUIState.tryEmit(context.getString(R.string.error_validation))
            return
        }
        val result = currentWord?.translates?.find {
            it.translation == answer
        }
        if (result == null) {
            validationErrorUIState.tryEmit(context.getString(R.string.error_validation))
            return
        }
        validationErrorUIState.tryEmit("")
        _validationSuccessUIState.value = true
    }

    fun skipAnswer() {
        currentWord?.let {
            result.add(
                QuizWordResult(
                    quizId = quiz!!._id!!,
                    wordId = it._id!!,
                    originalWord = it.original
                )
            )
        }
        provideNextWord()
    }

    fun nextWord(answer: String) {
        currentWord?.let {
            result.add(
                QuizWordResult(
                    quizId = quiz!!._id!!,
                    wordId = it._id!!,
                    originalWord = it.original,
                    answer = answer
                )
            )
        }
        provideNextWord()
    }

}