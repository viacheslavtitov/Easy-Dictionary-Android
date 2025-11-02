package org.easydictionary.app.test.view.signin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.easydictionary.app.domain.utils.isEmailValid
import org.easydictionary.app.domain.utils.isPasswordValid
import org.easydictionary.app.domain.viewmodels.auth.SignInContract
import org.easydictionary.app.domain.viewmodels.auth.SignInEffect
import org.easydictionary.app.domain.viewmodels.auth.SignInUiState

class FakeSignInContract : SignInContract {
    private val _state = MutableStateFlow(SignInUiState())
    override val state: StateFlow<SignInUiState> = _state

    private val _effects = MutableSharedFlow<SignInEffect>(extraBufferCapacity = 0, replay = 1)
    override val effects: Flow<SignInEffect> = _effects

    override fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, isEmailValid = isEmailValid(value), isFormValid = isEmailValid(value) && it.isPasswordValid) }
    }
    override fun onPasswordChanged(value: String) {
        _state.update { it.copy(password = value, isPasswordValid = isPasswordValid(value), isFormValid = it.isEmailValid && isPasswordValid(value)) }
    }
    override fun onSubmit() {
        _effects.tryEmit(SignInEffect.NavigateHome)
    }
    override fun onNavigateSignUp() {
        _effects.tryEmit(SignInEffect.NavigateSignUp)
    }

    //Just for easiest testing
    fun emitLoading(b: Boolean) = _state.update { it.copy(isLoading = b) }
    fun emitError(msg: String) { _effects.tryEmit(SignInEffect.ShowError(msg)) }
    fun emitNavigateHome() { _effects.tryEmit(SignInEffect.NavigateHome) }
}
