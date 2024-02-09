package my.dictionary.free.domain.viewmodels.user.dictionary

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import my.dictionary.free.R
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class UserDictionaryViewModel @Inject constructor(
    private val dictionaryUseCase: GetCreateDictionaryUseCase
) : ViewModel() {

    companion object {
        private val TAG = UserDictionaryViewModel::class.simpleName
    }

    fun loadDictionaries(context: Context?) = flow<FetchDataState<Dictionary>> {
        if (context == null) {
            return@flow
        }
        Log.d(TAG, "loadDictionaries()")
        emit(FetchDataState.StartLoadingState)
        dictionaryUseCase.getDictionaries(context)
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .collect {
                Log.d(
                    TAG,
                    "collect dictionary ${it.dictionaryFrom.lang} - ${it.dictionaryTo.lang}"
                )
                emit(FetchDataState.DataState(it))
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