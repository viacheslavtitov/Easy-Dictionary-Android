package org.easydictionary.app.domain.usecases.word

import kotlinx.coroutines.flow.Flow
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.usecases.BaseUseCase
import javax.inject.Inject

class DeleteWordUseCase @Inject constructor(
    private val wordRepository: WordRepository
) : BaseUseCase<Int, DomainResult<Unit>> {
    override suspend fun invoke(params: Int): Flow<DomainResult<Unit>> {
        return wordRepository.deleteWord(params)
    }
}