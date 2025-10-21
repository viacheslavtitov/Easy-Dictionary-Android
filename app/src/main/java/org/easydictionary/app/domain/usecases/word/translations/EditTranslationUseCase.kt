package org.easydictionary.app.domain.usecases.word.translations

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.translations.TranslationVariantRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class EditTranslationParams(
    val wordId: Int,
    val translationId: Int,
    val translate: String,
    val description: String?,
    val categoryId: Int?
)

class EditTranslationUseCase @Inject constructor(
    private val translationVariantRepository: TranslationVariantRepository
) : BaseUseCase<EditTranslationParams, DomainResult<Unit>> {
    override suspend fun invoke(params: EditTranslationParams): Flow<DomainResult<Unit>> {
        return translationVariantRepository.editTranslation(
            wordId = params.wordId,
            translationId = params.translationId,
            translate = params.translate,
            description = params.description,
            categoryId = params.categoryId
        )
    }
}