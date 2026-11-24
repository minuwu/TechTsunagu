package com.techtsunagu.wakatta.feature.connect.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

class ConnectRepositoriesHolder(
    val contacts: ContactRepository,
    val threads: PersonalThreadRepository,
)

/**
 * Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake.
 * Nothing touches the network until first use.
 */
@Module
@InstallIn(SingletonComponent::class)
object ConnectDataModule {

    @Provides
    @Singleton
    fun provideHolder(
        fake: Provider<FakeConnectRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        cache: CacheStore,
        @ApplicationContext context: Context,
    ): ConnectRepositoriesHolder {
        if (!ApiConfig.USE_LIVE_API) {
            val f = fake.get()
            return ConnectRepositoriesHolder(f, f)
        }
        val api = lazy { retrofit.get().create(ConnectApi::class.java) }
        val real = RealConnectRepository({ api.value }, session, context, cache)
        return ConnectRepositoriesHolder(real, real)
    }

    @Provides
    @Singleton
    fun provideContactRepository(holder: ConnectRepositoriesHolder): ContactRepository = holder.contacts

    @Provides
    @Singleton
    fun providePersonalThreadRepository(holder: ConnectRepositoriesHolder): PersonalThreadRepository = holder.threads

    /** The chats list shows Contact Threads and Talk Threads next to the work Threads. */
    @Provides
    @IntoSet
    fun providePersonalThreadSource(
        repository: PersonalThreadRepository,
    ): ThreadSource = repository
}
