package org.easydictionary.app.domain.usecases.auth

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.repository.auth.AuthRepository
import javax.inject.Inject

class AuthUseCase @Inject constructor(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        email: String?,
        password: String?,
        provider: String,
        providerToken: String?
    ): Flow<DomainResult<Auth>> {
        return authRepository.signIn(
            email = email,
            password = password,
            provider = provider,
            providerToken = providerToken
        )
    }

}