package org.easydictionary.app.domain.repository.auth

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth

interface AuthRepository {
    suspend fun signIn(email: String?, password: String?, provider: String, providerToken: String?): Flow<DomainResult<Auth>>
}