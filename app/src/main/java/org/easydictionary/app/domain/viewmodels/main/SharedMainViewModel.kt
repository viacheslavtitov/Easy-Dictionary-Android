package org.easydictionary.app.domain.viewmodels.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.easydictionary.app.data.remote.errors.AuthEventsReadable
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

interface SharedMainContract {
    val loadingUIState: State<Boolean>
    val authEvents: Flow<GlobalErrorEvent>
    fun loading(isLoading: Boolean)
    fun clearData()
}

@HiltViewModel
class SharedMainViewModel @Inject constructor(
    private val preferenceUtils: PreferenceUtils,
    @AuthEventsReadable override val authEvents: SharedFlow<GlobalErrorEvent>
) : ViewModel(), SharedMainContract {

    private val _loadingUIState = mutableStateOf(false)
    override val loadingUIState: State<Boolean> = _loadingUIState

    override fun clearData() {
        preferenceUtils.clear()
    }

    override fun loading(isLoading: Boolean) {
        _loadingUIState.value = isLoading
    }
}