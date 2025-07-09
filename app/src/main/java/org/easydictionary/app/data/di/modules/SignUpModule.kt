package org.easydictionary.app.data.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.easydictionary.app.data.remote.register.SignUpApiService
import org.easydictionary.app.data.repositories.register.SignUpRepositoryImpl
import org.easydictionary.app.domain.repository.register.SignUpRepository
import org.easydictionary.app.domain.usecases.register.SignUpUseCase
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class SignUpModule {
    @Provides
    fun provideSignUpApiService(retrofit: Retrofit): SignUpApiService =
        retrofit.create(SignUpApiService::class.java)

    @Provides
    fun provideSignUpRepository(@ApplicationContext context: Context, signUpApiService: SignUpApiService): SignUpRepository {
        return SignUpRepositoryImpl(context.resources, signUpApiService)
    }

    @Provides
    fun provideSignUpUseCase(signUpRepository: SignUpRepository): SignUpUseCase {
        return SignUpUseCase(signUpRepository)
    }
}