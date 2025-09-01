package org.easydictionary.app.domain.usecases.category

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.category.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(dictionaryId: Int, name: String): Flow<DomainResult<Unit>> {
        return categoryRepository.addCategory(dictionaryId, name)
    }
}