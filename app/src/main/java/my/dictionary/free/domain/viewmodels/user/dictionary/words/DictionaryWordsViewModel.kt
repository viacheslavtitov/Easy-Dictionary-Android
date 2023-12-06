package my.dictionary.free.domain.viewmodels.user.dictionary.words

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import my.dictionary.free.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import javax.inject.Inject

@HiltViewModel
class DictionaryWordsViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = DictionaryWordsViewModel::class.simpleName
    }

    val wordsUIState: MutableSharedFlow<Word> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

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

    val titleUIState: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var dictionary: Dictionary? = null

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
                    _shouldClearWordsUIState.value = true
                    _loadingUIState.value = true
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _shouldClearWordsUIState.value = false
                    _loadingUIState.value = false
                }
                .collect {
                    Log.d(
                        TAG,
                        "collect word ${it.original} | translates ${it.translates.size}"
                    )
                    wordsUIState.tryEmit(it)
                }
        }.invokeOnCompletion {
            if (dictionary == null) {
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
                            titleUIState.tryEmit(
                                "${it.dictionaryFrom.langFull} - ${it.dictionaryTo.langFull}"
                            )
                        }
                }
            } else {
                dictionary?.let {
                    titleUIState.tryEmit(
                        "${it.dictionaryFrom.langFull} - ${it.dictionaryTo.langFull}"
                    )
                }
            }
        }
    }

    fun deleteWords(context: Context?, words: List<Word>?) {
        if (context == null || words.isNullOrEmpty()) return
        val dictionaryId = dictionary?._id ?: return
        Log.d(TAG, "deleteWords(${words.size})")
        viewModelScope.launch {
            _loadingUIState.value = true
            val result = wordsUseCase.deleteWords(dictionaryId, words)
            _clearActionModeUIState.value = true
            _loadingUIState.value = false
            Log.d(TAG, "delete result is ${result.first}")
            if (!result.first) {
                val error =
                    result.second ?: context.getString(R.string.error_delete_word)
                _displayErrorUIState.value = error
            } else {
                _shouldClearWordsUIState.value = true
            }
            _clearActionModeUIState.value = true
        }.invokeOnCompletion {
            loadWords(context, dictionaryId)
        }
    }

}