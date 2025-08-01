package org.easydictionary.app.view.ext

import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun <T> SavedStateHandle.getFlow(key: String): Flow<T?> = callbackFlow {
    val liveData = getLiveData<T>(key)
    val observer = Observer<T> { value ->
        trySend(value)
    }
    liveData.observeForever(observer)
    awaitClose { liveData.removeObserver(observer) }
}

