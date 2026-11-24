package com.techtsunagu.wakatta.feature.briefing

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.BriefingBoardKey
import com.techtsunagu.wakatta.core.navigation.BriefingCardKey
import com.techtsunagu.wakatta.core.navigation.BriefingNewKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BriefingEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: briefing
        entry<BriefingNewKey> {
            BriefingNewScreen(
                onBackClick = { navigator.pop() },
                onNavigateToBoard = { id -> navigator.push(BriefingBoardKey(id)) },
            )
        }
        // Screen id: briefing-board
        entry<BriefingBoardKey> { key ->
            BriefingBoardScreen(
                id = key.id,
                onBackClick = { navigator.pop() },
            )
        }
        // Screen id: briefing-card
        entry<BriefingCardKey> { key ->
            BriefingCardScreen(
                id = key.id,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BriefingModule {
    @Binds
    @IntoSet
    abstract fun bindBriefingEntries(entries: BriefingEntries): FeatureEntries
}
