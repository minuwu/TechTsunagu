package com.techtsunagu.wakatta.feature.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.common.LanguageManagerImpl
import com.techtsunagu.wakatta.core.model.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class PushHandlerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager get() = context.getSystemService(NotificationManager::class.java)
    private val ok = mapOf("v" to "1", "type" to "contact_message", "eventId" to "e1", "senderId" to "u7")

    private fun testSettings(enabled: Boolean = true): SettingsStore = object : SettingsStore {
        override val isDarkMode: StateFlow<Boolean?> = MutableStateFlow(null)
        override val isLargeText: StateFlow<Boolean> = MutableStateFlow(false)
        override val isOfflineHintEnabled: StateFlow<Boolean> = MutableStateFlow(true)
        override val isNotificationEnabled: StateFlow<Boolean> = MutableStateFlow(enabled)
        override val isOnboardingDone: StateFlow<Boolean> = MutableStateFlow(true)
        override suspend fun setDarkMode(enabled: Boolean?) {}
        override suspend fun setLargeText(enabled: Boolean) {}
        override suspend fun setOfflineHint(enabled: Boolean) {}
        override suspend fun setNotification(enabled: Boolean) {}
        override suspend fun setOnboardingDone(done: Boolean) {}
    }

    private fun handler(
        language: String = "en",
        repo: NotificationsRepository = FakeNotificationsRepository(),
        senders: SenderNames = FakeSenderNames(LanguageManagerImpl(language)),
        settings: SettingsStore = testSettings(true),
    ) = PushHandler(context, LanguageManagerImpl(language), senders, FakeDeviceRegistrar(), repo, settings)

    private fun posted() = shadowOf(manager).allNotifications

    @Before
    fun clear() = manager.cancelAll()

    @Test
    fun unknownSenderShowsGenericLocalizedTitleNeverTanaka() = runTest {
        val repo = FakeNotificationsRepository().apply { clear() }
        val h = handler("en", repo)
        h.handle(ok)
        val n = posted().single()
        assertEquals("New message", n.extras.getCharSequence("android.text").toString())
        assertEquals(NotificationChannels.CONTACTS, n.channelId)

        val items = (repo.load() as com.techtsunagu.wakatta.core.model.AppResult.Success).value
        assertEquals("e1", items.first().id)
        assertEquals("New message", items.first().senderName)
    }

    @Test
    fun knownSenderShowsPersonalizedNotification() = runTest {
        val fakeSenders = FakeSenderNames(LanguageManagerImpl("en"))
        fakeSenders.setSenderName("u7", "Sato")
        val h = handler("en", senders = fakeSenders)
        h.handle(ok)
        val n = posted().single()
        assertEquals("New message from Sato", n.extras.getCharSequence("android.text").toString())
    }

    @Test
    fun senderNameInPayloadIsUsedDirectly() = runTest {
        val h = handler("en")
        h.handle(mapOf("v" to "1", "type" to "contact_message", "eventId" to "e9", "senderName" to "Yamaguchi"))
        val n = posted().single()
        assertEquals("New message from Yamaguchi", n.extras.getCharSequence("android.text").toString())
    }

    @Test
    fun unknownSenderFollowsJapaneseLanguage() = runTest {
        handler("ja").handle(ok)
        assertEquals("新しいメッセージ", posted().single().extras.getCharSequence("android.text").toString())
    }

    @Test
    fun unknownSenderFollowsBengaliLanguage() = runTest {
        handler("bn").handle(ok)
        assertEquals("নতুন বার্তা", posted().single().extras.getCharSequence("android.text").toString())
    }

    @Test
    fun notificationSwitchDisabledSavesToRepoButShowsNoSystemNotification() = runTest {
        val repo = FakeNotificationsRepository()
        repo.clear()
        val h = handler("en", repo = repo, settings = testSettings(enabled = false))
        h.handle(ok)
        assertTrue(posted().isEmpty())

        val items = (repo.load() as com.techtsunagu.wakatta.core.model.AppResult.Success).value
        assertEquals(1, items.size)
        assertEquals("e1", items.first().id)
    }

    @Test
    fun talkAnswerAndUnknownPayloadsShowNothing() = runTest {
        val h = handler()
        h.handle(ok + ("type" to "talk_message") + ("eventId" to "e2"))
        h.handle(ok + ("answers" to "1") + ("eventId" to "e3"))
        h.handle(ok + ("check" to "1") + ("eventId" to "e3b"))
        h.handle(ok + ("careNote" to "1") + ("eventId" to "e3c"))
        h.handle(ok + ("v" to "9") + ("eventId" to "e4"))
        assertEquals(0, posted().size)
    }

    @Test
    fun aServerPushShowsItsOwnTextAndOpensItsThreadOrMachine() = runTest {
        val repo = FakeNotificationsRepository().apply { clear() }
        val h = handler(repo = repo)
        val push = mapOf("v" to "1", "sentAt" to "2026-11-24T00:00:00Z")
        h.handle(push + mapOf("type" to "chat_message", "eventId" to "c1", "threadId" to "7", "messageId" to "9"), "Sato sent a message", "Open")
        h.handle(push + mapOf("type" to "check_due", "eventId" to "c2", "machineId" to "3"), "A Check is waiting", "Press 1")

        val chat = posted().single { it.channelId == NotificationChannels.MESSAGES }
        val check = posted().single { it.channelId == NotificationChannels.SAFETY }
        assertEquals("Sato sent a message", chat.extras.getCharSequence("android.title").toString())
        assertEquals("Press 1", check.extras.getCharSequence("android.text").toString())

        val items = (repo.load() as com.techtsunagu.wakatta.core.model.AppResult.Success).value
        assertEquals(listOf("A Check is waiting", "Sato sent a message"), items.take(2).map { it.title })
        val parsed = PushPayloadParser.parse(push + mapOf("type" to "nudge", "eventId" to "c3", "threadId" to "7", "messageId" to "9"))
        assertEquals("7", parsed?.threadId)
    }

    @Test
    fun fiveChannelsAreCreated() {
        NotificationChannels.createAll(context)
        assertEquals(
            setOf("messages", "contacts", "safety", "life", "updates"),
            manager.notificationChannels.map { it.id }.toSet(),
        )
    }
}
