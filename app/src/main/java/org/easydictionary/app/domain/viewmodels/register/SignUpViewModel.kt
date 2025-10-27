package org.easydictionary.app.domain.viewmodels.register

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
import org.easydictionary.app.domain.usecases.register.SignUpParams
import org.easydictionary.app.domain.usecases.register.SignUpUseCase
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    companion object {
        private val TAG = SignUpViewModel::class.simpleName
    }

    private val _loadingDataUI = MutableStateFlow<Boolean>(false)
    val loadingDataUI: StateFlow<Boolean> = _loadingDataUI.asStateFlow()
    private val _errorMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    private val _signedUpSuccess = MutableSharedFlow<Boolean>()
    val signedUpSuccess: SharedFlow<Boolean> = _signedUpSuccess

    fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        provider: String,
        providerToken: String?
    ) {
        Log.d(
            TAG,
            "signIn: email=$email | password = $password | firstName = $firstName | lastName = $lastName | provider = $provider | providerToken = $providerToken"
        )
        _loadingDataUI.value = true
        viewModelScope.launch {
            signUpUseCase(
                SignUpParams(
                    email,
                    password,
                    firstName,
                    lastName,
                    provider,
                    providerToken
                )
            )
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
                            _signedUpSuccess.emit(true)
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