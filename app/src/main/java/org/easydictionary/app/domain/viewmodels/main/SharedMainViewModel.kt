package org.easydictionary.app.domain.viewmodels.main

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import org.easydictionary.app.domain.usecases.users.GetUpdateUsersUseCase
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor(
    private val getUpdateUsersUseCase: GetUpdateUsersUseCase,
    private val preferenceUtils: PreferenceUtils,
    private val uiStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private val TAG = SharedMainViewModel::class.simpleName
        private const val KEY_STATE_TITLE = "title"
    }

    private val _loadingUIState = mutableStateOf(false)
    val loadingUIState: State<Boolean> = _loadingUIState

    private val _showActionButtonUIState = mutableStateOf(false)
    val showActionButtonUIState: State<Boolean> = _showActionButtonUIState

    val titleSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(KEY_STATE_TITLE, "")

    fun clearData() {
        preferenceUtils.clear()
    }

    fun setTitle(title: String) {
        saveTitle(title)
    }

    fun loading(loading: Boolean) {
        _loadingUIState.value = loading
    }

    fun showOrHideActionButton(show: Boolean) {
        _showActionButtonUIState.value = show
    }

    fun saveTitle(value: String?) {
        Log.d(TAG, "title save $value")
        uiStateHandle[KEY_STATE_TITLE] = value
    }

}