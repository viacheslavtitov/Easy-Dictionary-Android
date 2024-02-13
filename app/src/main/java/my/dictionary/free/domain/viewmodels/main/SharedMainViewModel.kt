package my.dictionary.free.domain.viewmodels.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
import my.dictionary.free.domain.models.navigation.ActionNavigation
import my.dictionary.free.domain.models.navigation.AddTagNavigation
import my.dictionary.free.domain.models.navigation.AppNavigation
import my.dictionary.free.domain.models.navigation.HomeScreen
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import my.dictionary.free.domain.viewmodels.user.dictionary.words.add.AddDictionaryWordViewModel
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

    private fun updateUserData(user: FirebaseUser) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val result = getUpdateUsersUseCase.insertOrUpdateUser(
                    User(
                        name = user.displayName ?: "",
                        email = user.email ?: "",
                        uid = user.email ?: "",
                        providerId = user.providerId
                    )
                )
            }
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            FirebaseAuth.getInstance().currentUser?.let { user ->
                user.photoUrl?.let {
                    userAvatarUri.tryEmit(it)
                }
                user.email?.let {
                    _userEmailValue.send(it)
                }
                updateUserData(user)
            }
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