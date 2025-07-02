package org.easydictionary.app.data.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.easydictionary.app.BuildConfig
import org.easydictionary.app.R
import org.easydictionary.app.data.models.auth.refresh_token.RefreshTokenRequest
import org.easydictionary.app.data.remote.ApiCallAdapterFactory
import org.easydictionary.app.data.remote.ApiResult
import org.easydictionary.app.data.remote.auth.AuthInterceptor
import org.easydictionary.app.data.remote.auth.TokenAuthenticator
import org.easydictionary.app.data.remote.auth.AuthApiService
import org.easydictionary.app.data.remote.provideGsonDateConvertor
import org.easydictionary.app.domain.models.DomainResult
import org.easydictionary.app.domain.utils.PreferenceUtils
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.ACCESS_TOKEN_KEY
import org.easydictionary.app.domain.utils.PreferenceUtils.Companion.REFRESH_ACCESS_TOKEN_KEY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if(BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    fun provideAuthInterceptor(preferenceUtils: PreferenceUtils): AuthInterceptor =
        AuthInterceptor { preferenceUtils.getSecureString(ACCESS_TOKEN_KEY) }

    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .addCallAdapterFactory(ApiCallAdapterFactory())
            .build()

    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    fun provideTokenAuthenticator(preferenceUtils: PreferenceUtils): Authenticator = TokenAuthenticator(
        tokenRefresher = { doRefreshToken(preferenceUtils) },
        preferenceUtils
    )

    private suspend fun doRefreshToken(preferenceUtils: PreferenceUtils): String? {
        val refreshToken = preferenceUtils.getSecureString(REFRESH_ACCESS_TOKEN_KEY) ?: ""
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create(provideGsonDateConvertor()))
            .addCallAdapterFactory(ApiCallAdapterFactory())
            .build()

        val authApi = retrofit.create(AuthApiService::class.java)
        val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
        return when(response) {
            is ApiResult.Success -> {
                response.data.accessToken
            }

            else ->  {
                null
            }
        }
    }

}
