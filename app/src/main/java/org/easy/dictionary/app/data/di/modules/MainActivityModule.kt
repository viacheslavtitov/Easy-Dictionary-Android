package org.easy.dictionary.app.data.di.modules

import android.content.Context
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.easy.dictionary.app.BuildConfig
import org.easy.dictionary.app.data.repositories.DatabaseRepository
import org.easy.dictionary.app.domain.usecases.dictionary.GetCreateDictionaryUseCase
import org.easy.dictionary.app.domain.usecases.languages.GetDictionaryLanguagesUseCase
import org.easy.dictionary.app.domain.usecases.quize.GetCreateQuizUseCase
import org.easy.dictionary.app.domain.usecases.translations.GetCreateTranslationCategoriesUseCase
import org.easy.dictionary.app.domain.usecases.translations.GetCreateTranslationsUseCase
import org.easy.dictionary.app.domain.usecases.users.GetUpdateUsersUseCase
import org.easy.dictionary.app.domain.usecases.words.WordsUseCase
import org.easy.dictionary.app.domain.utils.PreferenceUtils
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainActivityModule {

    @Provides
    fun provideDatabaseRepository(): DatabaseRepository {
        return DatabaseRepository(Firebase.database(BuildConfig.FIREBASE_DATABASE_URL))
    }

    @Provides
    fun provideGetUpdateUsersUseCase(
        databaseRepository: DatabaseRepository,
        preferenceUtils: PreferenceUtils
    ): GetUpdateUsersUseCase {
        return GetUpdateUsersUseCase(databaseRepository, preferenceUtils)
    }

    @Provides
    fun provideGetDictionaryLanguagesUseCase(): GetDictionaryLanguagesUseCase {
        return GetDictionaryLanguagesUseCase()
    }

    @Provides
    fun provideWordsUseCase(
        databaseRepository: DatabaseRepository,
        preferenceUtils: PreferenceUtils
    ): WordsUseCase {
        return WordsUseCase(databaseRepository, preferenceUtils)
    }

    @Provides
    fun provideGetCreateDictionaryUseCase(
        databaseRepository: DatabaseRepository,
        preferenceUtils: PreferenceUtils,
        getDictionaryLanguagesUseCase: GetDictionaryLanguagesUseCase
    ): GetCreateDictionaryUseCase {
        return GetCreateDictionaryUseCase(
            databaseRepository,
            preferenceUtils,
            getDictionaryLanguagesUseCase
        )
    }

    @Provides
    fun provideGetCreateTranslationsUseCase(
        databaseRepository: DatabaseRepository,
        preferenceUtils: PreferenceUtils
    ): GetCreateTranslationsUseCase {
        return GetCreateTranslationsUseCase(
            databaseRepository,
            preferenceUtils
        )
    }

    @Provides
    fun provideGetCreateTranslationCategoriesUseCase(
        databaseRepository: DatabaseRepository,
        preferenceUtils: PreferenceUtils
    ): GetCreateTranslationCategoriesUseCase {
        return GetCreateTranslationCategoriesUseCase(
            databaseRepository,
            preferenceUtils
        )
    }

    @Provides
    fun provideGetCreateQuizeUseCase(
        databaseRepository: DatabaseRepository,
        getCreateDictionaryUseCase: GetCreateDictionaryUseCase,
        preferenceUtils: PreferenceUtils
    ): GetCreateQuizUseCase {
        return GetCreateQuizUseCase(
            databaseRepository,
            getCreateDictionaryUseCase,
            preferenceUtils
        )
    }

    @Singleton
    @Provides
    fun providePreferenceUtils(@ApplicationContext context: Context): PreferenceUtils {
        return PreferenceUtils(context)
    }

}