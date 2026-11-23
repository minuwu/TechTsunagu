package com.techtsunagu.wakatta.feature.chat

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.model.AudioRecorder
import com.techtsunagu.wakatta.core.model.OutboxWriter
import com.techtsunagu.wakatta.core.model.Speaker
import com.techtsunagu.wakatta.core.model.ThreadSource
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.ChatsKey
import com.techtsunagu.wakatta.core.navigation.PChatKey
import com.techtsunagu.wakatta.core.navigation.FeatureEntries
import com.techtsunagu.wakatta.core.navigation.Navigator
import com.techtsunagu.wakatta.feature.chat.data.FakeAudioRecorder
import com.techtsunagu.wakatta.feature.chat.data.FakeOutboxWriter
import com.techtsunagu.wakatta.feature.chat.data.FakeSpeaker
import com.techtsunagu.wakatta.feature.chat.data.WorkThreadSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Personal threads (Talk `tt_`, Contact `tc_`, see `PersonalThreadIds` in :feature:connect) open the personal chat.
 * Chat cannot import another feature, so the two prefixes are repeated here.
 */
internal fun threadKey(threadId: String): NavKey =
    if (threadId.startsWith("tt_") || threadId.startsWith("tc_")) PChatKey(threadId) else ChatKey(threadId)

@Singleton
class ChatEntries @Inject constructor() : FeatureEntries {
    override fun EntryProviderScope<NavKey>.install(navigator: Navigator) {
        // Screen id: chats
        entry<ChatsKey> {
            ChatsScreen(
                onThreadClick = { threadId -> navigator.push(threadKey(threadId)) },
            )
        }
        // Screen id: chat
        entry<ChatKey> { key ->
            ChatScreen(
                threadId = key.threadId,
                onBackClick = { navigator.pop() },
            )
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {
    @Binds
    @IntoSet
    abstract fun bindChatEntries(entries: ChatEntries): FeatureEntries

    @Binds
    @IntoSet
    abstract fun bindThreadSource(source: WorkThreadSource): ThreadSource

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(fake: FakeAudioRecorder): AudioRecorder

    @Binds
    @Singleton
    abstract fun bindSpeaker(fake: FakeSpeaker): Speaker

    @Binds
    @Singleton
    abstract fun bindOutboxWriter(fake: FakeOutboxWriter): OutboxWriter
}
