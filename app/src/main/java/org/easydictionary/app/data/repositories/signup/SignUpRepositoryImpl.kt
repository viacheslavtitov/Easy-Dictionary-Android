package org.easydictionary.app.data.repositories.signup

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.signup.SignUpRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.signup.SignUpApiService
import org.easydictionary.app.data.repositories.handleApiErrors
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.users.User
import org.easydictionary.app.domain.repository.register.SignUpRepository
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val signUpService: SignUpApiService
) : SignUpRepository {

    override suspend fun signUp(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        provider: String,
        providerToken: String?
    ): Flow<DomainResult<User>> {
        return flowOf(
            when (val result = wrapApi {
                signUpService.signUp(
                    SignUpRequest(
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        password = password,
                        provider = provider,
                        providerToken = providerToken
                    )
                )
            }) {
                is ApiResult.Success -> {
                    DomainResult.Success(result.data.toDomain())
                }

                is ApiResult.ApiError -> {
                    DomainResult.Error("${resources.getString(R.string.error)}: ${result.message}")
                }

                else -> {
                    handleApiErrors(resources, result)
                }
            }
        )
    }
}