package my.dictionary.free.domain.usecases.words

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.language.LanguageType
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.utils.PreferenceUtils
import my.dictionary.free.view.ext.toUnicode
import java.io.IOException
import javax.inject.Inject

class WordsUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val preferenceUtils: PreferenceUtils
) {

    companion object {
        private val TAG = WordsUseCase::class.simpleName
    }

    /**
     * @return first - if true word was created success
     * @return second - error message exists if respond returns error
     * @return third - created word id
     */
    suspend fun createWord(word: Word): Triple<Boolean, String?, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Triple(
                false,
                null,
                null
            )
        return databaseRepository.createWord(userId, word)
    }

    suspend fun deleteWord(word: Word): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.deleteWord(userId, word.dictionaryId, word._id!!)
    }

    suspend fun deleteWords(dictionaryId: String, words: List<Word>): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        val requestDeleteWordIds = mutableListOf<String>()
        words.forEach { if (it._id != null) requestDeleteWordIds.add(it._id) }
        return databaseRepository.deleteWords(userId, dictionaryId, requestDeleteWordIds)
    }

    suspend fun getWordsByDictionaryId(dictionaryId: String): Flow<Word> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getWordsByDictionaryId(userId, dictionaryId)
                .map { table ->
                    return@map Word(
                        _id = table._id,
                        dictionaryId = table.dictionaryId,
                        original = table.original,
                        phonetic = table.phonetic,
                        translates = emptyList()
                    )
                }
                .map {
                    val converted: MutableList<TranslationVariant> = mutableListOf()
                    databaseRepository.getTranslationVariantByWordId(
                        userId,
                        dictionaryId,
                        it._id ?: ""
                    ).firstOrNull()?.forEach {
                        converted.add(
                            TranslationVariant(
                                _id = it._id,
                                wordId = it.wordId,
                                categoryId = it.categoryId,
                                example = it.description,
                                translation = it.translate
                            )
                        )
                    }
                    return@map it.copyWithNewTranslations(converted)
                }
        }
    }

    suspend fun getWordById(dictionaryId: String, wordId: String): Flow<Word> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getWordById(userId, dictionaryId, wordId)
                .map { table ->
                    return@map Word(
                        _id = table._id,
                        dictionaryId = table.dictionaryId,
                        original = table.original,
                        phonetic = table.phonetic,
                        translates = emptyList()
                    )
                }
                .map {
                    val converted: MutableList<TranslationVariant> = mutableListOf()
                    databaseRepository.getTranslationVariantByWordId(
                        userId,
                        dictionaryId,
                        it._id ?: ""
                    ).firstOrNull()?.forEach {
                        converted.add(
                            TranslationVariant(
                                _id = it._id,
                                wordId = it.wordId,
                                categoryId = it.categoryId,
                                example = it.description,
                                translation = it.translate
                            )
                        )
                    }
                    return@map it.copyWithNewTranslations(converted)
                }
        }
    }

    suspend fun getPhonetics(context: Context, langCode: String): List<String> {
        val languageType =
            LanguageType.values().firstOrNull { it.name == langCode }
                ?: LanguageType.EN
        var phoneticFileName = when (languageType) {
            LanguageType.EN -> "phonetic_en.json"
            LanguageType.UKR -> "phonetic_en.json"
            LanguageType.RU -> "phonetic_en.json"
            LanguageType.DE -> "phonetic_en.json"
            LanguageType.FR -> "phonetic_en.json"
            LanguageType.ES -> "phonetic_en.json"
        }
        try {
            val jsonString = context.assets.open(phoneticFileName)
                .bufferedReader()
                .use { it.readText() }
            val phoneticType = object : TypeToken<List<String>>() {}.type
            val phonetics: List<String> = Gson().fromJson(jsonString, phoneticType)
            val convertedPhonetics = mutableListOf<String>()
            phonetics.forEach {
                try {
                    convertedPhonetics.add(it.toUnicode())
                } catch (ex: IllegalArgumentException) {
                    Log.e(TAG, "Failed to convert HEX $it to ${Charsets.US_ASCII.name()}")
                }
            }
            return convertedPhonetics
        } catch (ex: IOException) {
            //skip
        }
        return emptyList()
    }

}