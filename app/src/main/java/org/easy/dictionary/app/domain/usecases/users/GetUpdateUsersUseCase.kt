package org.easy.dictionary.app.domain.usecases.users

import org.easy.dictionary.app.data.models.users.UsersTable
import org.easy.dictionary.app.data.repositories.DatabaseRepository
import org.easy.dictionary.app.domain.models.users.User
import org.easy.dictionary.app.domain.utils.PreferenceUtils
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