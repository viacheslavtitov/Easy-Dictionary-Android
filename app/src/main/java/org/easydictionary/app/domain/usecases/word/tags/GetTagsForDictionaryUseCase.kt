package org.easydictionary.app.domain.usecases.word.tags

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.models.word.WordTag
import org.easydictionary.app.domain.repository.word.tags.TagRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class GetTagsForDictionaryUseCase @Inject constructor(
    private val tagRepository: TagRepository
) : BaseUseCase<Int, DomainResult<List<WordTag>>> {
    override suspend fun invoke(params: Int): Flow<DomainResult<List<WordTag>>> {
        return tagRepository.getAllForDictionary(params)
    }
}