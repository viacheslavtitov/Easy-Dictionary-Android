package my.dictionary.free.domain.viewmodels.quiz.run

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
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.quiz.QuizWordResult
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import javax.inject.Inject

@HiltViewModel
class RunQuizViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase
) : ViewModel() {
    companion object {
        private val TAG = RunQuizViewModel::class.simpleName
    }

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    val nextWordUIState: MutableSharedFlow<Pair<Word, Boolean>> = MutableSharedFlow(
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

    private val _successSaveQuizUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successSaveQuizUIState: StateFlow<Boolean> = _successSaveQuizUIState.asStateFlow()

    private var quiz: Quiz? = null
    private var reversed: Boolean = false
    private var currentStep: Int = -1
    private var currentWord: Word? = null
    private var result: ArrayList<QuizWordResult> = arrayListOf()

    fun setQuiz(quiz: Quiz?) {
        this.quiz = quiz
        reversed = quiz?.reversed ?: false
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
                val pair = Pair(currentWord!!, reversed)
                nextWordUIState.tryEmit(pair)
                _titleQuizUIState.value = Pair(currentStep + 1, it.size)
            } else {
                val countWords = result.size
                val countAnswers = result.filter { it.answer?.isNotEmpty() == true }.size
                Log.d(TAG, "quiz is ended. Passed $countWords and right answers is $countAnswers")
                _titleQuizUIState.value = Pair(currentStep, it.size)
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
        val result = if(!reversed) currentWord?.translates?.find {
            it.translation == answer
        } else if(currentWord?.original == answer) TranslationVariant.empty() else null
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
                    originalWord = if(reversed) it.translates.first().translation else it.original
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
                    originalWord = if(reversed) it.translates.first().translation else it.original,
                    answer = answer
                )
            )
        }
        provideNextWord()
    }

    fun saveQuiz(context: Context?) {
        if (context == null) return
        viewModelScope.launch {
            _loadingUIState.value = true
            _successSaveQuizUIState.value = false
            val countWords = result.size
            val countAnswers = result.filter { it.answer?.isNotEmpty() == true }.size
            val quizResult = getCreateQuizUseCase.saveQuizResult(
                quizId = quiz?._id ?: "",
                wordsCount = countWords,
                rightAnswers = countAnswers,
                unixDateTimeStamp = System.currentTimeMillis()
            )
            _loadingUIState.value = false
            if (!quizResult.first) {
                val error = quizResult.second ?: context.getString(R.string.error_save_result_quiz)
                _displayErrorUIState.value = error
            } else {
                var quizSavedSuccess = true
                val quizResultId = quizResult.third ?: ""
                for (word in result) {
                    val wordResult = getCreateQuizUseCase.addWordToQuizResult(quizResultId, word)
                    if (!wordResult.first) {
                        val error =
                            wordResult.second ?: context.getString(R.string.error_save_result_quiz)
                        _displayErrorUIState.value = error
                        getCreateQuizUseCase.deleteQuizResult(
                            quizId = quiz?._id ?: "",
                            quizResultId = quizResultId
                        )
                        quizSavedSuccess = false
                        break
                    }
                }
                _loadingUIState.value = false
                _successSaveQuizUIState.value = quizSavedSuccess
            }
        }
    }

}