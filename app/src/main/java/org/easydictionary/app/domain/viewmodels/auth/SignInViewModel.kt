package org.easydictionary.app.domain.viewmodels.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.usecases.auth.AuthParams
import org.easydictionary.app.domain.usecases.auth.AuthUseCase
import org.easydictionary.app.domain.utils.EmailValidator
import org.easydictionary.app.domain.utils.PasswordValidator
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import javax.inject.Inject

sealed interface SignInEffect {
    data object NavigateHome : SignInEffect
    data object NavigateSignUp : SignInEffect
    data class ShowError(val message: String) : SignInEffect
}

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false
)

interface SignInContract {
    val state: StateFlow<SignInUiState>
    val effects: Flow<SignInEffect>

    fun onEmailChanged(value: String)
    fun onPasswordChanged(value: String)
    fun onSubmit()
    fun onNavigateSignUp()
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val preferenceUtils: PreferenceUtils,
    private val emailValidator: EmailValidator,
    private val passwordValidator: PasswordValidator,
) : ViewModel(), SignInContract {
    companion object {
        private val TAG = SignInViewModel::class.simpleName
    }

    private val _state = MutableStateFlow(SignInUiState())
    override val state: StateFlow<SignInUiState> = _state

    private val _effects = MutableSharedFlow<SignInEffect>(extraBufferCapacity = 1)
    override val effects: Flow<SignInEffect> = _effects

    override fun onEmailChanged(value: String) {
        _state.update { s ->
            val isEmailValid = emailValidator.isValid(value)
            s.copy(
                email = value,
                isEmailValid = isEmailValid,
                isFormValid = isEmailValid && s.isPasswordValid
            )
        }
    }

    override fun onPasswordChanged(value: String) {
        _state.update { s ->
            val isPassValid = passwordValidator.isValid(value)
            s.copy(
                password = value,
                isPasswordValid = isPassValid,
                isFormValid = s.isEmailValid && isPassValid
            )
        }
    }

    override fun onSubmit() {
        val currentState = _state.value
        if (!currentState.isFormValid || currentState.isLoading) return

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authUseCase(
                AuthParams(
                    currentState.email,
                    currentState.password,
                    provider = "email",
                    providerToken = null
                )
            )
                .catch {
                    Log.d(TAG, "catch ${it.message}")
                    displayError(it.message ?: "Error")
                }
                .onCompletion {
                    Log.d(TAG, "onCompletion")
                    _state.update { it.copy(isLoading = false) }
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
                            _effects.tryEmit(SignInEffect.NavigateHome)
                        }

                        is DomainResult.Error -> {
                            displayError(result.message)
                        }
                    }
                }
        }
    }

    override fun onNavigateSignUp() {
        _effects.tryEmit(SignInEffect.NavigateSignUp)
    }

    fun displayError(message: String) {
        _effects.tryEmit(SignInEffect.ShowError(message))
    }
}