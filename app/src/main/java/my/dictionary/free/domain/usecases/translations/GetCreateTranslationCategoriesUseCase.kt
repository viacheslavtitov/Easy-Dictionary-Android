package my.dictionary.free.domain.usecases.translations

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import my.dictionary.free.data.models.words.variants.TranslationCategoryTable
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.models.words.variants.TranslationCategory
import my.dictionary.free.domain.utils.PreferenceUtils
import javax.inject.Inject

class GetCreateTranslationCategoriesUseCase @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val preferenceUtils: PreferenceUtils
) {
    companion object {
        private val TAG = GetCreateTranslationCategoriesUseCase::class.simpleName
    }

    suspend fun createCategory(categoryName: String): Pair<Boolean, String?> {
        val userId =
            preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID) ?: return Pair(false, null)
        return databaseRepository.createCategory(
            userId = userId,
            category = TranslationCategoryTable(
                userUUID = userId,
                categoryName = categoryName
            )
        )
    }

    suspend fun getCategories(): Flow<TranslationCategory> {
        val userId = preferenceUtils.getString(PreferenceUtils.CURRENT_USER_ID)
        if (userId.isNullOrEmpty()) {
            return emptyFlow()
        } else {
            return databaseRepository.getCategories(userId)
                .map { cat ->
                    return@map TranslationCategory(
                        _id = cat._id,
                        userUUID = cat.userUUID,
                        categoryName = cat.categoryName,
                    )
                }
        }
    }
}