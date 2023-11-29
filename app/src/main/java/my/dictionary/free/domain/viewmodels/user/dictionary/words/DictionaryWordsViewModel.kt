package my.dictionary.free.domain.viewmodels.user.dictionary.words

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import javax.inject.Inject

@HiltViewModel
class DictionaryWordsViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = DictionaryWordsViewModel::class.simpleName
    }

    private val _wordsUIState: MutableStateFlow<Word> =
        MutableStateFlow(Word.empty())
    val wordsUIState: StateFlow<Word> = _wordsUIState.asStateFlow()

    private val _clearActionModeUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val clearActionModeUIState: StateFlow<Boolean> = _clearActionModeUIState.asStateFlow()

    private val _shouldClearWordsUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val shouldClearWordsUIState: StateFlow<Boolean> = _shouldClearWordsUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private var dictionary: Dictionary? = null

    private val _titleUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val titleUIState: StateFlow<String> = _titleUIState.asStateFlow()

    fun loadWords(context: Context?, dictionaryId: String?) {
        if (context == null) return
        if (dictionaryId.isNullOrEmpty()) {
            _displayErrorUIState.value = context.getString(R.string.error_load_data)
            return
        }
        Log.d(TAG, "loadWords()")
        viewModelScope.launch {
            wordsUseCase.getWordsByDictionaryId(dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _displayErrorUIState.value =
                        it.message ?: context.getString(R.string.unknown_error)
                }
                .onStart {
                    Log.d(TAG, "onStart")
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingUIState.value = false
                }
                .collect {
                    Log.d(
                        TAG,
                        "collect word ${it.original} | translates ${it.translates.size}"
                    )
                    _wordsUIState.value = it
                }
        }.invokeOnCompletion {
            if(dictionary == null) {
                viewModelScope.launch {
                    getCreateDictionaryUseCase.getDictionaryById(context, dictionaryId)
                        .catch {
                            Log.d(TAG, "catch ${it.message}")
                            _displayErrorUIState.value =
                                it.message ?: context.getString(R.string.unknown_error)
                        }
                        .onStart {
                            Log.d(TAG, "onStart")
                            _loadingUIState.value = true
                        }
                        .onCompletion {
                            Log.d(TAG, "onCompletion")
                            _loadingUIState.value = false
                        }
                        .collect {
                            Log.d(
                                TAG,
                                "collect dictionary ${it.dictionaryFrom.lang} - ${it.dictionaryTo.lang}"
                            )
                            dictionary = it
                            _titleUIState.value = "${it.dictionaryFrom.langFull} - ${it.dictionaryTo.langFull}"
                        }
                }
            }
        }
    }

    fun deleteWords(context: Context?, words: List<Word>?) {

    }

}