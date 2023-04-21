package my.dictionary.free.domain.viewmodels.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var getUpdateUsersUseCase: GetUpdateUsersUseCase

    fun updateUserData(user: FirebaseUser) {
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

}