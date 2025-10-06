package org.easydictionary.app.view.ext

fun androidx.navigation.NavOptionsBuilder.clearStack() {
    popUpTo(0)
    { inclusive = true }
}