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
        Log.d(TAG, "create quiz($name)")
        viewModelScope.launch {
            _loadingUIState.value = true
            _successCreateQuizUIState.value = false
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
                var quizCreatedSuccess = true
                val quizId = quizResult.third ?: ""
                for (word in words) {
                    val wordResult = getCreateQuizUseCase.addWordToQuiz(quizId, word._id ?: "")
                    if (!wordResult.first) {
                        val error =
                            wordResult.second ?: context.getString(R.string.error_create_word)
                        _displayErrorUIState.value = error
                        getCreateQuizUseCase.deleteQuiz(Quiz(
                            _id = quizId,
                            userId = "",
                            dictionary = dictionary,
                            name = name ?: "",
                            timeInSeconds = duration ?: 0,
                            words = words.toMutableList()
                        ))
                        quizCreatedSuccess = false
                        break
                    }
                }
                _loadingUIState.value = false
                _successCreateQuizUIState.value = quizCreatedSuccess
            }
        }
    }
}