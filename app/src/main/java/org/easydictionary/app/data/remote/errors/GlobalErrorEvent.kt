package org.easydictionary.app.data.remote.errors

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthEventsWritable

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthEventsReadable

sealed class GlobalErrorEvent {
    object Unauthorized : GlobalErrorEvent()
}