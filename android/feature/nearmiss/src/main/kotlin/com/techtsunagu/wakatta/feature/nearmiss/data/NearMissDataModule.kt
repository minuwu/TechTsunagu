package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissRepository
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
object NearMissDataModule {

    @Provides
    @Singleton
    fun provideNearMissRepository(
        fake: Provider<FakeNearMissRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
    ): NearMissRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(NearMissApi::class.java) }
        return RealNearMissRepository({ api.value }, session)
    }
}
