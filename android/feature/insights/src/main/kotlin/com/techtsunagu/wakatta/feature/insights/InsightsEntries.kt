package com.techtsunagu.wakatta.feature.insights

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.AnalyticsKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.FollowUpsKey
import com.techtsunagu.wakatta.core.navigation.LearningKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.SkillsKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: analytics
        entry<AnalyticsKey> {
            AnalyticsScreen(
                onBackClick = { navigator.pop() },
                onOpenLearning = { navigator.push(LearningKey(it)) },
            )
        }
        // Screen id: skills
        entry<SkillsKey> {
            SkillsScreen(
                onBackClick = { navigator.pop() },
                onOpenLearning = { navigator.push(LearningKey(it)) },
            )
        }
        // Screen id: followups
        entry<FollowUpsKey> {
            FollowUpsScreen(
                onBackClick = { navigator.pop() },
            )
        }
        // Screen id: learning
        entry<LearningKey> { key ->
            LearningScreen(
                userId = key.userId,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class InsightsModule {
    @Binds
    @IntoSet
    abstract fun bindInsightsEntries(entries: InsightsEntries): FeatureEntries
}
