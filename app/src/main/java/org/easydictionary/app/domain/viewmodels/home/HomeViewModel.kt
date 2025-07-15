package org.easydictionary.app.domain.viewmodels.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.easydictionary.app.domain.usecases.register.SignUpUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    companion object {
        private val TAG = HomeViewModel::class.simpleName
    }
}