package org.easydictionary.app.data.di.modules

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.easydictionary.app.BuildConfig
import org.easydictionary.app.data.models.auth.refresh_token.RefreshTokenRequest
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.auth.AuthApiService
import org.easydictionary.app.data.remote.auth.AuthInterceptor
import org.easydictionary.app.data.remote.auth.RefreshInterceptor
import org.easydictionary.app.data.remote.auth.TokenAuthenticator
import org.easydictionary.app.data.remote.category.CategoryApiService
import org.easydictionary.app.data.remote.dictionary.DictionaryApiService
import org.easydictionary.app.data.remote.errors.GlobalErrorEvent
import org.easydictionary.app.data.remote.language.LanguageApiService
import org.easydictionary.app.data.remote.language.LanguageStaticApiService
import org.easydictionary.app.data.remote.language.PhoneticsStaticApiService
import org.easydictionary.app.data.remote.provideGsonDateConvertor
import org.easydictionary.app.data.remote.word.WordApiService
import org.easydictionary.app.data.remote.word.types.WordTypesStaticApiService
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    fun provideAuthEvents(): MutableSharedFlow<GlobalErrorEvent> =
        MutableSharedFlow(extraBufferCapacity = 1)

    @Provides
    fun provideAuthInterceptor(
        preferenceUtils: PreferenceUtils,
        authEvents: MutableSharedFlow<GlobalErrorEvent>
    ): AuthInterceptor =
        AuthInterceptor(
            tokenProvider = { preferenceUtils.getSecureString(ACCESS_TOKEN_KEY) },
            preferenceUtils = preferenceUtils,
            authEvents = authEvents
        )

    @Provides
    fun provideRefreshInterceptor(
        preferenceUtils: PreferenceUtils,
        authEvents: MutableSharedFlow<GlobalErrorEvent>
    ): RefreshInterceptor =
        RefreshInterceptor(
            preferenceUtils = preferenceUtils,
            authEvents = authEvents
        )

    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        authenticator: TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(authenticator)
            .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .build()

    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    fun provideDictionaryApiService(retrofit: Retrofit): DictionaryApiService =
        retrofit.create(DictionaryApiService::class.java)

    @Provides
    fun provideLanguageApiService(retrofit: Retrofit): LanguageApiService =
        retrofit.create(LanguageApiService::class.java)

    @Provides
    fun provideLanguageStaticApiService(loggingInterceptor: HttpLoggingInterceptor): LanguageStaticApiService {
        val client =
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.LANGUAGES_BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .build()
        return retrofit.create(LanguageStaticApiService::class.java)
    }
    @Provides
    fun provideWordTypesStaticApiService(loggingInterceptor: HttpLoggingInterceptor): WordTypesStaticApiService {
        val client =
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.WORD_TYPES_BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .build()
        return retrofit.create(WordTypesStaticApiService::class.java)
    }

    @Provides
    fun providePhoneticsStaticApiService(loggingInterceptor: HttpLoggingInterceptor): PhoneticsStaticApiService {
        val client =
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.PHONETICS_BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .build()
        return retrofit.create(PhoneticsStaticApiService::class.java)
    }

    @Provides
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides
    fun provideWordApiService(retrofit: Retrofit): WordApiService =
        retrofit.create(WordApiService::class.java)

    @Provides
    fun provideTokenAuthenticator(
        preferenceUtils: PreferenceUtils,
        refreshInterceptor: RefreshInterceptor,
        authEvents: MutableSharedFlow<GlobalErrorEvent>
    ): TokenAuthenticator =
        TokenAuthenticator(
            tokenRefresher = { doRefreshToken(preferenceUtils, refreshInterceptor) },
            preferenceUtils,
            authEvents
        )

    private suspend fun doRefreshToken(
        preferenceUtils: PreferenceUtils,
        refreshInterceptor: RefreshInterceptor
    ): String? {
        val refreshToken = preferenceUtils.getSecureString(REFRESH_ACCESS_TOKEN_KEY) ?: ""
        val client =
            OkHttpClient.Builder()
                .addInterceptor(refreshInterceptor)
                .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .build()

        val authApi = retrofit.create(AuthApiService::class.java)
        try {
            val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
            if(response.isSuccessful) {
                Log.d("NetworkModule", "Token refreshed successful")
                preferenceUtils.putSecureString(ACCESS_TOKEN_KEY, response.body()?.accessToken ?: "")
                preferenceUtils.putSecureString(REFRESH_ACCESS_TOKEN_KEY, response.body()?.refreshToken ?: "")
                return response.body()?.accessToken
            } else {
                Log.e("NetworkModule", "Failed to update refresh token with code ${response.code()}")
                preferenceUtils.clear()
                return null
            }
        } catch (ex: Exception) {
            Log.e("NetworkModule", "Failed to update refresh token", ex)
            return null
        }
    }

}
