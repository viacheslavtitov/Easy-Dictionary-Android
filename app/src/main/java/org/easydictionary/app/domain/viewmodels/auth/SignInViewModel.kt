package org.easydictionary.app.domain.viewmodels.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.usecases.auth.AuthParams
import org.easydictionary.app.domain.usecases.auth.AuthUseCase
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val preferenceUtils: PreferenceUtils
) : ViewModel() {
    companion object {
        private val TAG = SignInViewModel::class.simpleName
    }

    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _errorMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    private val _signedInSuccess = MutableSharedFlow<Boolean>()
    val signedInSuccess: SharedFlow<Boolean> = _signedInSuccess

    fun signIn(
        email: String?,
        password: String?,
        provider: String,
        providerToken: String?
    ) {
        Log.d(
            TAG,
            "signIn: email=$email | password = $password | provider = $provider | providerToken = $providerToken"
        )
        _loadingDataUI.value = true
        viewModelScope.launch {
            authUseCase(AuthParams(email, password, provider, providerToken))
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _loadingDataUI.value = false
                }
                .collect { result ->
                    when (result) {
                        is DomainResult.Success -> {
                            preferenceUtils.putSecureString(
                                ACCESS_TOKEN_KEY,
                                result.data.accessToken
                            )
                            preferenceUtils.putSecureString(
                                REFRESH_ACCESS_TOKEN_KEY,
                                result.data.refreshToken
                            )
                            _signedInSuccess.emit(true)
                        }

                        is DomainResult.Error -> displayError(result.message)
                    }
                }
        }
    }

    fun displayError(message: String) {
        _errorMessage.tryEmit(message)
    }
}