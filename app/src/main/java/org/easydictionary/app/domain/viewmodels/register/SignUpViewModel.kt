package org.easydictionary.app.domain.viewmodels.register

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.usecases.auth.AuthUseCase
import org.easydictionary.app.domain.usecases.register.SignUpUseCase
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import org.easydictionary.app.view.FetchDataState
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val preferenceUtils: PreferenceUtils
) : ViewModel() {
    companion object {
        private val TAG = SignUpViewModel::class.simpleName
    }

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        provider: String,
        providerToken: String?
    ) = flow {
        Log.d(
            TAG,
            "signIn: email=$email | password = $password | firstName = $firstName | lastName = $lastName | provider = $provider | providerToken = $providerToken"
        )
        emit(FetchDataState.StartLoadingState)
        signUpUseCase(email, password, firstName, lastName, provider, providerToken)
            .catch {
                Log.d(TAG, "catch ${it.message}")
                emit(FetchDataState.ErrorState(it))
            }
            .onCompletion {
                Log.d(TAG, "onCompletion")
                emit(FetchDataState.FinishLoadingState)
            }
            .collect { result ->
                when (result) {
                    is DomainResult.Success -> {
//                        preferenceUtils.putSecureString(ACCESS_TOKEN_KEY, result.data.accessToken)
//                        preferenceUtils.putSecureString(REFRESH_ACCESS_TOKEN_KEY, result.data.refreshToken)
                        emit(FetchDataState.DataState(result.data))
                    }
                    is DomainResult.Error -> emit(FetchDataState.ErrorStateString(result.message))
                }
            }
    }
}