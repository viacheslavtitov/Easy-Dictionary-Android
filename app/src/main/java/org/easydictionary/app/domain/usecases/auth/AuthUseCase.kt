package org.easydictionary.app.domain.usecases.auth

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.repository.auth.AuthRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class AuthParams(
    val email: String?,
    val password: String?,
    val provider: String,
    val providerToken: String?
)

class AuthUseCase @Inject constructor(private val authRepository: AuthRepository) :
    BaseUseCase<AuthParams, DomainResult<Auth>> {

    override suspend fun invoke(params: AuthParams): Flow<DomainResult<Auth>> {
        return authRepository.signIn(
            email = params.email,
            password = params.password,
            provider = params.provider,
            providerToken = params.providerToken
        )
    }

}