package com.techtsunagu.wakatta.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.crypto.tink.Aead
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.model.User
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class EncryptedSessionStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private class FakeAead : Aead {
        override fun encrypt(plaintext: ByteArray, associatedData: ByteArray): ByteArray {
            val result = ByteArray(plaintext.size + 1)
            result[0] = 0x5A
            for (i in plaintext.indices) {
                result[i + 1] = (plaintext[i].toInt() xor 0xFF).toByte()
            }
            return result
        }

        override fun decrypt(ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
            require(ciphertext.isNotEmpty() && ciphertext[0] == 0x5A.toByte()) { "Invalid header" }
            val result = ByteArray(ciphertext.size - 1)
            for (i in result.indices) {
                result[i] = (ciphertext[i + 1].toInt() xor 0xFF).toByte()
            }
            return result
        }
    }

    @Test
    fun sessionStore_roundTrip_savesAndRestoresSession() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val testFile = File(tempFolder.root, "test_session.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        val fakeAead = FakeAead()
        val store = EncryptedSessionStore(
            dataStore = dataStore,
            aead = fakeAead,
            scope = testScope
        )

        val session = Session(
            token = "secret-token-xyz",
            expiresAt = "2026-12-31T23:59:59Z",
            user = User(
                id = "u1",
                displayName = "Rahim",
                role = Role.WORKER,
                language = "bn",
                shopId = "shop-1",
                districtId = "dist-1"
            ),
            mustChangePin = false
        )

        store.save(session)
        testScheduler.advanceUntilIdle()

        assertEquals("secret-token-xyz", store.getCachedToken())
        assertEquals(session, store.session.value)

        // Clear session
        store.clear()
        testScheduler.advanceUntilIdle()

        assertNull(store.getCachedToken())
        assertNull(store.session.value)
    }
}
