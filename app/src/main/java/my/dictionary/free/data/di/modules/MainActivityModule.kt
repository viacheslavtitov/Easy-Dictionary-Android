package my.dictionary.free.data.di.modules

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import my.dictionary.free.BuildConfig
import my.dictionary.free.data.repositories.DatabaseRepository
import my.dictionary.free.domain.usecases.users.GetUpdateUsersUseCase

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainActivityModule {

    @Provides
    fun provideDatabaseRepository(): DatabaseRepository {
        return DatabaseRepository(Firebase.database(BuildConfig.FIREBASE_DATABASE_URL))
    }

    @Provides
    fun provideGetUpdateUsersUseCase(databaseRepository: DatabaseRepository): GetUpdateUsersUseCase {
        return GetUpdateUsersUseCase(databaseRepository)
    }

}