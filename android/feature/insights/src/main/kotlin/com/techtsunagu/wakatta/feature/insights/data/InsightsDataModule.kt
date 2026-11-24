package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.GlossaryRepository
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.ApiConfig
import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
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
object InsightsDataModule {

    @Provides
    @Singleton
    fun provideFollowUpRepository(
        fake: Provider<FakeFollowUpRepository>,
        retrofit: Lazy<Retrofit>,
    ): FollowUpRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(InsightsApi::class.java) }
        return RealFollowUpRepository({ api.value })
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        fake: Provider<FakeAnalyticsRepository>,
        retrofit: Lazy<Retrofit>,
        followUps: FollowUpRepository,
        glossary: GlossaryRepository,
    ): AnalyticsRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(InsightsApi::class.java) }
        return RealAnalyticsRepository({ api.value }, followUps, glossary)
    }

    @Provides
    @Singleton
    fun provideSkillMatrixRepository(
        fake: Provider<FakeSkillMatrixRepository>,
        retrofit: Lazy<Retrofit>,
    ): SkillMatrixRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(InsightsApi::class.java) }
        return RealSkillMatrixRepository({ api.value })
    }

    @Provides
    @Singleton
    fun provideLearningRepository(
        fake: Provider<FakeLearningRepository>,
        retrofit: Lazy<Retrofit>,
        sessionStore: SessionStore,
    ): LearningRepository {
        if (!ApiConfig.USE_LIVE_API) return fake.get()
        val api = lazy { retrofit.get().create(InsightsApi::class.java) }
        return RealLearningRepository({ api.value }, sessionStore)
    }
}
