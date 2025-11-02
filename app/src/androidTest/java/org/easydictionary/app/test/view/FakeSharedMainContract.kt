package org.easydictionary.app.test.view

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.domain.viewmodels.main.SharedMainContract

class FakeSharedMainContract() : SharedMainContract {
    override val loadingUIState = mutableStateOf(false)
    override val authEvents: SharedFlow<GlobalErrorEvent> = MutableSharedFlow<GlobalErrorEvent>(extraBufferCapacity = 1).asSharedFlow()
    override fun loading(isLoading: Boolean) { loadingUIState.value = isLoading }
    override fun clearData() {  }
}