package com.techtsunagu.wakatta.feature.notices

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.navigation.AskKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.LifeKey
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.NoticeKey
import com.techtsunagu.wakatta.core.navigation.NoticeNewKey
import com.techtsunagu.wakatta.core.navigation.SenpaiKey
import com.techtsunagu.wakatta.core.navigation.SupportLogKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoticesEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: life
        entry<LifeKey> {
            LifeScreen(
                onNoticeClick = { id -> navigator.push(NoticeKey(id)) },
                onNewNoticeClick = { navigator.push(NoticeNewKey) },
                onAskClick = { navigator.push(AskKey) },
                onSenpaiClick = { navigator.push(SenpaiKey) },
                onSupportLogClick = { navigator.push(SupportLogKey()) },
            )
        }
        // Screen id: notice
        entry<NoticeKey> { key ->
            NoticeScreen(
                id = key.id,
                onBackClick = { navigator.pop() },
                onAskClick = { navigator.push(AskKey) },
            )
        }
        // Screen id: notice-new
        entry<NoticeNewKey> {
            NoticeNewScreen(
                onBackClick = { navigator.pop() },
                onPublished = { id ->
                    navigator.pop()
                    navigator.push(NoticeKey(id))
                },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoticesModule {
    @Binds
    @IntoSet
    abstract fun bindNoticesEntries(entries: NoticesEntries): FeatureEntries
}
