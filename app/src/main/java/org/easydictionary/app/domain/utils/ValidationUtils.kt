package org.easydictionary.app.domain.utils

fun isPasswordValid(password: String?): Boolean {
    return password?.isNotEmpty() == true && password.length >= 8 &&
            password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() }
}

fun isEmailValid(email: String?): Boolean {
    return email?.isNotEmpty() == true && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}