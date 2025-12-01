package org.easydictionary.app.data.di.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.easydictionary.app.domain.utils.DateRangeFormatter
import org.easydictionary.app.domain.utils.DateRangeFormatterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FormatterModule {
    @Binds
    @Singleton
    abstract fun bindDateRangeFormatter(impl: DateRangeFormatterImpl): DateRangeFormatter
}