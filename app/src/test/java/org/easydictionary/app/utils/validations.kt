package org.easydictionary.app.utils

import org.easydictionary.app.domain.utils.EmailValidator

class EmailRegExValidatorImpl : EmailValidator {
    private val EMAIL_REGEX =
        Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    override fun isValid(email: String?): Boolean {
        if (email.isNullOrEmpty()) return false
        return EMAIL_REGEX.matches(email)
    }
}