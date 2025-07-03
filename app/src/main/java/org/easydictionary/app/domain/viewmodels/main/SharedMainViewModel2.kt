package org.easydictionary.app.domain.viewmodels.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel2 @Inject constructor(): ViewModel() {
    private val _loadingState = mutableStateOf(false)
    val loadingState: State<Boolean> = _loadingState
}