package org.easydictionary.app.data.repositories.auth

import android.content.res.Resources
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.easydictionary.app.R
import org.easydictionary.app.data.models.auth.AuthRequest
import org.easydictionary.app.data.models.auth.AuthResponse
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.auth.AuthApiService
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.repository.auth.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val resources: Resources,
    private val authApiService: AuthApiService
) : AuthRepository {

    override suspend fun signIn(
        email: String?,
        password: String?,
        provider: String,
        providerToken: String?
    ): Flow<DomainResult<Auth>> {
        return flowOf(
            when (val result = wrapApi {
                authApiService.login(
                    AuthRequest(
                        email = email,
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

                is ApiResult.NetworkError -> {
                    DomainResult.Error(resources.getString(R.string.network_error))
                }

                is ApiResult.UnknownError -> {
                    DomainResult.Error(resources.getString(R.string.unknown_error))
                }
            }
        )
    }
}