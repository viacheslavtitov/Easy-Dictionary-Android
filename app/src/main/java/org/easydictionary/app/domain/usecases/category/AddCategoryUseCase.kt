package org.easydictionary.app.domain.usecases.category

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.category.CategoryRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class AddCategoryParams(
    val dictionaryId: Int,
    val name: String
)

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) : BaseUseCase<AddCategoryParams, DomainResult<Unit>> {
    override suspend fun invoke(params: AddCategoryParams): Flow<DomainResult<Unit>> {
        return categoryRepository.addCategory(params.dictionaryId, params.name)
    }
}