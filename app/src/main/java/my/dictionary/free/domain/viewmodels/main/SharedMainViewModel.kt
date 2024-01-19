package my.dictionary.free.domain.viewmodels.main

import android.net.Uri
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
import my.dictionary.free.domain.models.navigation.AppNavigation
import my.dictionary.free.domain.models.navigation.HomeScreen
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor(
    private val getUpdateUsersUseCase: GetUpdateUsersUseCase,
    private val preferenceUtils: PreferenceUtils
) : ViewModel() {

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

    val toolbarTitleUIState: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _loadingUIState: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val loadingUIState: StateFlow<Boolean> = _loadingUIState.asStateFlow()

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

    fun clearData() {
        preferenceUtils.clear()
    }

    fun setTitle(title: String) {
        toolbarTitleUIState.tryEmit(title)
    }

    fun loading(loading: Boolean) {
        _loadingUIState.value = loading
    }

}