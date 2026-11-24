package com.techtsunagu.wakatta.feature.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.HomeKey
import com.techtsunagu.wakatta.core.navigation.MeKey
import com.techtsunagu.wakatta.core.navigation.MembersKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.OnboardingKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: home
        entry<HomeKey> {
            HomeScreen(
                onNavigate = { key ->
                    (key as? NavKey)?.let { navigator.push(it) }
                },
            )
        }
        // Screen id: me
        entry<MeKey> {
            MeScreen(
                onNavigate = { key ->
                    (key as? NavKey)?.let { navigator.push(it) }
                },
            )
        }
        entry<MembersKey> {
            MembersScreen(onBack = { navigator.pop() })
        }
        // Screen id: onboarding
        entry<OnboardingKey> {
            OnboardingScreen(
                onFinish = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds
    @IntoSet
    abstract fun bindHomeEntries(entries: HomeEntries): FeatureEntries
}
