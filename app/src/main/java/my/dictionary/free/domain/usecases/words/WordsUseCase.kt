package my.dictionary.free.domain.usecases.words

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.words.TranslateVariant
import my.dictionary.free.domain.models.words.Word
import my.dictionary.free.domain.usecases.dictionary.GetCreateDictionaryUseCase
import my.dictionary.free.domain.utils.PreferenceUtils
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
                        transcription = table.transcription,
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

}