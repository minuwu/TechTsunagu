package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.TodayWordSource
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

/** Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake. Nothing touches the network until first use. */
@Module
@InstallIn(SingletonComponent::class)
object AsksDataModule {

    @Provides
    @Singleton
    fun provideAskRepository(
        fake: Provider<FakeAskRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
    ): AskRepository =
        if (ApiConfig.USE_LIVE_API) RealAskRepository(apiOf(retrofit), session) else fake.get()

    @Provides
    @Singleton
    fun provideSenpaiRepository(
        fake: Provider<FakeSenpaiRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        workers: WorkerRepository,
    ): SenpaiRepository =
        if (ApiConfig.USE_LIVE_API) RealSenpaiRepository(apiOf(retrofit), session, workers) else fake.get()

    @Provides
    @Singleton
    fun provideMeetupRepository(
        fake: Provider<FakeMeetupRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        workers: WorkerRepository,
    ): MeetupRepository =
        if (ApiConfig.USE_LIVE_API) RealMeetupRepository(apiOf(retrofit), session, workers) else fake.get()

    @Provides
    @Singleton
    fun provideTodayWordSource(
        fake: Provider<FakeTodayWordSource>,
        real: Provider<RealTodayWordSource>,
    ): TodayWordSource =
        if (ApiConfig.USE_LIVE_API) real.get() else fake.get()

    private fun apiOf(retrofit: Lazy<Retrofit>): () -> AsksApi {
        val api = lazy { retrofit.get().create(AsksApi::class.java) }
        return { api.value }
    }
}
