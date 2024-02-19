package my.dictionary.free.domain.viewmodels.quiz.add

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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class AddQuizViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase
) : ViewModel() {
    companion object {
        private val TAG = AddQuizViewModel::class.simpleName
    }

    private val _validateName = Channel<String>()
    val validateName: StateFlow<String> = _validateName.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    //edit flows
    private val _reversedUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val reversedUIState: StateFlow<Boolean> = _reversedUIState.asStateFlow()

    private val _hidePhoneticUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val hidePhoneticUIState: StateFlow<Boolean> = _hidePhoneticUIState.asStateFlow()

    private val _showTagsUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val showTagsUIState: StateFlow<Boolean> = _showTagsUIState.asStateFlow()

    private val _showCategoriesUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val showCategoriesUIState: StateFlow<Boolean> = _showCategoriesUIState.asStateFlow()

    private val _showTypesUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val showTypesUIState: StateFlow<Boolean> = _showTypesUIState.asStateFlow()

    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    val durationUIState: MutableSharedFlow<Int> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    val dictionaryUIState: MutableSharedFlow<Dictionary> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    private var editQuiz: Quiz? = null

    fun validate(
        context: Context?,
        name: String?,
        duration: Int?,
        dictionary: Dictionary?,
        words: List<Word>
    ) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (name?.isEmpty() == true) {
            _validateName.send(context.getString(R.string.field_required))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        if (duration == null || duration <= 0) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_empty_duration)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        if (dictionary?._id == null) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_empty_dictionary)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        if (words.isEmpty()) {
            emit(FetchDataState.ErrorStateString(context.getString(R.string.error_empty_words)))
            emit(FetchDataState.DataState(false))
            return@flow
        }
        emit(FetchDataState.DataState(true))
    }

    fun save(
        context: Context?,
        name: String?,
        duration: Int?,
        dictionary: Dictionary?,
        reversed: Boolean,
        hidePhonetic: Boolean,
        showTags: Boolean,
        showCategories: Boolean,
        showTypes: Boolean,
        words: List<Word>
    ) = flow<FetchDataState<Boolean>> {
        if (context == null) return@flow
        if (isEditMode()) {
            Log.d(TAG, "create quiz($name)")
        } else {
            Log.d(TAG, "update quiz($name)")
        }
        emit(FetchDataState.StartLoadingState)
        if (isEditMode()) {
            var successUpdateQuiz = false
            val updatedQuiz = getCreateQuizUseCase.updateQuiz(
                Quiz(
                    _id = editQuiz!!._id,
                    userId = editQuiz!!.userId,
                    dictionary = dictionary,
                    name = name ?: "",
                    reversed = reversed,
                    hidePhonetic = hidePhonetic,
                    showTags = showTags,
                    showCategories = showCategories,
                    showTypes = showTypes,
                    timeInSeconds = duration ?: 0,
                    words = words.toMutableList()
                )
            )
            if (updatedQuiz) {
                val shouldDeleteWordsIds = arrayListOf<String>()
                editQuiz?.quizWords?.forEach { oldWord ->
                    var exist = false
                    words.forEach { newWord ->
                        if (newWord._id == oldWord.wordId) {
                            exist = true
                        }
                    }
                    if (!exist) {
                        shouldDeleteWordsIds.add(oldWord._id!!)
                    }
                }
                val resultDeleteWords = getCreateQuizUseCase.deleteWordsFromQuiz(
                    editQuiz!!._id!!,
                    shouldDeleteWordsIds
                )
                if (!resultDeleteWords.first) {
                    val error = resultDeleteWords.second
                        ?: context.getString(R.string.error_create_quiz)
                    emit(FetchDataState.ErrorStateString(error))
                } else {
                    val newWords = arrayListOf<Word>()
                    words.forEach { word ->
                        var exist = false
                        shouldDeleteWordsIds.forEach { id ->
                            if (id == word._id) {
                                exist = true
                            }
                        }
                        if (!exist && editQuiz!!.words.find { it._id == word._id } == null) {
                            newWords.add(word)
                        }
                    }

                    val resultToAddWords = addWordsToQuiz(
                        editQuiz!!._id!!,
                        name,
                        duration,
                        reversed,
                        hidePhonetic,
                        showTags,
                        showCategories,
                        showTypes,
                        dictionary,
                        newWords
                    )
                    if (!resultToAddWords) {
                        val error = context.getString(R.string.error_create_word)
                        emit(FetchDataState.ErrorStateString(error))
                    } else {
                        successUpdateQuiz = true
                    }
                }
            } else {
                emit(FetchDataState.ErrorStateString(context.getString(R.string.error_update_quiz)))
            }
            emit(FetchDataState.FinishLoadingState)
            emit(FetchDataState.DataState(successUpdateQuiz))
        } else {
            val quizResult = getCreateQuizUseCase.createQuiz(
                Quiz(
                    _id = null,
                    userId = "",
                    dictionary = dictionary,
                    name = name ?: "",
                    reversed = reversed,
                    hidePhonetic = hidePhonetic,
                    showTags = showTags,
                    showCategories = showCategories,
                    showTypes = showTypes,
                    timeInSeconds = duration ?: 0,
                    words = words.toMutableList()
                )
            )
            if (!quizResult.first) {
                val error = quizResult.second ?: context.getString(R.string.error_create_quiz)
                emit(FetchDataState.ErrorStateString(error))
                emit(FetchDataState.FinishLoadingState)
            } else {
                val quizId = quizResult.third ?: ""
                val resultToAddWords =
                    addWordsToQuiz(
                        quizId,
                        name,
                        duration,
                        reversed,
                        hidePhonetic,
                        showTags,
                        showCategories,
                        showTypes,
                        dictionary,
                        words,
                        true
                    )
                if (!resultToAddWords) {
                    val error = context.getString(R.string.error_create_word)
                    emit(FetchDataState.ErrorStateString(error))
                }
                emit(FetchDataState.FinishLoadingState)
                emit(FetchDataState.DataState(resultToAddWords))
            }
        }
    }

    private suspend fun addWordsToQuiz(
        quizId: String, name: String?,
        duration: Int?,
        reversed: Boolean,
        hidePhonetic: Boolean,
        showTags: Boolean,
        showCategories: Boolean,
        showTypes: Boolean,
        dictionary: Dictionary?, words: List<Word>, shouldDeleteWordIfNotSuccess: Boolean = false
    ): Boolean {
        var quizCreatedSuccess = true
        for (word in words) {
            val wordResult = getCreateQuizUseCase.addWordToQuiz(quizId, word._id ?: "")
            if (!wordResult.first && shouldDeleteWordIfNotSuccess) {
                getCreateQuizUseCase.deleteQuiz(
                    Quiz(
                        _id = quizId,
                        userId = "",
                        dictionary = dictionary,
                        name = name ?: "",
                        reversed = reversed,
                        timeInSeconds = duration ?: 0,
                        hidePhonetic = hidePhonetic,
                        showTags = showTags,
                        showCategories = showCategories,
                        showTypes = showTypes,
                        words = words.toMutableList()
                    )
                )
                quizCreatedSuccess = false
                break
            }
        }
        return quizCreatedSuccess
    }

    fun isEditMode() = editQuiz != null

    fun setQuiz(quiz: Quiz?) = flow<FetchDataState<List<Word>>> {
        editQuiz = quiz
        if (quiz != null) {
            _nameUIState.value = quiz.name
            quiz.dictionary?.let { dict ->
                dictionaryUIState.tryEmit(dict)
            }
            durationUIState.tryEmit(quiz.timeInSeconds)
            _reversedUIState.value = quiz.reversed
            _hidePhoneticUIState.value = quiz.hidePhonetic
            _showTagsUIState.value = quiz.showTags
            _showCategoriesUIState.value = quiz.showCategories
            _showTypesUIState.value = quiz.showTypes
            emit(FetchDataState.DataState(quiz.words))
        } else {
            emit(FetchDataState.FinishLoadingState)
        }
    }
}