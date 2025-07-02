package org.easydictionary.app.domain.viewmodels.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.easydictionary.app.domain.models.navigation.ActionNavigation
import org.easydictionary.app.domain.models.navigation.AddTagNavigation
import org.easydictionary.app.domain.models.navigation.AppNavigation
import org.easydictionary.app.domain.models.navigation.HomeScreen
import org.easydictionary.app.domain.models.users.User
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

    private val _userEmailValue = Channel<String>()
    val userEmailValue: StateFlow<String> = _userEmailValue.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val userAvatarUri: MutableSharedFlow<Uri> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST,
    )

    private val _navigation = Channel<AppNavigation>()
    val navigation: StateFlow<AppNavigation> = _navigation.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HomeScreen())

    private val _actionNavigation = Channel<ActionNavigation>()
    val actionNavigation: StateFlow<ActionNavigation> = _actionNavigation.receiveAsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AddTagNavigation())

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

    private val _showActionButtonUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val showActionButtonUIState: StateFlow<Boolean> = _showActionButtonUIState.asStateFlow()

    val titleSavedUIState: StateFlow<String> = uiStateHandle.getStateFlow(KEY_STATE_TITLE, "")

    fun loadUserData() {
        viewModelScope.launch {

        }
    }

    fun navigateTo(navigateTo: AppNavigation) {
        viewModelScope.launch {
            _navigation.send(navigateTo)
        }
    }

    fun actionNavigate(navigateTo: ActionNavigation) {
        viewModelScope.launch {
            _actionNavigation.send(navigateTo)
        }
    }

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