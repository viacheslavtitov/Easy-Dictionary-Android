package org.easydictionary.app.domain.usecases.category

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.repository.category.CategoryRepository
import javax.inject.Inject

class GetUserDictionaryCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(dictionaryId: Int): Flow<DomainResult<List<Category>>> {
        return categoryRepository.getAllCategoriesForDictionary(dictionaryId)
    }
}