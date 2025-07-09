package org.easydictionary.app.domain.repository.auth

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.repository.BaseRepository

interface AuthRepository: BaseRepository {
    suspend fun signIn(email: String?, password: String?, provider: String, providerToken: String?): Flow<DomainResult<Auth>>
}