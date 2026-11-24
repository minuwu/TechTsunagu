package com.techtsunagu.wakatta.feature.nearmiss

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.NearMissDetailKey
import com.techtsunagu.wakatta.core.navigation.NearMissListKey
import com.techtsunagu.wakatta.core.navigation.NearMissNewKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearMissEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: nearmiss
        entry<NearMissNewKey> { key ->
            NearMissNewScreen(
                machineId = key.machineId,
                onBackClick = { navigator.pop() },
                onNavigateToList = { navigator.push(NearMissListKey) },
                onNavigateToDetail = { id -> navigator.push(NearMissDetailKey(id)) },
            )
        }
        // Screen id: nearmiss-list
        entry<NearMissListKey> {
            NearMissListScreen(
                onReportClick = { id -> navigator.push(NearMissDetailKey(id)) },
                onNewReportClick = { navigator.push(NearMissNewKey()) },
                onBackClick = { navigator.pop() },
            )
        }
        // Screen id: nearmiss-detail
        entry<NearMissDetailKey> { key ->
            NearMissDetailScreen(
                id = key.id,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NearMissModule {
    @Binds
    @IntoSet
    abstract fun bindNearMissEntries(entries: NearMissEntries): FeatureEntries

}
