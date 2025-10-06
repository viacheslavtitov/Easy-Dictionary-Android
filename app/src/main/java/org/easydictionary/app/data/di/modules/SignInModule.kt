package org.easydictionary.app.data.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.easydictionary.app.data.remote.auth.AuthApiService
import org.easydictionary.app.data.repositories.auth.AuthRepositoryImpl
import org.easydictionary.app.domain.repository.auth.AuthRepository
import org.easydictionary.app.domain.usecases.auth.AuthUseCase

@Module
@InstallIn(ViewModelComponent::class)
class SignInModule {
    @Provides
    fun provideAuthRepository(@ApplicationContext context: Context, authApiService: AuthApiService): AuthRepository {
        return AuthRepositoryImpl(context.resources, authApiService)
    }

    @Provides
    fun provideAuthUseCase(authRepository: AuthRepository): AuthUseCase {
        return AuthUseCase(authRepository)
    }
}