package com.techtsunagu.wakatta.feature.connect

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.navigation.ConnectKey
import com.techtsunagu.wakatta.core.navigation.ConnectScanKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.core.navigation.PChatKey
import com.techtsunagu.wakatta.feature.connect.data.FakeConnectRepository
import com.techtsunagu.wakatta.feature.connect.domain.ContactRepository
import com.techtsunagu.wakatta.feature.connect.domain.PersonalThreadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: connect
        entry<ConnectKey> { key ->
            ConnectScreen(
                openNewTalk = key.newTalk,
                onScanClick = { navigator.push(ConnectScanKey()) },
                onOpenThread = { navigator.push(PChatKey(it)) },
            )
        }
        // Screen id: connect-scan; the scan screen is replaced by the chat it opens
        entry<ConnectScanKey> { key ->
            ConnectScanScreen(
                linkToken = key.token,
                onBackClick = { navigator.pop() },
                onOpenThread = { navigator.replaceTop(PChatKey(it)) },
            )
        }
        // Screen id: pchat (Contact Thread or Talk Thread)
        entry<PChatKey> { key ->
            PChatScreen(
                threadId = key.threadId,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectModule {
    @Binds
    @IntoSet
    abstract fun bindConnectEntries(entries: ConnectEntries): FeatureEntries
}
