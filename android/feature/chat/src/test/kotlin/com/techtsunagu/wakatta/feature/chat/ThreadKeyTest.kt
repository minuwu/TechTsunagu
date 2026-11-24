package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.PChatKey
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadKeyTest {
    @Test
    fun personalThreadsOpenThePersonalChatAndWorkThreadsTheWorkChat() {
        assertEquals(PChatKey("tt_3"), threadKey("tt_3"))
        assertEquals(PChatKey("tc_9"), threadKey("tc_9"))
        assertEquals(ChatKey("7"), threadKey("7"))
    }
}
