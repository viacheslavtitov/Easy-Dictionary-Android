package my.dictionary.free.domain.usecases.dictionary

import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetCreateDictionaryUseCase @Inject constructor(private val databaseRepository: DatabaseRepository, private val preferenceUtils: PreferenceUtils) {

    suspend fun createDictionary(dictionary: Dictionary): Boolean {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        return databaseRepository.createDictionary(
            userId = userId,
            dictionary = DictionaryTable(
                _id = dictionary._id,
                userUUID = dictionary.userUUID,
                langFrom = dictionary.langFrom,
                langTo = dictionary.langTo,
                dialect = dictionary.dialect
            )
        )
    }

    suspend fun getDictionaries(): List<Dictionary> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return emptyList()
        return databaseRepository.getDictionariesByUserUUID(userId).map {
            Dictionary(
                _id = it._id,
                userUUID = it.userUUID,
                langFrom = it.langFrom,
                langTo = it.langTo,
                dialect = it.dialect
            )
        }
    }

}