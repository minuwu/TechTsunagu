package com.techtsunagu.wakatta.feature.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class LocalNotificationsRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repo: LocalNotificationsRepository

    @Before
    fun setUp() {
        context.getSharedPreferences("wakatta_notifications_local", Context.MODE_PRIVATE).edit().clear().commit()
        repo = LocalNotificationsRepository(context)
    }

    @Test
    fun emptyByDefaultThenSavesAndLoads() = runTest {
        val initial = (repo.load() as AppResult.Success).value
        assertTrue(initial.isEmpty())

        repo.save(NotificationItem("id-1", ThreadKind.CONTACT, "Alice", isUnread = true))
        repo.save(NotificationItem("id-2", ThreadKind.CONTACT, "Bob", isUnread = true))

        val loaded = (repo.load() as AppResult.Success).value
        assertEquals(2, loaded.size)
        assertEquals("id-2", loaded[0].id)
        assertEquals("id-1", loaded[1].id)

        // Persistence check with fresh instance
        val repo2 = LocalNotificationsRepository(context)
        val loaded2 = (repo2.load() as AppResult.Success).value
        assertEquals(2, loaded2.size)
    }

    @Test
    fun markAsReadUpdatesItem() = runTest {
        repo.save(NotificationItem("id-1", ThreadKind.CONTACT, "Alice", isUnread = true))
        repo.markAsRead("id-1")

        val loaded = (repo.load() as AppResult.Success).value
        assertFalse(loaded.single().isUnread)
    }

    @Test
    fun clearEmptiesList() = runTest {
        repo.save(NotificationItem("id-1", ThreadKind.CONTACT, "Alice", isUnread = true))
        repo.clear()
        val loaded = (repo.load() as AppResult.Success).value
        assertTrue(loaded.isEmpty())
    }
}
