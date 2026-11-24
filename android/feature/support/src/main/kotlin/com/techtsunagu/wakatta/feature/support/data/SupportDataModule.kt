package com.techtsunagu.wakatta.feature.support.data

import android.content.Context
import com.techtsunagu.wakatta.core.model.InterviewDueSource
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.WorkerRepository
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * Real (live backend) when `ApiConfig.USE_LIVE_API`, else the Fake.
 * Nothing touches the network until first use.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupportDataModule {

    @Provides
    @Singleton
    fun provideSupportLogRepository(
        fake: Provider<FakeSupportLogRepository>,
        retrofit: Lazy<Retrofit>,
        @ApplicationContext context: Context,
        workerRepository: WorkerRepository,
    ): SupportLogRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(SupportApi::class.java) }
        return RealSupportLogRepository({ api.value }, context, workerRepository)
    }

    @Provides
    @Singleton
    fun provideInterviewRepository(
        fake: Provider<FakeInterviewRepository>,
        retrofit: Lazy<Retrofit>,
        session: SessionStore,
        workerRepository: WorkerRepository,
    ): InterviewRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(SupportApi::class.java) }
        return RealInterviewRepository({ api.value }, session, workerRepository)
    }

    @Provides
    @Singleton
    fun provideInterviewDueSource(
        interviewRepository: InterviewRepository,
    ): InterviewDueSource = interviewRepository
}
