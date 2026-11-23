package com.techtsunagu.wakatta.feature.board.data

import com.techtsunagu.wakatta.core.model.TaskRepository
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
object BoardDataModule {

    @Provides
    @Singleton
    fun provideTaskRepository(
        fake: Provider<FakeTaskRepository>,
        retrofit: Lazy<Retrofit>,
    ): TaskRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(TaskApi::class.java) }
        return RealTaskRepository({ api.value })
    }
}
