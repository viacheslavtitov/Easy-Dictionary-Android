package org.easydictionary.app.domain.viewmodels.user.dictionary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.usecases.dictionary.DeleteDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.GetAllDetailShortUseCase
import javax.inject.Inject

@HiltViewModel
class UserDictionaryViewModel @Inject constructor(
    private val getAllDictionaryDetailShortUseCase: GetAllDetailShortUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = UserDictionaryViewModel::class.simpleName
    }

    private val _dictionariesDetailShort =
        MutableStateFlow<List<DictionaryDetailShort>>(emptyList())
    val dictionariesDetailShort: StateFlow<List<DictionaryDetailShort>> =
        _dictionariesDetailShort.asStateFlow()
    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()

    fun loadDictionariesDetailShort() {
        Log.d(TAG, "loadDictionariesDetailShort()")
        _loadingDataUI.value = true
        viewModelScope.launch {
            getAllDictionaryDetailShortUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _errorUI.value = it.message ?: "Error"
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} dictionaries")
                            _dictionariesDetailShort.value = result.data
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }

    fun deleteDictionary(dictionary: DictionaryDetailShort) {
        deleteDictionary(dictionary.id)
    }

    fun deleteDictionary(dictionary: Dictionary) {
        deleteDictionary(dictionary.id)
    }

    private fun deleteDictionary(dictionaryId: Int) {
        Log.d(TAG, "deleteDictionary $dictionaryId")
        _loadingDataUI.value = true
        viewModelScope.launch {
            deleteDictionaryUseCase.invoke(dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _errorUI.value = it.message ?: "Error"
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Dictionary $dictionaryId deleted")
                            loadDictionariesDetailShort()
                        }

                        is DomainResult.Error -> _errorUI.value = result.message
                    }
                }
        }
    }
}