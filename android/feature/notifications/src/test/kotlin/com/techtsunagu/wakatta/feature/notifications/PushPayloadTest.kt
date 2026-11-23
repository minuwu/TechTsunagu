package com.techtsunagu.wakatta.feature.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PushPayloadTest {
    private val ok = mapOf("v" to "1", "type" to "contact_message", "eventId" to "e1", "senderId" to "u7")

    @Test
    fun idsOnlyPayloadIsAccepted() {
        val p = PushPayloadParser.parse(ok)!!
        assertEquals(ThreadKind.CONTACT, p.threadKind)
        assertEquals("u7", p.senderId)
        assertEquals(ThreadKind.TALK, PushPayloadParser.parse(ok + ("type" to "talk_message"))!!.threadKind)
        assertEquals(ThreadKind.UNKNOWN, PushPayloadParser.parse(ok + ("type" to "notice_new"))!!.threadKind)
    }

    @Test
    fun senderNameInPayloadIsParsed() {
        val p = PushPayloadParser.parse(ok + ("senderName" to "Tanaka"))!!
        assertEquals("Tanaka", p.senderName)
    }

    @Test
    fun answerAndCareAndCheckFieldsRejectThePayload() {
        for (key in listOf("answers", "correctIndex", "careNote", "health", "painLevel", "appetite", "body", "text", "message", "transcript", "check", "checkAnswer", "checkQuestion", "confusion")) {
            assertNull(key, PushPayloadParser.parse(ok + (key to "x")))
        }
    }

    @Test
    fun unknownVersionAndMissingIdsAreIgnored() {
        assertNull(PushPayloadParser.parse(ok + ("v" to "2")))
        assertNull(PushPayloadParser.parse(ok - "v"))
        assertNull(PushPayloadParser.parse(ok - "eventId"))
        assertNull(PushPayloadParser.parse(ok - "type"))
        assertNotNull(PushPayloadParser.parse(ok + ("link" to "https://h/t/7")))
    }

    @Test
    fun recentEventIdsRemembersTheLastHundred() {
        val ids = RecentEventIds(max = 100)
        assertTrue(ids.add("a"))
        assertFalse(ids.add("a"))
        for (i in 1..100) ids.add("x$i")
        assertTrue(ids.add("a"))
    }
}
