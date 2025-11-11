package org.easydictionary.app.domain.utils

import javax.inject.Inject

interface EmailValidator {
    fun isValid(email: String?): Boolean
}

interface PasswordValidator {
    fun isValid(password: String?): Boolean
}

class PasswordValidatorImpl @Inject constructor(): PasswordValidator {
    override fun isValid(password: String?): Boolean {
        return password?.isNotEmpty() == true && password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() }
    }
}

class EmailValidatorImpl @Inject constructor(): EmailValidator {
    override fun isValid(email: String?): Boolean {
        return email?.isNotEmpty() == true && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }
}