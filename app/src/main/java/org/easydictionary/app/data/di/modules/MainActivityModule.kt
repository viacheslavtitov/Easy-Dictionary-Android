package org.easydictionary.app.data.di.modules

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.easydictionary.app.data.remote.category.CategoryApiService
import org.easydictionary.app.data.remote.dictionary.DictionaryApiService
import org.easydictionary.app.data.remote.language.LanguageApiService
import org.easydictionary.app.data.remote.language.LanguageStaticApiService
import org.easydictionary.app.data.remote.language.PhoneticsStaticApiService
import org.easydictionary.app.data.remote.word.WordApiService
import org.easydictionary.app.data.remote.word.translations.TranslationVariantApiService
import org.easydictionary.app.data.remote.word.types.WordTypesStaticApiService
import org.easydictionary.app.data.repositories.category.CategoryRepositoryImpl
import org.easydictionary.app.data.repositories.dictionary.DictionaryRepositoryImpl
import org.easydictionary.app.data.repositories.language.LanguageRepositoryImpl
import org.easydictionary.app.data.repositories.language.PhoneticsRepositoryImpl
import org.easydictionary.app.data.repositories.word.WordRepositoryImpl
import org.easydictionary.app.data.repositories.word.translations.TranslationVariantRepositoryImpl
import org.easydictionary.app.domain.repository.category.CategoryRepository
import org.easydictionary.app.domain.repository.dictionary.DictionaryRepository
import org.easydictionary.app.domain.repository.language.LanguageRepository
import org.easydictionary.app.domain.repository.language.PhoneticsRepository
import org.easydictionary.app.domain.repository.word.WordRepository
import org.easydictionary.app.domain.repository.word.translations.TranslationVariantRepository
import org.easydictionary.app.domain.usecases.category.AddCategoryUseCase
import org.easydictionary.app.domain.usecases.category.GetUserDictionaryCategoriesUseCase
import org.easydictionary.app.domain.usecases.dictionary.CreateDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.DeleteDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.GetAllDetailShortUseCase
import org.easydictionary.app.domain.usecases.dictionary.GetAllDictionaryUseCase
import org.easydictionary.app.domain.usecases.dictionary.UpdateDictionaryUseCase
import org.easydictionary.app.domain.usecases.languages.AddUserLanguageUseCase
import org.easydictionary.app.domain.usecases.languages.GetLanguagesStaticUseCase
import org.easydictionary.app.domain.usecases.languages.GetLanguagesUserUseCase
import org.easydictionary.app.domain.usecases.languages.GetPhoneticsUseCase
import org.easydictionary.app.domain.usecases.word.AddWordToDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.DeleteWordUseCase
import org.easydictionary.app.domain.usecases.word.GetAllWordsForDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.SearchWordsForDictionaryUseCase
import org.easydictionary.app.domain.usecases.word.translations.DeleteTranslationUseCase
import org.easydictionary.app.domain.usecases.word.translations.EditTranslationUseCase
import org.easydictionary.app.domain.usecases.word.types.GetWordTypesUseCase
import org.easydictionary.app.domain.utils.PreferenceUtils
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainActivityModule {

    @Provides
    fun provideDictionaryRepository(
        @ApplicationContext context: Context,
        dictionaryApiService: DictionaryApiService
    ): DictionaryRepository {
        return DictionaryRepositoryImpl(context.resources, dictionaryApiService)
    }

    @Provides
    fun provideLanguageRepository(
        @ApplicationContext context: Context,
        languageStaticApiService: LanguageStaticApiService,
        languageApiService: LanguageApiService,
    ): LanguageRepository {
        return LanguageRepositoryImpl(
            context.resources,
            languageStaticApiService,
            languageApiService
        )
    }

    @Provides
    fun provideCategoryRepository(
        @ApplicationContext context: Context,
        categoryApiService: CategoryApiService
    ): CategoryRepository {
        return CategoryRepositoryImpl(
            context.resources,
            categoryApiService
        )
    }

    @Provides
    fun provideWordRepository(
        @ApplicationContext context: Context,
        wordTypesStaticApiService: WordTypesStaticApiService,
        wordApiService: WordApiService
    ): WordRepository {
        return WordRepositoryImpl(
            context.resources,
            wordTypesStaticApiService,
            wordApiService
        )
    }

    @Provides
    fun providePhoneticsRepository(
        @ApplicationContext context: Context,
        phoneticsStaticApiService: PhoneticsStaticApiService
    ): PhoneticsRepository {
        return PhoneticsRepositoryImpl(
            context.resources,
            phoneticsStaticApiService
        )
    }

    @Provides
    fun providePhoneticsRepository(
        @ApplicationContext context: Context,
        translationVariantApiService: TranslationVariantApiService
    ): TranslationVariantRepository {
        return TranslationVariantRepositoryImpl(
            context.resources,
            translationVariantApiService
        )
    }

    @Provides
    fun provideGetAllWordsForDictionaryUseCase(
        wordRepository: WordRepository
    ): GetAllWordsForDictionaryUseCase {
        return GetAllWordsForDictionaryUseCase(wordRepository)
    }

    @Provides
    fun provideSearchWordsForDictionaryUseCase(
        wordRepository: WordRepository
    ): SearchWordsForDictionaryUseCase {
        return SearchWordsForDictionaryUseCase(wordRepository)
    }

    @Provides
    fun provideDeleteWordUseCase(
        wordRepository: WordRepository
    ): DeleteWordUseCase {
        return DeleteWordUseCase(wordRepository)
    }

    @Provides
    fun provideGetPhoneticsUseCase(
        phoneticsRepository: PhoneticsRepository
    ): GetPhoneticsUseCase {
        return GetPhoneticsUseCase(phoneticsRepository)
    }

    @Provides
    fun provideDeleteDictionaryUseCase(dictionaryRepository: DictionaryRepository): DeleteDictionaryUseCase {
        return DeleteDictionaryUseCase(dictionaryRepository)
    }

    @Provides
    fun provideUpdateDictionaryUseCase(dictionaryRepository: DictionaryRepository): UpdateDictionaryUseCase {
        return UpdateDictionaryUseCase(dictionaryRepository)
    }

    @Provides
    fun provideGetLanguagesStaticUseCase(languageRepository: LanguageRepository): GetLanguagesStaticUseCase {
        return GetLanguagesStaticUseCase(languageRepository)
    }

    @Provides
    fun provideGetLanguagesUserUseCase(languageRepository: LanguageRepository): GetLanguagesUserUseCase {
        return GetLanguagesUserUseCase(languageRepository)
    }

    @Provides
    fun provideAddUserLanguageUseCase(languageRepository: LanguageRepository): AddUserLanguageUseCase {
        return AddUserLanguageUseCase(languageRepository)
    }

    @Provides
    fun provideAddCategoryUseCase(categoryRepository: CategoryRepository): AddCategoryUseCase {
        return AddCategoryUseCase(categoryRepository)
    }

    @Provides
    fun provideGetUserCategoriesUseCase(categoryRepository: CategoryRepository): GetUserDictionaryCategoriesUseCase {
        return GetUserDictionaryCategoriesUseCase(categoryRepository)
    }

    @Provides
    fun provideGetWordTypesUseCase(
        wordRepository: WordRepository
    ): GetWordTypesUseCase {
        return GetWordTypesUseCase(wordRepository)
    }

    @Provides
    fun provideAddWordToDictionaryUseCase(
        wordRepository: WordRepository
    ): AddWordToDictionaryUseCase {
        return AddWordToDictionaryUseCase(wordRepository)
    }

    @Provides
    fun provideGetAllDictionaryUseCase(
        dictionaryRepository: DictionaryRepository
    ): GetAllDictionaryUseCase {
        return GetAllDictionaryUseCase(
            dictionaryRepository
        )
    }

    @Provides
    fun provideGetAllDetailShortUseCase(
        dictionaryRepository: DictionaryRepository
    ): GetAllDetailShortUseCase {
        return GetAllDetailShortUseCase(
            dictionaryRepository
        )
    }

    @Provides
    fun provideCreateDictionaryUseCase(
        dictionaryRepository: DictionaryRepository
    ): CreateDictionaryUseCase {
        return CreateDictionaryUseCase(
            dictionaryRepository
        )
    }

    @Provides
    fun provideDeleteTranslationUseCase(
        translationVariantRepository: TranslationVariantRepository
    ): DeleteTranslationUseCase {
        return DeleteTranslationUseCase(
            translationVariantRepository
        )
    }

    @Provides
    fun provideEditTranslationUseCase(
        translationVariantRepository: TranslationVariantRepository
    ): EditTranslationUseCase {
        return EditTranslationUseCase(
            translationVariantRepository
        )
    }

    @Singleton
    @Provides
    fun providePreferenceUtils(@ApplicationContext context: Context): PreferenceUtils {
        return PreferenceUtils(context)
    }

    @Singleton
    @Provides
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

}