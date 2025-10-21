package org.easydictionary.app.domain.usecases.word.translations

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.translations.TranslationVariantRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class DeleteTranslationUseCase @Inject constructor(
    private val translationVariantRepository: TranslationVariantRepository
) : BaseUseCase<Int, DomainResult<Unit>> {
    override suspend fun invoke(params: Int): Flow<DomainResult<Unit>> {
        return translationVariantRepository.deleteTranslation(params)
    }
}