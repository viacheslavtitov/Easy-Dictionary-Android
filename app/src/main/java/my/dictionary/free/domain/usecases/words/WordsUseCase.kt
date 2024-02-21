package my.dictionary.free.domain.usecases.words

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import my.dictionary.free.data.models.words.WordTable
import my.dictionary.free.data.models.words.WordTagTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.language.LanguageType
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.models.words.tags.WordTag
import my.dictionary.free.domain.models.words.variants.TranslationVariant
import my.dictionary.free.domain.models.words.verb_tense.WordVerbTense
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

    suspend fun deleteVerbTense(
        dictionaryId: String,
        wordId: String,
        wordTenseId: String
    ): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.deleteVerbTenseFromWord(userId, dictionaryId, wordId, wordTenseId)
    }

    suspend fun deleteWords(dictionaryId: String, words: List<Word>): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        val requestDeleteWordIds = mutableListOf<String>()
        words.forEach { if (it._id != null) requestDeleteWordIds.add(it._id) }
        return databaseRepository.deleteWords(userId, dictionaryId, requestDeleteWordIds)
    }

    suspend fun addTagsToWord(dictionaryId: String, tags: List<WordTag>, wordId: String): Boolean {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        val requestTagIds = mutableListOf<String>()
        tags.forEach { if (it._id != null) requestTagIds.add(it._id) }
        return databaseRepository.addTagsToWord(userId, requestTagIds, dictionaryId, wordId)
    }

    suspend fun addTensesToWord(
        dictionaryId: String, wordId: String, tenseId: String,
        tenseValue: String
    ): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.addVerbTenseToWord(
            userId,
            dictionaryId,
            wordId,
            tenseId,
            tenseValue
        )
    }

    suspend fun getWordsByDictionaryId(dictionaryId: String): Flow<List<Word>> {
        Log.d(TAG, "getWordsByDictionaryId($dictionaryId)")
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        return if (userId.isNullOrEmpty()) {
            emptyFlow()
        } else {
            return combine(
                databaseRepository.getTagsForDictionary(
                    userId,
                    dictionaryId
                ), databaseRepository.getWordsByDictionaryTestId(userId, dictionaryId)
            ) { tags, words ->
                words.map {
                    convertWordTable(
                        userId,
                        tags,
                        it
                    )
                }
            }
        }
    }

    suspend fun getWordById(dictionaryId: String, wordId: String): Flow<Word> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        return if (userId.isNullOrEmpty()) {
            emptyFlow()
        } else {
            return combine(
                databaseRepository.getTagsForDictionary(
                    userId,
                    dictionaryId
                ), databaseRepository.getWordById(userId, dictionaryId, wordId)
            ) { tags, word ->
                convertWordTable(
                    userId,
                    tags,
                    word
                )
            }
        }
    }

    private fun convertWordTable(
        userId: String,
        allTags: List<WordTagTable>,
        wordTable: WordTable
    ): Word {
        val convertedTranslations: MutableList<TranslationVariant> = mutableListOf()
        val convertedTags: ArrayList<WordTag> = arrayListOf()
        val convertedTenses: ArrayList<WordVerbTense> = arrayListOf()
        wordTable.translations.forEach {
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
        wordTable.verbTenses.forEach {
            convertedTenses.add(
                WordVerbTense(
                    _id = it._id,
                    wordId = it.wordId,
                    tenseId = it.tenseId,
                    value = it.value
                )
            )
        }
        wordTable.wordTagsIds.forEach { id ->
            allTags.find { it._id == id }?.let { table ->
                convertedTags.add(
                    WordTag(
                        _id = table._id,
                        userUUID = userId,
                        tag = table.tagName
                    )
                )
            }
        }
        return Word(
            _id = wordTable._id,
            dictionaryId = wordTable.dictionaryId,
            original = wordTable.original,
            phonetic = wordTable.phonetic,
            type = wordTable.type,
            translates = convertedTranslations,
            tags = convertedTags,
            tenses = convertedTenses
        )
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

}