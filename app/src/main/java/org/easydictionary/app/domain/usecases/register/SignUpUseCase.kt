package org.easydictionary.app.domain.usecases.register

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.models.users.User
import org.easydictionary.app.domain.repository.auth.AuthRepository
import org.easydictionary.app.domain.repository.register.SignUpRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val signUpRepository: SignUpRepository) {

    suspend operator fun invoke(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        provider: String,
        providerToken: String?
    ): Flow<DomainResult<User>> {
        return signUpRepository.signUp(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            provider = provider,
            providerToken = providerToken
        )
    }

}