package com.techtsunagu.wakatta.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutboxDecisionTest {

    @Test
    fun test2xxIsDone() {
        assertEquals(OutboxDecision.Done, decide(200))
        assertEquals(OutboxDecision.Done, decide(201))
        assertEquals(OutboxDecision.Done, decide(204))
    }

    @Test
    fun test401IsPauseSession() {
        assertEquals(OutboxDecision.PauseSession, decide(401))
    }

    @Test
    fun test404And409AreDrop() {
        assertEquals(OutboxDecision.Drop(404), decide(404))
        assertEquals(OutboxDecision.Drop(409), decide(409))
    }

    @Test
    fun test429And503AreRetryLater() {
        val d429 = decide(429, 60L)
        assertTrue(d429 is OutboxDecision.RetryLater)
        assertEquals(60L, (d429 as OutboxDecision.RetryLater).retryAfterSec)

        val d503 = decide(503, 15L)
        assertTrue(d503 is OutboxDecision.RetryLater)
        assertEquals(15L, (d503 as OutboxDecision.RetryLater).retryAfterSec)

        val dDefault = decide(429, null)
        assertTrue(dDefault is OutboxDecision.RetryLater)
        assertEquals(null, (dDefault as OutboxDecision.RetryLater).retryAfterSec)
    }

    @Test
    fun test5xxIsBackoff() {
        assertEquals(OutboxDecision.Backoff, decide(500))
        assertEquals(OutboxDecision.Backoff, decide(502))
        assertEquals(OutboxDecision.Backoff, decide(504))
    }

    @Test
    fun testOther4xxIsDrop() {
        assertEquals(OutboxDecision.Drop(400), decide(400))
        assertEquals(OutboxDecision.Drop(422), decide(422))
    }
}
