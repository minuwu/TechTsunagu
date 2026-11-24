package com.techtsunagu.wakatta.feature.asks

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.AskKey
import com.techtsunagu.wakatta.core.navigation.ChatbotKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.NoticeKey
import com.techtsunagu.wakatta.core.navigation.SenpaiKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsksEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: ask
        entry<AskKey> {
            AskScreen(
                onBackClick = { navigator.pop() },
                onNavigateToNotice = { noticeId -> navigator.push(NoticeKey(noticeId)) },
            )
        }
        // Screen id: chatbot (XREQ-016)
        entry<ChatbotKey> { key ->
            ChatbotScreen(machineId = key.machineId, onBackClick = { navigator.pop() })
        }
        // Screen id: senpai
        entry<SenpaiKey> {
            SenpaiScreen(
                onBackClick = { navigator.pop() },
                onNavigateToAsk = { navigator.push(AskKey) },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AsksModule {
    @Binds
    @IntoSet
    abstract fun bindAsksEntries(entries: AsksEntries): FeatureEntries
}
