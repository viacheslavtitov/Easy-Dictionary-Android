package org.easydictionary.app.domain.usecases.register

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.users.User
import org.easydictionary.app.domain.repository.register.SignUpRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class SignUpParams(
    val email: String?,
    val password: String?,
    val firstName: String?,
    val lastName: String?,
    val provider: String,
    val providerToken: String?
)

class SignUpUseCase @Inject constructor(private val signUpRepository: SignUpRepository) :
    BaseUseCase<SignUpParams, DomainResult<User>> {
    override suspend fun invoke(params: SignUpParams): Flow<DomainResult<User>> {
        return signUpRepository.signUp(
            email = params.email,
            password = params.password,
            firstName = params.firstName,
            lastName = params.lastName,
            provider = params.provider,
            providerToken = params.providerToken
        )
    }

}