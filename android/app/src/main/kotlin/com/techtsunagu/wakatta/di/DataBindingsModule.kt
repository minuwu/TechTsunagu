package com.techtsunagu.wakatta.di

import com.techtsunagu.wakatta.core.data.FakeCheckRepository
import com.techtsunagu.wakatta.core.data.FakeGlossaryRepository
import com.techtsunagu.wakatta.core.data.FakeMachineRepository
import com.techtsunagu.wakatta.core.data.FakeReceiptRepository
import com.techtsunagu.wakatta.core.data.FakeSignOffRepository
import com.techtsunagu.wakatta.core.data.FakeWorkerRepository
import com.techtsunagu.wakatta.core.data.real.RealCheckRepository
import com.techtsunagu.wakatta.core.data.real.RealGlossaryRepository
import com.techtsunagu.wakatta.core.data.real.RealMachineRepository
import com.techtsunagu.wakatta.core.data.real.RealReceiptRepository
import com.techtsunagu.wakatta.core.data.real.RealSignOffRepository
import com.techtsunagu.wakatta.core.data.real.RealWorkerRepository
import com.techtsunagu.wakatta.core.data.real.SharedDataApi
import com.techtsunagu.wakatta.core.media.ZxingQrEncoder
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.QrEncoder
import com.techtsunagu.wakatta.core.model.ReceiptRepository
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig
import dagger.Binds
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * Binds the shared repositories: Real (built on `:core:network`) when `ApiConfig.USE_LIVE_API`,
 * else the Fake. Providers and `Lazy` keep the unused side and the HTTP stack from being built.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds @Singleton abstract fun qrEncoder(impl: ZxingQrEncoder): QrEncoder

    companion object {
        @Provides @Singleton
        fun sharedDataApi(retrofit: Retrofit): SharedDataApi = retrofit.create(SharedDataApi::class.java)

        @Provides @Singleton
        fun machines(fake: Provider<FakeMachineRepository>, api: Lazy<SharedDataApi>, session: SessionStore): MachineRepository =
            if (ApiConfig.USE_LIVE_API) RealMachineRepository({ api.get() }, session) else fake.get()

        @Provides @Singleton
        fun signOffs(fake: Provider<FakeSignOffRepository>, api: Lazy<SharedDataApi>): SignOffRepository =
            if (ApiConfig.USE_LIVE_API) RealSignOffRepository { api.get() } else fake.get()

        @Provides @Singleton
        fun workers(fake: Provider<FakeWorkerRepository>, api: Lazy<SharedDataApi>): WorkerRepository =
            if (ApiConfig.USE_LIVE_API) RealWorkerRepository { api.get() } else fake.get()

        @Provides @Singleton
        fun glossary(fake: Provider<FakeGlossaryRepository>, api: Lazy<SharedDataApi>): GlossaryRepository =
            if (ApiConfig.USE_LIVE_API) RealGlossaryRepository { api.get() } else fake.get()

        @Provides @Singleton
        fun checks(fake: Provider<FakeCheckRepository>, api: Lazy<SharedDataApi>): CheckRepository =
            if (ApiConfig.USE_LIVE_API) RealCheckRepository { api.get() } else fake.get()

        @Provides @Singleton
        fun receipts(fake: Provider<FakeReceiptRepository>, api: Lazy<SharedDataApi>): ReceiptRepository =
            if (ApiConfig.USE_LIVE_API) RealReceiptRepository { api.get() } else fake.get()
    }
}
