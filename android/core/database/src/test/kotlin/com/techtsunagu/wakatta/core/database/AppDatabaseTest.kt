package com.techtsunagu.wakatta.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AppDatabaseTest {

    private lateinit var context: Context
    private val dbName = "wakatta_test.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun rowSurvivesCloseAndReopenWithSameKey() = runTest {
        val db1 = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
        val outboxDao1 = db1.outboxDao()

        val item = OutboxEntity(
            key = "uuid-test-1234",
            kind = "nearmiss",
            payloadJson = "{\"note\":\"test\"}",
            createdAt = 1000L,
            attempts = 1,
            nextAttemptAt = 2000L,
            state = OutboxEntity.STATE_PENDING
        )
        outboxDao1.insert(item)

        db1.close()

        val db2 = Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()
        val outboxDao2 = db2.outboxDao()

        val retrieved = outboxDao2.get("uuid-test-1234")
        assertNotNull(retrieved)
        assertEquals(item.key, retrieved?.key)
        assertEquals(item.kind, retrieved?.kind)
        assertEquals(item.payloadJson, retrieved?.payloadJson)
        assertEquals(item.attempts, retrieved?.attempts)
        assertEquals(item.nextAttemptAt, retrieved?.nextAttemptAt)

        db2.close()
    }

    @Test
    fun nextAttemptAtOrdering() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.outboxDao()

        val item1 = OutboxEntity(key = "k1", kind = "ask", payloadJson = "{}", createdAt = 100L, nextAttemptAt = 500L)
        val item2 = OutboxEntity(key = "k2", kind = "ask", payloadJson = "{}", createdAt = 100L, nextAttemptAt = 200L)
        val item3 = OutboxEntity(key = "k3", kind = "ask", payloadJson = "{}", createdAt = 100L, nextAttemptAt = 800L)

        dao.insert(item1)
        dao.insert(item2)
        dao.insert(item3)

        val ordered = dao.getAllOrderedByNextAttempt()
        assertEquals(3, ordered.size)
        assertEquals("k2", ordered[0].key)
        assertEquals("k1", ordered[1].key)
        assertEquals("k3", ordered[2].key)

        db.close()
    }
}
