package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.common.AppConfig
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.core.network.AuthApi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {

    /** Real when the live API is on (ApiConfig) or the build is not a mock build; the HTTP stack is built on first use. */
    @Provides
    @Singleton
    fun provideAuthRepository(
        config: AppConfig,
        store: SessionStore,
        api: Lazy<AuthApi>,
    ): AuthRepository =
        if (ApiConfig.USE_LIVE_API || !config.useMock) {
            RealAuthRepository(api = { api.get() }, store = store)
        } else {
            FakeAuthRepository(store)
        }
}
