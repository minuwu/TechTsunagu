package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object HomeDataModule {

    // TaskRepository is bound by :feature:board (it owns the Task Board). SettingsStore is bound by :app.

    /** Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake. */
    @Provides
    @Singleton
    fun provideHomeBoardSource(fake: Provider<FakeHomeBoardSource>, retrofit: Lazy<Retrofit>): HomeBoardSource {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(HomeApi::class.java) }
        return RealHomeBoardSource { api.value }
    }

    @Provides
    @Singleton
    fun provideMembersSource(fake: Provider<FakeMembersSource>, retrofit: Lazy<Retrofit>): MembersSource {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(MembersApi::class.java) }
        return RealMembersSource { api.value }
    }
}
