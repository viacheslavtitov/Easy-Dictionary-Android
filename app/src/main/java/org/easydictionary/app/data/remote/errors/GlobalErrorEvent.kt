package org.easydictionary.app.data.remote.errors

sealed class GlobalErrorEvent {
    object Unauthorized : GlobalErrorEvent()
}