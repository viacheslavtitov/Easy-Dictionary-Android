package my.dictionary.free.domain.viewmodels.user.dictionary.words.add

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
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.usecases.words.WordsUseCase
import javax.inject.Inject

@HiltViewModel
class AddDictionaryWordViewModel @Inject constructor(
    private val wordsUseCase: WordsUseCase,
    private val getCreateDictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = AddDictionaryWordViewModel::class.simpleName
    }

    private val _phoneticsUIState: MutableStateFlow<List<String>> =
        MutableStateFlow(emptyList())
    val phoneticsUIState: StateFlow<List<String>> = _phoneticsUIState.asStateFlow()

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState.asStateFlow()

    private var dictionary: Dictionary? = null

    fun loadData(context: Context?, dictionaryId: String?) {
        if (context == null) return
        if (dictionaryId.isNullOrEmpty()) {
            _displayErrorUIState.value = context.getString(R.string.error_load_data)
            return
        }
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
                }
        }.invokeOnCompletion {
            loadPhonetic(context)
        }
    }

    private fun loadPhonetic(context: Context) {
        viewModelScope.launch {
            val phonetics = wordsUseCase.getPhonetics(context)
            Log.d(TAG, "found phonetics count ${phonetics.size}")
            phonetics.forEach {
                Log.d(TAG, it)
            }
        }
    }

}