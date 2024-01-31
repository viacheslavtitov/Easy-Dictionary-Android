package my.dictionary.free.domain.usecases.words

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import my.dictionary.free.data.models.words.WordTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.dictionary.Dictionary
import my.dictionary.free.domain.models.language.LanguageType
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.WordTag
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

    suspend fun updateWord(word: Word): Boolean {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        return databaseRepository.updateWord(
            userId, WordTable(
                _id = word._id,
                dictionaryId = word.dictionaryId,
                original = word.original,
                type = word.type,
                phonetic = word.phonetic
            )
        )
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
        Log.d(TAG, "getWordsByDictionaryId($dictionaryId)")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        return if (userId.isNullOrEmpty()) {
            emptyFlow()
        } else {
            databaseRepository.getWordsByDictionaryId(userId, dictionaryId)
                .map {
                    return@map convertWordTableAndGetAllNecessaryData(
                        userId,
                        dictionaryId,
                        it
                    ).first()
                }
        }
    }

    suspend fun getWordById(dictionaryId: String, wordId: String): Flow<Word> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        return if (userId.isNullOrEmpty()) {
            emptyFlow()
        } else {
            databaseRepository.getWordById(userId, dictionaryId, wordId)
                .map {
                    convertWordTableAndGetAllNecessaryData(userId, dictionaryId, it).first()
                }
        }
    }

    private suspend fun convertWordTableAndGetAllNecessaryData(
        userId: String,
        dictionaryId: String,
        wordTable: WordTable
    ): Flow<Word> {
        return combine(
            flowOf(wordTable),
            databaseRepository.getTranslationVariantByWordId(
                userId,
                dictionaryId,
                wordTable._id ?: ""
            ),
            databaseRepository.getTagsIdsForWord(
                userId,
                dictionaryId,
                wordTable._id ?: ""
            ),
            databaseRepository.getTagsForDictionary(
                userId,
                dictionaryId
            )
        ) { table, translations, tagIds, allTags ->
            val convertedTranslations: MutableList<TranslationVariant> = mutableListOf()
            val convertedTags: ArrayList<WordTag> = arrayListOf()
            translations.forEach {
                convertedTranslations.add(
                    TranslationVariant(
                        _id = it._id,
                        wordId = it.wordId,
                        categoryId = it.categoryId,
                        example = it.description,
                        translation = it.translate
                    )
                )
            }
            tagIds.forEach { id ->
                allTags.find { it._id == id }?.let { table ->
                    convertedTags.add(
                        WordTag(
                            _id = table._id,
                            userUUID = userId,
                            tagName = table.tagName
                        )
                    )
                }
            }
            return@combine Word(
                _id = table._id,
                dictionaryId = table.dictionaryId,
                original = table.original,
                phonetic = table.phonetic,
                type = table.type,
                translates = convertedTranslations,
                tags = convertedTags
            )
        }
    }

    suspend fun getPhonetics(context: Context, langCode: String): List<String> {
        val languageType =
            LanguageType.values().firstOrNull { it.name == langCode }
                ?: LanguageType.EN
        var phoneticFileName = "phonetics/" + when (languageType) {
            LanguageType.EN -> "phonetic_en.json"
            LanguageType.DE -> "phonetic_de.json"
            LanguageType.FR -> "phonetic_fr.json"
            else -> "phonetic_common.json"
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
                    if (it.contains("+")) {
                        val codes = it.split("+")
                        var result = ""
                        for (code in codes) {
                            try {
                                result += code.toUnicode()
                            } catch (skipEx: IllegalArgumentException) {
                                Log.e(
                                    TAG,
                                    "Failed to convert HEX $code to ${Charsets.US_ASCII.name()}"
                                )
                            }
                        }
                        if (result.isNotEmpty()) convertedPhonetics.add(result)
                    }
                }
            }
            return convertedPhonetics
        } catch (ex: IOException) {
            Log.e(TAG, "phonetics error", ex)
        }
        return emptyList()
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