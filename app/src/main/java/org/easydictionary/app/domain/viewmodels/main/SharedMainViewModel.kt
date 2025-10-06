package org.easydictionary.app.domain.viewmodels.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

@HiltViewModel
class SharedMainViewModel @Inject constructor(
    private val preferenceUtils: PreferenceUtils,
    val authEvents: MutableSharedFlow<GlobalErrorEvent>
) : ViewModel() {

    private val _loadingUIState = mutableStateOf(false)
    val loadingUIState: State<Boolean> = _loadingUIState

    fun clearData() {
        preferenceUtils.clear()
    }

    fun loading(loading: Boolean) {
        _loadingUIState.value = loading
    }

}