package org.easydictionary.app.domain.viewmodels.user.dictionary

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.easydictionary.app.R
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.usecases.dictionary.GetCreateDictionaryUseCase
import org.easydictionary.app.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class UserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = UserDictionaryViewModel::class.simpleName
    }

    private val _dictionariesDetailShort = MutableStateFlow<List<DictionaryDetailShort>>(emptyList())
    val dictionariesDetailShort: StateFlow<List<DictionaryDetailShort>> = _dictionariesDetailShort.asStateFlow()
    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _errorUI = MutableStateFlow<String>("")
    val errorUI: StateFlow<String> = _errorUI.asStateFlow()

    fun loadDictionaries() = flow<FetchDataState<List<Dictionary>>> {
        Log.d(TAG, "loadDictionaries()")
        emit(FetchDataState.StartLoadingState)
        dictionaryUseCase.getDictionaries()
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        emit(FetchDataState.DataState(result.data))
                    }

                    is DomainResult.Error -> emit(FetchDataState.ErrorStateString(result.message))
                }
            }
    }

    fun loadDictionariesDetailShort() {
        Log.d(TAG, "loadDictionariesDetailShort()")
        _loadingDataUI.value = true
        viewModelScope.launch {
            dictionaryUseCase.getDictionariesDetailShort()
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

    fun deleteDictionaries(context: Context?, list: List<Dictionary>?) =
        flow<FetchDataState<Nothing>> {
            if (context == null || list.isNullOrEmpty()) {
                return@flow
            }
            Log.d(TAG, "deleteDictionaries()")
            emit(FetchDataState.StartLoadingState)
            val result = dictionaryUseCase.deleteDictionaries(list)
            emit(FetchDataState.FinishLoadingState)
            Log.d(TAG, "delete result is ${result.first}")
            if (!result.first) {
                val error =
                    result.second ?: context.getString(R.string.error_delete_dictionary)
                emit(FetchDataState.ErrorStateString(error))
            }
        }

}