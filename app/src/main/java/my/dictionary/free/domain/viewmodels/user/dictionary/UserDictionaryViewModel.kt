package my.dictionary.free.domain.viewmodels.user.dictionary

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import javax.inject.Inject

@HiltViewModel
class UserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = UserDictionaryViewModel::class.simpleName
    }

    private val _dictionariesUIState: MutableStateFlow<Dictionary> =
        MutableStateFlow(Dictionary.empty())
    val dictionariesUIState: StateFlow<Dictionary> = _dictionariesUIState.asStateFlow()

    private val _clearActionModeUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val clearActionModeUIState: StateFlow<Boolean> = _clearActionModeUIState

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState

    private val _displayErrorUIState: MutableStateFlow<String> =
        MutableStateFlow("")
    val displayErrorUIState: StateFlow<String> = _displayErrorUIState

    fun loadDictionaries(context: Context?) {
        if (context == null) return
        Log.d(TAG, "loadDictionaries()")
        viewModelScope.launch {
            dictionaryUseCase.getDictionaries(context)
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
                    _dictionariesUIState.tryEmit(it)
                }
        }
    }

    fun deleteDictionaries(context: Context?, list: List<Dictionary>?) {
        if (context == null || list.isNullOrEmpty()) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = dictionaryUseCase.deleteDictionaries(list)
                _clearActionModeUIState.value = true
                if (!result.first) {
                    val error = result.second ?: context.getString(R.string.error_delete_dictionary)
                    _displayErrorUIState.value = error
                }
            }
        }.invokeOnCompletion {
            loadDictionaries(context)
        }
    }

}