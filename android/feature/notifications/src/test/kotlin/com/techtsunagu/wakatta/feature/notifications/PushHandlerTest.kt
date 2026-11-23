package com.techtsunagu.wakatta.feature.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.common.LanguageManagerImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    private fun handler(language: String = "en", repo: NotificationsRepository = FakeNotificationsRepository()) =
        PushHandler(context, LanguageManagerImpl(language), FakeSenderNames(), FakeDeviceRegistrar(), repo)

    private fun posted() = shadowOf(manager).allNotifications

    @Before
    fun clear() = manager.cancelAll()

    @Test
    fun contactMessageShowsOneGenericNotificationOnTheContactsChannel() = runTest {
        val repo = FakeNotificationsRepository()
        val h = handler("en", repo)
        h.handle(ok)
        h.handle(ok) // a repeated event id is ignored
        val n = posted().single()
        assertEquals("New message from Tanaka", n.extras.getCharSequence("android.text").toString())
        assertEquals(NotificationChannels.CONTACTS, n.channelId)

        // Saved to repository
        val items = (repo.load() as com.techtsunagu.wakatta.core.model.AppResult.Success).value
        assertEquals("e1", items.first().id)
        assertEquals("Tanaka", items.first().senderName)
    }

    @Test
    fun senderNameInPayloadIsUsedDirectly() = runTest {
        val h = handler("en")
        h.handle(mapOf("v" to "1", "type" to "contact_message", "eventId" to "e9", "senderName" to "Yamaguchi"))
        val n = posted().single()
        assertEquals("New message from Yamaguchi", n.extras.getCharSequence("android.text").toString())
    }

    @Test
    fun textFollowsTheAccountLanguage() = runTest {
        handler("ja").handle(ok)
        assertEquals("Tanakaさんから新しいメッセージ", posted().single().extras.getCharSequence("android.text").toString())
    }

    @Test
    fun textFollowsBengaliLanguage() = runTest {
        handler("bn").handle(ok)
        assertEquals("Tanaka এর কাছ থেকে নতুন বার্তা", posted().single().extras.getCharSequence("android.text").toString())
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
    fun fiveChannelsAreCreated() {
        NotificationChannels.createAll(context)
        assertEquals(
            setOf("messages", "contacts", "safety", "life", "updates"),
            manager.notificationChannels.map { it.id }.toSet(),
        )
    }
}
