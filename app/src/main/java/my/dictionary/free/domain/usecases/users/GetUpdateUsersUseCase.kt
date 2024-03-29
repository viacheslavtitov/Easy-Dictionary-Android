package my.dictionary.free.domain.usecases.users

import my.dictionary.free.data.models.users.UsersTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.users.User
import my.dictionary.free.domain.utils.PreferenceUtils
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