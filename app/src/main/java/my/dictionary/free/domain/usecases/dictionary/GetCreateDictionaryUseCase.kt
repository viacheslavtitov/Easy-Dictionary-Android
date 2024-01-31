package my.dictionary.free.domain.usecases.dictionary

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.models.dictionary.DictionaryTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.dictionary.DictionaryItem
import my.dictionary.free.domain.models.dictionary.Flags
import my.dictionary.free.domain.models.words.WordTag
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

    suspend fun updateDictionary(dictionary: Dictionary): Boolean {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        return databaseRepository.updateDictionary(
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
                }.map {
                    Pair(
                        it,
                        databaseRepository.getTagsForDictionary(userId, it._id ?: "").firstOrNull()
                    )
                }.map { pair ->
                    val dictionary = pair.first
                    val tags = pair.second
                    tags?.forEach {
                        dictionary.tags.add(
                            WordTag(
                                _id = it._id,
                                userUUID = userId,
                                tagName = it.tagName
                            )
                        )
                    }
                    return@map dictionary
                }
        }
    }

    suspend fun getDictionaryById(context: Context, dictionaryId: String): Flow<Dictionary> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            val allLanguages = languagesUseCase.getLanguages(context)
            return combine(
                databaseRepository.getDictionaryById(userId, dictionaryId),
                databaseRepository.getTagsForDictionary(userId, dictionaryId)
            ) { dict, tags ->
                val foundLanguageFrom = allLanguages.find { it.key == dict.langFrom }
                val foundLanguageTo = allLanguages.find { it.key == dict.langTo }
                val convertedTags = mutableListOf<WordTag>()
                tags.forEach {
                    convertedTags.add(
                        WordTag(
                            _id = it._id,
                            userUUID = userId,
                            tagName = it.tagName
                        )
                    )
                }
                return@combine Dictionary(
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
                    dialect = dict.dialect,
                    tags = convertedTags
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

    suspend fun getDictionaryTags(dictionaryId: String): Flow<List<WordTag>> {
        Log.d(TAG, "getDictionaryTags($dictionaryId)")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getTagsForDictionary(userId, dictionaryId)
                .map { tableList ->
                    Log.d(TAG, "tags=${tableList.size}")
                    val tagList = mutableListOf<WordTag>()
                    tableList.forEach {
                        tagList.add(
                            WordTag(
                                _id = it._id,
                                userUUID = userId,
                                tagName = it.tagName
                            )
                        )
                    }
                    return@map tagList
                }
        }
    }

    /**
     * @return first - if true tag was created success
     * @return second - created tag id
     */
    suspend fun createDictionaryTag(dictionary: Dictionary, tag: String): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(
                false,
                null
            )
        return databaseRepository.createDictionaryTag(userId, dictionary._id ?: "", tag)
    }
}