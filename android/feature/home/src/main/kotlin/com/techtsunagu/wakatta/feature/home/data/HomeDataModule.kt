package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AttentionSource
import com.techtsunagu.wakatta.core.model.EventSource
import com.techtsunagu.wakatta.core.model.SettingsStore
import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {

    @Binds
    @Singleton
    abstract fun bindSettingsStore(impl: FakeSettingsStore): SettingsStore

    // TaskRepository is bound by :feature:board (it owns the Task Board).

    @Binds
    @Singleton
    abstract fun bindEventSource(impl: FakeEventSource): EventSource

    @Binds
    @IntoSet
    abstract fun bindHomeAttentionSource(impl: HomeAttentionSource): AttentionSource

    @Multibinds
    abstract fun attentionSources(): Set<AttentionSource>

    companion object {
        /** Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake. */
        @Provides
        @Singleton
        fun provideHomeBoardSource(fake: Provider<FakeHomeBoardSource>, retrofit: Lazy<Retrofit>): HomeBoardSource {
            if (!ApiConfig.USE_LIVE_API) return fake.get()
            val api = lazy { retrofit.get().create(HomeApi::class.java) }
            return RealHomeBoardSource { api.value }
        }
    }
}
