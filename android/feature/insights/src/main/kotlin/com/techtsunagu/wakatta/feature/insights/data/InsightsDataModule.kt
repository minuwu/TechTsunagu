package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.feature.insights.domain.AnalyticsRepository
import com.techtsunagu.wakatta.feature.insights.domain.FollowUpRepository
import com.techtsunagu.wakatta.feature.insights.domain.LearningRepository
import com.techtsunagu.wakatta.feature.insights.domain.SkillMatrixRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class InsightsDataModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(fake: FakeAnalyticsRepository): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindSkillMatrixRepository(fake: FakeSkillMatrixRepository): SkillMatrixRepository

    @Binds
    @Singleton
    abstract fun bindFollowUpRepository(fake: FakeFollowUpRepository): FollowUpRepository

    @Binds
    @Singleton
    abstract fun bindLearningRepository(fake: FakeLearningRepository): LearningRepository
}
