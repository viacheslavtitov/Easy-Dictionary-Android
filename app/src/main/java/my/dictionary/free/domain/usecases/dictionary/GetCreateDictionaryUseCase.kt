package my.dictionary.free.domain.usecases.dictionary

import android.content.Context
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

    suspend fun createDictionary(dictionary: Dictionary): Boolean {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
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

    suspend fun getDictionaries(context: Context): List<Dictionary> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return emptyList()
        val allLanguages = languagesUseCase.getLanguages(context)
        return databaseRepository.getDictionariesByUserUUID(userId).map { dict ->
            val foundLanguageFrom = allLanguages.find { it.key == dict.langFrom }
            val foundLanguageTo = allLanguages.find { it.key == dict.langTo }
            Dictionary(
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