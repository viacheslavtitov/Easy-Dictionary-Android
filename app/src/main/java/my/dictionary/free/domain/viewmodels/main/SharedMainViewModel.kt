package my.dictionary.free.domain.viewmodels.main

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.dictionary.free.domain.models.navigation.AppNavigation
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var getUpdateUsersUseCase: GetUpdateUsersUseCase

    @Inject
    lateinit var preferenceUtils: PreferenceUtils

    val userEmailValue = MutableLiveData<String>()
    val userAvatarUri = MutableLiveData<Uri>()
    val navigation = MutableLiveData<AppNavigation>()

    val toolbarTitleUIState: MutableSharedFlow<String> = MutableSharedFlow(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private fun updateUserData(user: FirebaseUser) {
        CoroutineScope(Dispatchers.IO).launch {
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

    fun loadUserData() {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            user.photoUrl?.let {
                userAvatarUri.value = it
            }
            userEmailValue.value = user.email
            updateUserData(user)
        }
    }

    fun navigateTo(navigateTo: AppNavigation) {
        navigation.value = navigateTo
    }

    fun clearData() {
        preferenceUtils.clear()
    }

    fun setTitle(title: String) {
        toolbarTitleUIState.tryEmit(title)
    }

}