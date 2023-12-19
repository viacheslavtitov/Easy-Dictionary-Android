package my.dictionary.free.domain.viewmodels.quiz.detail

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
import my.dictionary.free.view.quiz.detail.QuizDetailFragment
import javax.inject.Inject

@HiltViewModel
class QuizDetailViewModel @Inject constructor() : ViewModel() {
    companion object {
        private val TAG = QuizDetailViewModel::class.simpleName
    }

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _nameUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val nameUIState: StateFlow<String> = _nameUIState.asStateFlow()

    private val _durationUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val durationUIState: StateFlow<String> = _durationUIState.asStateFlow()

    private val _dictionaryUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val dictionaryUIState: StateFlow<String> = _dictionaryUIState.asStateFlow()

    val wordUIState: MutableSharedFlow<List<Word>> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    private var quizModel: Quiz? = null

    fun loadQuiz(context: Context?, quiz: Quiz?) {
        if (context == null) return
        if (quiz == null) return
        Log.d(TAG, "loadQuiz($quiz)")
        quizModel = quiz
        _nameUIState.value = quiz.name
        _durationUIState.value = context.getString(R.string.seconds_value, quiz.timeInSeconds)
        quiz.dictionary?.let { dict ->
            _dictionaryUIState.value =
                "${dict.dictionaryFrom.langFull} - ${dict.dictionaryTo.langFull}"
        }
        wordUIState.tryEmit(quiz.words)
    }

    fun getQuiz() = quizModel

}