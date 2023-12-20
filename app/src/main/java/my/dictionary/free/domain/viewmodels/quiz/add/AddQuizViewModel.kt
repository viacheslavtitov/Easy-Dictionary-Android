package my.dictionary.free.domain.viewmodels.quiz.add

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
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.quiz.Quiz
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.quize.GetCreateQuizUseCase
import javax.inject.Inject

@HiltViewModel
class AddQuizViewModel @Inject constructor(
    private val getCreateQuizUseCase: GetCreateQuizUseCase
) : ViewModel() {
    companion object {
        private val TAG = AddQuizViewModel::class.simpleName
    }

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _successCreateQuizUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val successCreateQuizUIState: StateFlow<Boolean> = _successCreateQuizUIState.asStateFlow()

    val validateName: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    //edit flows
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

    val wordUIState: MutableSharedFlow<List<Word>> = MutableSharedFlow(
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
    ): Boolean {
        if (context == null) return false
        if (name?.isNullOrEmpty() == true) {
            validateName.tryEmit(context.getString(R.string.field_required))
            return false
        } else {
            validateName.tryEmit("")
        }
        if (duration == null || duration <= 0) {
            _displayErrorUIState.value = context.getString(R.string.error_empty_duration)
            return false
        }
        if (dictionary == null || dictionary._id == null) {
            _displayErrorUIState.value = context.getString(R.string.error_empty_dictionary)
            return false
        }
        if (words.isEmpty()) {
            _displayErrorUIState.value = context.getString(R.string.error_empty_words)
            return false
        }
        return true
    }

    fun save(
        context: Context?,
        name: String?,
        duration: Int?,
        dictionary: Dictionary?,
        words: List<Word>
    ) {
        if (context == null) return
        if (isEditMode()) {
            Log.d(TAG, "create quiz($name)")
        } else {
            Log.d(TAG, "update quiz($name)")
        }
        viewModelScope.launch {
            _loadingUIState.value = true
            _successCreateQuizUIState.value = false
            if (isEditMode()) {
                var successUpdateQuiz = false
                val updatedQuiz = getCreateQuizUseCase.updateQuiz(
                    Quiz(
                        _id = editQuiz!!._id,
                        userId = editQuiz!!.userId,
                        dictionary = dictionary,
                        name = name ?: "",
                        timeInSeconds = duration ?: 0,
                        words = words.toMutableList()
                    )
                )
                if (updatedQuiz) {
                    val shouldDeleteWordsIds = arrayListOf<String>()
                    editQuiz?.quizWords?.forEach { word ->
                        if(words.find { it._id == word.wordId } == null) {
                            shouldDeleteWordsIds.add(word._id!!)
                        }
                    }
                    val resultDeleteWords = getCreateQuizUseCase.deleteWordsFromQuiz(editQuiz!!._id!!, shouldDeleteWordsIds)
                    if (!resultDeleteWords.first) {
                        val error = resultDeleteWords.second ?: context.getString(R.string.error_create_quiz)
                        _displayErrorUIState.value = error
                    } else {
                        val resultToAddWords = addWordsToQuiz(editQuiz!!._id!!, name, duration, dictionary, words)
                        if(!resultToAddWords) {
                            val error = context.getString(R.string.error_create_word)
                            _displayErrorUIState.value = error
                        } else {
                            successUpdateQuiz = true
                        }
                    }
                } else {
                    _displayErrorUIState.value = context.getString(R.string.error_update_quiz)
                }
                _loadingUIState.value = false
                _successCreateQuizUIState.value = successUpdateQuiz
            } else {
                val quizResult = getCreateQuizUseCase.createQuiz(
                    Quiz(
                        _id = null,
                        userId = "",
                        dictionary = dictionary,
                        name = name ?: "",
                        timeInSeconds = duration ?: 0,
                        words = words.toMutableList()
                    )
                )
                _loadingUIState.value = false
                if (!quizResult.first) {
                    val error = quizResult.second ?: context.getString(R.string.error_create_quiz)
                    _displayErrorUIState.value = error
                } else {
                    val quizId = quizResult.third ?: ""
                    val resultToAddWords = addWordsToQuiz(quizId, name, duration, dictionary, words, true)
                    if(!resultToAddWords) {
                        val error = context.getString(R.string.error_create_word)
                        _displayErrorUIState.value = error
                    }
                    _loadingUIState.value = false
                    _successCreateQuizUIState.value = resultToAddWords
                }
            }
        }
    }

    private suspend fun addWordsToQuiz(quizId: String, name: String?,
                                       duration: Int?,
                                       dictionary: Dictionary?, words: List<Word>, shouldDeleteWordIfNotSuccess : Boolean = false): Boolean {
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
                        timeInSeconds = duration ?: 0,
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

    fun setQuiz(quiz: Quiz?) {
        editQuiz = quiz
        if (quiz != null) {
            _nameUIState.value = quiz.name
            quiz.dictionary?.let { dict ->
                dictionaryUIState.tryEmit(dict)
            }
            durationUIState.tryEmit(quiz.timeInSeconds)
            wordUIState.tryEmit(quiz.words)
        }
    }
}