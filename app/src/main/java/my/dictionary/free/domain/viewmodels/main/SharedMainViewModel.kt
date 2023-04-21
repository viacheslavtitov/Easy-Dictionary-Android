package my.dictionary.free.domain.viewmodels.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase

class SharedMainViewModel(private val getUpdateUsersUseCase: GetUpdateUsersUseCase) : ViewModel() {

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