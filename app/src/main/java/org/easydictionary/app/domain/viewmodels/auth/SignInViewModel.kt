package org.easydictionary.app.domain.viewmodels.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.usecases.auth.AuthUseCase
import org.easydictionary.app.domain.viewmodels.user.dictionary.UserDictionaryViewModel
import org.easydictionary.app.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {
    companion object {
        private val TAG = SignInViewModel::class.simpleName
    }

    fun signIn(
        email: String?,
        password: String?,
        provider: String,
        providerToken: String?
    ) = flow {
        Log.d(
            TAG,
            "signIn: email=$email | password = $password | provider = $provider | providerToken = $providerToken"
        )
        emit(FetchDataState.StartLoadingState)
        authUseCase(email, password, provider, providerToken)
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .collect { result->
                when (result) {
                    is DomainResult.Success -> emit(FetchDataState.DataState(result.data))
                    is DomainResult.Error -> emit(FetchDataState.ErrorStateString(result.message))
                }
            }
    }
}