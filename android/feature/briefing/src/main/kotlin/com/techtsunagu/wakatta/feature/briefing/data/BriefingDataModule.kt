package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.model.SessionStore
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
object BriefingDataModule {

    @Provides
    @Singleton
    fun provideBriefingRepository(
        fake: Provider<FakeBriefingRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
    ): BriefingRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(BriefingApi::class.java) }
        return RealBriefingRepository({ api.value }, session)
    }
}
