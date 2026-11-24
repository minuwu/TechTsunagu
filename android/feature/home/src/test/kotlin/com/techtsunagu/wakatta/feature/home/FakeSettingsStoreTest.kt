package com.techtsunagu.wakatta.feature.home.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSettingsStoreTest {

    private lateinit var store: FakeSettingsStore

    @Before
    fun setUp() {
        store = FakeSettingsStore()
    }

    @Test
    fun setDarkModeUpdatesState() = runTest {
        store.setDarkMode(true)
        assertEquals(true, store.isDarkMode.value)

        store.setDarkMode(false)
        assertEquals(false, store.isDarkMode.value)
    }

    @Test
    fun setLargeTextUpdatesState() = runTest {
        assertFalse(store.isLargeText.value)
        store.setLargeText(true)
        assertTrue(store.isLargeText.value)
    }

    @Test
    fun setOfflineHintUpdatesState() = runTest {
        assertFalse(store.isOfflineHintEnabled.value)
        store.setOfflineHint(true)
        assertTrue(store.isOfflineHintEnabled.value)
    }

    @Test
    fun setNotificationUpdatesState() = runTest {
        assertTrue(store.isNotificationEnabled.value)
        store.setNotification(false)
        assertFalse(store.isNotificationEnabled.value)
    }

    @Test
    fun setOnboardingDoneUpdatesState() = runTest {
        assertFalse(store.isOnboardingDone.value)
        store.setOnboardingDone(true)
        assertTrue(store.isOnboardingDone.value)
    }
}
