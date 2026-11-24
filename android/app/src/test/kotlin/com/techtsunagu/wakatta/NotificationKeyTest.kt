package com.techtsunagu.wakatta

import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationKeyTest {
    @Test
    fun idsOpenTheChatOrTheMachineAndAnythingElseNothing() {
        assertEquals(ChatKey("12"), notificationKey("12", null))
        assertEquals(ChatKey("12"), notificationKey("12", "3"))
        assertEquals(MachineKey("3"), notificationKey(null, "3"))
        assertNull(notificationKey(null, null))
        assertNull(notificationKey("../x", ""))
    }
}
