package org.easydictionary.app.domain.usecases.word.tags

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.tags.TagRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

data class CreateNewTagParams(
    val dictionaryId: Int,
    val name: String
)

class CreateNewTagUseCase @Inject constructor(
    private val tagRepository: TagRepository
) : BaseUseCase<CreateNewTagParams, DomainResult<Int>> {
    override suspend fun invoke(params: CreateNewTagParams): Flow<DomainResult<Int>> {
        return tagRepository.createTag(
            dictionaryId = params.dictionaryId,
            name = params.name
        )
    }
}