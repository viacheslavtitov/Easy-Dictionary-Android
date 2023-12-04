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
import my.dictionary.free.domain.models.words.TranslateVariant
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.utils.PreferenceUtils
import my.dictionary.free.view.ext.toUnicode
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class WordsUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val preferenceUtils: PreferenceUtils
) {

    companion object {
        private val TAG = WordsUseCase::class.simpleName
    }

    suspend fun getWordsByDictionaryId(dictionaryId: String): Flow<Word> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getWordsByDictionaryId(userId, dictionaryId)
                .map {table ->
                    return@map Word(
                        _id = table._id,
                        dictionaryId = table.dictionaryId,
                        original = table.original,
                        phonetic = table.phonetic,
                        translates = emptyList()
                    )
                }
                .map {
                    val translations = databaseRepository.getTranslationVariantByWordId(userId, dictionaryId, it._id ?: "").firstOrNull()
                    it.translates.toMutableList().also {translates ->
                        translations?.forEach {
                            translates.add(TranslateVariant(
                                _id = it._id,
                                translate = it.translate,
                                description = it.description,
                            ))
                        }
                    }
                    return@map it
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