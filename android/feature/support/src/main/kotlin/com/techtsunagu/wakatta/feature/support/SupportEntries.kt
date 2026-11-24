package com.techtsunagu.wakatta.feature.support

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.model.InterviewDueSource
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.InterviewKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.SupportLogKey
import com.techtsunagu.wakatta.feature.support.data.FakeInterviewRepository
import com.techtsunagu.wakatta.feature.support.data.FakeSupportLogRepository
import com.techtsunagu.wakatta.feature.support.domain.InterviewRepository
import com.techtsunagu.wakatta.feature.support.domain.SupportLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: supportlog
        entry<SupportLogKey> { key ->
            SupportLogScreen(
                workerId = key.workerId,
                onBackClick = { navigator.pop() },
                onNavigateToInterview = { wId -> navigator.push(InterviewKey(wId)) },
            )
        }
        // Screen id: interview
        entry<InterviewKey> { key ->
            InterviewScreen(
                workerId = key.workerId,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SupportModule {
    @Binds
    @IntoSet
    abstract fun bindSupportEntries(entries: SupportEntries): FeatureEntries
}
