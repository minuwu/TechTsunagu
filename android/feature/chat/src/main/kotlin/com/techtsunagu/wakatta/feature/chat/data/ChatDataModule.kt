package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.CacheStore
import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.SessionStore
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
object ChatDataModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        fake: Provider<FakeChatRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        workers: WorkerRepository,
        glossary: GlossaryRepository,
        cache: CacheStore,
    ): ChatRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(ChatApi::class.java) }
        return RealChatRepository({ api.value }, session, workers, glossary, cache)
    }
}
