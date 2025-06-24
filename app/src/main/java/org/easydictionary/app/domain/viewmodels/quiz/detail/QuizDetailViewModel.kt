package org.easydictionary.app.domain.viewmodels.quiz.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.quiz.Quiz
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

    private var quizModel: Quiz? = null

    fun loadQuiz(context: Context?, quiz: Quiz?) {
        if (context == null) return
        if (quiz == null) return
        Log.d(TAG, "loadQuiz($quiz)")
        quizModel = quiz
        _nameUIState.value = quiz.name
        _durationUIState.value = context.getString(R.string.seconds_value, quiz.timeInSeconds)
        quiz.dictionary?.let { dict ->
            if(quiz.reversed) {
                _dictionaryUIState.value =
                    "${dict.dictionaryTo.langFull} - ${dict.dictionaryFrom.langFull}"
            } else {
                _dictionaryUIState.value =
                    "${dict.dictionaryFrom.langFull} - ${dict.dictionaryTo.langFull}"
            }
        }
    }

    fun getQuiz() = quizModel

}