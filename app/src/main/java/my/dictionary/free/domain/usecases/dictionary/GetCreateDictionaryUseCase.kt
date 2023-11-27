package my.dictionary.free.domain.usecases.dictionary

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.dictionary.Flags
import my.dictionary.free.domain.usecases.languages.GetDictionaryLanguagesUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetCreateDictionaryUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val preferenceUtils: PreferenceUtils,
    private val languagesUseCase: GetDictionaryLanguagesUseCase
) {
    companion object {
        private val TAG = GetCreateDictionaryUseCase::class.simpleName
    }

    private val ioScope = Dispatchers.IO

    suspend fun createDictionary(dictionary: Dictionary): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.createDictionary(
            userId = userId,
            dictionary = DictionaryTable(
                _id = dictionary._id,
                userUUID = dictionary.userUUID,
                langFrom = dictionary.dictionaryFrom.lang,
                langTo = dictionary.dictionaryTo.lang,
                dialect = dictionary.dialect
            )
        )
    }

    suspend fun getDictionaries(context: Context): Flow<Dictionary> {
            val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
            if (userId.isNullOrEmpty()) {
                return emptyFlow()
            } else {
                val allLanguages = languagesUseCase.getLanguages(context)
                return databaseRepository.getDictionariesByUserUUID(userId)
                    .map { dict ->
                        val foundLanguageFrom = allLanguages.find { it.key == dict.langFrom }
                        val foundLanguageTo = allLanguages.find { it.key == dict.langTo }
                        return@map Dictionary(
                            _id = dict._id,
                            userUUID = dict.userUUID,
                            dictionaryFrom = DictionaryItem(
                                lang = dict.langFrom,
                                langFull = foundLanguageFrom?.value,
                                flag = Flags(
                                    png = foundLanguageFrom?.flags?.png ?: "",
                                    svg = foundLanguageFrom?.flags?.svg ?: ""
                                )
                            ),
                            dictionaryTo = DictionaryItem(
                                lang = dict.langTo,
                                langFull = foundLanguageTo?.value,
                                flag = Flags(
                                    png = foundLanguageTo?.flags?.png ?: "",
                                    svg = foundLanguageTo?.flags?.svg ?: ""
                                )
                            ),
                            dialect = dict.dialect
                        )
                    }
            }
    }

    suspend fun deleteDictionaries(dictionaries: List<Dictionary>): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        val requestDeleteDictionaryIds = mutableListOf<String>()
        dictionaries.forEach { if (it._id != null) requestDeleteDictionaryIds.add(it._id) }
        return databaseRepository.deleteDictionaries(userId, requestDeleteDictionaryIds)
    }

}