package org.easydictionary.app.domain.usecases.translations

import org.easydictionary.app.data.models.words.variants.TranslationVariantTable
import org.easydictionary.app.data.repositories.DatabaseRepository
import org.easydictionary.app.domain.models.words.variants.TranslationVariant
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetCreateTranslationsUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val preferenceUtils: PreferenceUtils
) {
    companion object {
        private val TAG = GetCreateTranslationsUseCase::class.simpleName
    }

    suspend fun createTranslation(
        translation: TranslationVariant,
        dictionaryId: String
    ): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.createTranslation(
            userId = userId,
            dictionaryId = dictionaryId,
            translation = TranslationVariantTable(
                _id = translation._id,
                wordId = translation.wordId,
                translate = translation.translation,
                categoryId = translation.categoryId,
                description = translation.example
            )
        )
    }

    suspend fun updateTranslation(
        translation: TranslationVariant,
        dictionaryId: String
    ): Boolean {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return false
        return databaseRepository.updateTranslation(
            userId = userId,
            dictionaryId = dictionaryId,
            wordId = translation.wordId,
            translation = TranslationVariantTable(
                _id = translation._id,
                wordId = translation.wordId,
                translate = translation.translation,
                categoryId = translation.categoryId,
                description = translation.example
            )
        )
    }

    suspend fun deleteTranslationsFromWord(
        translationIds: List<String>,
        dictionaryId: String,
        wordId: String
    ): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.deleteTranslationsFromWord(
            userId = userId,
            dictionaryId = dictionaryId,
            wordId = wordId,
            translationIds = translationIds
        )
    }
}