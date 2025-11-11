package org.easydictionary.app.domain.viewmodels.user.dictionary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.dictionary.Dictionary
import org.easydictionary.app.domain.models.dictionary.DictionaryDetailShort
import org.easydictionary.app.domain.usecases.dictionary.DeleteDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.GetAllDetailShortUseCase
import javax.inject.Inject

sealed interface UserDictionaryEffect {
    data class NavigateDictionaryWords(val item: DictionaryDetailShort) : UserDictionaryEffect
    data object LoadDictionaries : UserDictionaryEffect
    data class ShowError(val message: String) : UserDictionaryEffect
}

data class UserDictionaryUiState(
    val dictionaries: List<DictionaryDetailShort> = emptyList(),
    val isLoading: Boolean = false
)

interface UserDictionaryContract {
    val state: StateFlow<UserDictionaryUiState>
    val effects: Flow<UserDictionaryEffect>

    fun onNavigateDictionaryWords(item: DictionaryDetailShort)
    fun loadDictionaries()
    fun deleteDictionary(dictionary: DictionaryDetailShort)
    fun deleteDictionary(dictionary: Dictionary)
}

@HiltViewModel
class UserDictionaryViewModel @Inject constructor(
    private val getAllDictionaryDetailShortUseCase: GetAllDetailShortUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase
) : ViewModel(), UserDictionaryContract {

    companion object {
        private val TAG = UserDictionaryViewModel::class.simpleName
        const val BUNDLE_NEED_UPDATE_DICTIONARIES =
            "org.easydictionary.app.domain.viewmodels.user.dictionary.UserDictionaryViewModel.BUNDLE_NEED_UPDATE_DICTIONARIES"
    }

    private val _state = MutableStateFlow(UserDictionaryUiState())
    override val state: StateFlow<UserDictionaryUiState> = _state

    private val _effects =
        MutableSharedFlow<UserDictionaryEffect>(extraBufferCapacity = 1, replay = 1)
    override val effects: Flow<UserDictionaryEffect> = _effects

    init {
        _effects.tryEmit(UserDictionaryEffect.LoadDictionaries)
    }

    override fun onNavigateDictionaryWords(item: DictionaryDetailShort) {
        _effects.tryEmit(UserDictionaryEffect.NavigateDictionaryWords(item))
    }

    override fun loadDictionaries() {
        Log.d(TAG, "loadDictionaries()")
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getAllDictionaryDetailShortUseCase(Unit)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(UserDictionaryEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "collected ${result.data.size} dictionaries")
                            _state.update { it.copy(dictionaries = result.data) }
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            UserDictionaryEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }

    override fun deleteDictionary(dictionary: DictionaryDetailShort) {
        deleteDictionary(dictionary.id)
    }

    override fun deleteDictionary(dictionary: Dictionary) {
        deleteDictionary(dictionary.id)
    }

    private fun deleteDictionary(dictionaryId: Int) {
        Log.d(TAG, "deleteDictionary $dictionaryId")
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            deleteDictionaryUseCase.invoke(dictionaryId)
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    _effects.tryEmit(UserDictionaryEffect.ShowError(it.message ?: "Error"))
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            Log.d(TAG, "Dictionary $dictionaryId deleted")
                            loadDictionaries()
                        }

                        is DomainResult.Error -> _effects.tryEmit(
                            UserDictionaryEffect.ShowError(
                                result.message
                            )
                        )
                    }
                }
        }
    }
}