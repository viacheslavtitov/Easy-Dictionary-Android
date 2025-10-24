package org.easydictionary.app.domain.usecases.word.translations

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.translations.TranslationVariantRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class AddTranslationParams(
    val wordId: Int,
    val translate: String,
    val description: String?,
    val categoryId: Int?
)

class AddTranslationUseCase @Inject constructor(
    private val translationVariantRepository: TranslationVariantRepository
) : BaseUseCase<AddTranslationParams, DomainResult<Int>> {
    override suspend fun invoke(params: AddTranslationParams): Flow<DomainResult<Int>> {
        return translationVariantRepository.addTranslation(
            wordId = params.wordId,
            translate = params.translate,
            description = params.description,
            categoryId = params.categoryId
        )
    }
}