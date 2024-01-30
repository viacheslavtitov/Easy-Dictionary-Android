package my.dictionary.free.domain.viewmodels.user.dictionary.words.tags

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.domain.viewmodels.user.dictionary.words.DictionaryWordsViewModel
import javax.inject.Inject

@HiltViewModel
class AddWordTagsViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase,
    private val wordsUseCase: WordsUseCase
    ) : ViewModel() {
    companion object {
        private val TAG = AddWordTagsViewModel::class.simpleName
    }

    private val _displayErrorUIState = Channel<String>()
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _createdTagUIState = Channel<WordTag>()
    val createdTagUIState: StateFlow<WordTag> = _createdTagUIState.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), WordTag.empty())

    private var word: Word? = null
    private var dictionary: Dictionary? = null

    fun loadData(context: Context?, word: Word?, dictionary: Dictionary?) {
        if (context == null) return
        if (dictionary == null) {
            viewModelScope.launch {
                _displayErrorUIState.send(context.getString(R.string.error_load_data))
            }
            return
        }
        Log.d(
            TAG,
            "loadData(${word?.original}, ${dictionary.dictionaryFrom} - ${dictionary.dictionaryTo})"
        )
        this.word = word
        this.dictionary = dictionary
    }

    fun addTag(context: Context?, tagName: String?) {
        if (context == null) return
        if (dictionary == null) return
        if (tagName.isNullOrEmpty()) return
        if (dictionary?.tags?.find { it.tagName == tagName } != null) {
            viewModelScope.launch {
                _displayErrorUIState.send(context.getString(R.string.error_tag_exist))
            }
            return
        }
        Log.d(TAG, "addTag($tagName)")
        viewModelScope.launch {
            _loadingUIState.value = true
            val createdTagResult = dictionaryUseCase.createDictionaryTag(dictionary!!, tagName)
            Log.d(TAG, "created tag result is ${createdTagResult.first}")
            if (!createdTagResult.first) {
                val error =
                    createdTagResult.second ?: context.getString(R.string.error_delete_word)
                _loadingUIState.value = false
                _displayErrorUIState.send(error)
            } else {
                _loadingUIState.value = false
                val tag = WordTag(_id = createdTagResult.second, userUUID = "", tagName = tagName)
                dictionary?.tags?.add(tag)
                _createdTagUIState.send(tag)
            }
        }
    }
}