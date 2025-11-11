package org.easydictionary.app.data.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.easydictionary.app.domain.utils.EmailValidator
import org.easydictionary.app.domain.utils.EmailValidatorImpl
import org.easydictionary.app.domain.utils.PasswordValidator
import org.easydictionary.app.domain.utils.PasswordValidatorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ValidationModule {
    @Binds
    @Singleton
    abstract fun bindEmailValidator(impl: EmailValidatorImpl): EmailValidator

    @Binds
    @Singleton
    abstract fun bindPasswordValidator(impl: PasswordValidatorImpl): PasswordValidator
}