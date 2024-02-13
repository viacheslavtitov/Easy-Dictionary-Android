package my.dictionary.free.domain.viewmodels.quiz.run

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import my.dictionary.free.R
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.quiz.QuizWordResult
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class RunQuizViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase,
    private val getCreateTranslationCategoriesUseCase: GetCreateTranslationCategoriesUseCase
) : ViewModel() {
    companion object {
        private val TAG = RunQuizViewModel::class.simpleName
    }

    val nextWordUIState: MutableLiveData<Pair<Word, Boolean>> by lazy {
        MutableLiveData()
    }

    private val _validationErrorUIState = Channel<String>()
    val validationErrorUIState: StateFlow<String> = _validationErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _quizEndedUIState: MutableStateFlow<Triple<Boolean, Int, Int>> =
        MutableStateFlow(Triple(false, 0, 0))
    val quizEndedUIState: StateFlow<Triple<Boolean, Int, Int>> = _quizEndedUIState.asStateFlow()

    private val _titleQuizUIState: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(0, 0))
    val titleQuizUIState: StateFlow<Pair<Int, Int>> = _titleQuizUIState.asStateFlow()

    private var quiz: Quiz? = null
    private var reversed: Boolean = false
    private var currentStep: Int = -1
    private var currentWord: Word? = null
    private var result: ArrayList<QuizWordResult> = arrayListOf()
    private var translationCategories: ArrayList<TranslationCategory> = arrayListOf()

    fun setQuiz(quiz: Quiz?) = flow<FetchDataState<TranslationCategory>> {
        emit(FetchDataState.StartLoadingState)
        this@RunQuizViewModel.quiz = quiz
        reversed = quiz?.reversed ?: false
        this@RunQuizViewModel.quiz?.words?.shuffle()
        result = arrayListOf()
        translationCategories = arrayListOf()
        currentStep = -1
        getCreateTranslationCategoriesUseCase.getCategories()
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
                provideNextWord()
            }
            .collect {
                Log.d(TAG, "category loaded = ${it.categoryName}")
                translationCategories.add(it)
            }
    }

    fun getDictionary() = quiz?.dictionary
    fun getTranslationCategories() = translationCategories

    fun getQuiz() = quiz

    private fun provideNextWord() {
        currentStep += 1
        Log.d(TAG, "current step is $currentStep")
        quiz?.words?.let {
            if (currentStep >= 0 && currentStep < it.size) {
                currentWord = it[currentStep]
                val pair = Pair(currentWord!!, reversed)
                nextWordUIState.value = pair
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

    fun isEnded() = currentStep >= 0 && currentStep >= (quiz?.words?.size ?: 0)

    fun checkAnswer(context: Context?, answer: String?) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (answer.isNullOrEmpty()) {
//            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_validation)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        val result = if (!reversed) currentWord?.translates?.find {
            it.translation == answer
        } else if (currentWord?.original == answer) TranslationVariant.empty() else null
        if (result == null) {
//            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_validation)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        emit(FetchDataState.ErrorStateString(""))
        emit(FetchDataState.DataState(true))
    }

    fun skipAnswer() {
        currentWord?.let {
            result.add(
                QuizWordResult(
                    quizId = quiz!!._id!!,
                    wordId = it._id!!,
                    originalWord = if (reversed) it.translates.first().translation else it.original
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
                    originalWord = if (reversed) it.translates.first().translation else it.original,
                    answer = answer
                )
            )
        }
        provideNextWord()
    }

    fun saveQuiz(context: Context?) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        emit(FetchDataState.StartLoadingState)
        val countWords = result.size
        val countAnswers = result.filter { it.answer?.isNotEmpty() == true }.size
        val quizResult = getCreateQuizUseCase.saveQuizResult(
            quizId = quiz?._id ?: "",
            wordsCount = countWords,
            rightAnswers = countAnswers,
            unixDateTimeStamp = System.currentTimeMillis()
        )
        if (!quizResult.first) {
            emit(FetchDataState.FinishLoadingState)
            val error = quizResult.second ?: context.getString(R.string.error_save_result_quiz)
            emit(FetchDataState.ErrorStateString(error))
        } else {
            var quizSavedSuccess = true
            val quizResultId = quizResult.third ?: ""
            for (word in result) {
                val wordResult = getCreateQuizUseCase.addWordToQuizResult(quizResultId, word)
                if (!wordResult.first) {
                    val error =
                        wordResult.second ?: context.getString(R.string.error_save_result_quiz)
                    emit(FetchDataState.ErrorStateString(error))
                    getCreateQuizUseCase.deleteQuizResult(
                        quizId = quiz?._id ?: "",
                        quizResultId = quizResultId
                    )
                    quizSavedSuccess = false
                    break
                }
            }
            emit(FetchDataState.FinishLoadingState)
            emit(FetchDataState.DataState(quizSavedSuccess))
        }
    }

}