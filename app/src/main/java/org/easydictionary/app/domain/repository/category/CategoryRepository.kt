package org.easydictionary.app.domain.repository.category

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.category.Category
import org.easydictionary.app.domain.repository.BaseRepository

interface CategoryRepository: BaseRepository {
    suspend fun getAllCategories(): Flow<DomainResult<List<Category>>>
    suspend fun getAllCategoriesForDictionary(dictionaryId: Int): Flow<DomainResult<List<Category>>>
    suspend fun addCategory(dictionaryId: Int, name: String): Flow<DomainResult<Unit>>
}