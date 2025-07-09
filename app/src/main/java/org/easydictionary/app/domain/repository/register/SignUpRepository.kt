package org.easydictionary.app.domain.repository.register

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.auth.Auth
import org.easydictionary.app.domain.models.users.User
import org.easydictionary.app.domain.repository.BaseRepository

interface SignUpRepository : BaseRepository {
    suspend fun signUp(
        email: String?, password: String?, firstName: String?,
        lastName: String?, provider: String, providerToken: String?
    ): Flow<DomainResult<User>>
}