package org.easydictionary.app.domain.usecases

import kotlinx.coroutines.flow.Flow

interface BaseUseCase<in P, out R> {
    suspend operator fun invoke(params: P): Flow<R>
}