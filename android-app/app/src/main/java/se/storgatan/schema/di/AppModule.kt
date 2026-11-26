package se.storgatan.schema.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import se.storgatan.schema.BuildConfig
import se.storgatan.schema.data.fake.FakeRepository
import se.storgatan.schema.data.session.DataStoreSessionStore
import se.storgatan.schema.data.session.SessionStore
import se.storgatan.schema.domain.AuthRepository
import se.storgatan.schema.domain.ShiftRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideSessionStore(
        @ApplicationContext context: Context,
        scope: CoroutineScope,
    ): SessionStore = DataStoreSessionStore(context, scope)

    @Provides
    @Singleton
    fun provideFakeRepository(session: SessionStore): FakeRepository =
        FakeRepository(session)

    @Provides
    @Singleton
    fun provideAuthRepository(fake: FakeRepository): AuthRepository {
        if (!BuildConfig.USE_FAKE_BACKEND) {
            throw UnsupportedOperationException("RemoteRepository not yet wired — set USE_FAKE_BACKEND=true")
        }
        return fake
    }

    @Provides
    @Singleton
    fun provideShiftRepository(fake: FakeRepository): ShiftRepository {
        if (!BuildConfig.USE_FAKE_BACKEND) {
            throw UnsupportedOperationException("RemoteRepository not yet wired — set USE_FAKE_BACKEND=true")
        }
        return fake
    }
}
