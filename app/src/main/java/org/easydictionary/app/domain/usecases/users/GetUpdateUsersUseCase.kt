package org.easydictionary.app.domain.usecases.users

import org.easydictionary.app.data.models.users.UsersTable
import org.easydictionary.app.data.repositories.DatabaseRepository
import org.easydictionary.app.domain.models.users.User
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetUpdateUsersUseCase @Inject constructor(private val databaseRepository: DatabaseRepository, private val preferenceUtils: PreferenceUtils) {

    suspend fun insertOrUpdateUser(user: User): Boolean {
        return databaseRepository.insertOrUpdateUser(
            user = UsersTable(
                _id = user._id,
                name = user.name,
                email = user.email,
                providerId = user.providerId,
                uid = user.uid
            ), preferenceUtils
        )
    }

}