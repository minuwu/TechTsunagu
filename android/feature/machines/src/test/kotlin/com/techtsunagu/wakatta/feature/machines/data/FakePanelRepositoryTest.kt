package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.PanelState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakePanelRepositoryTest {

    private lateinit var repository: FakePanelRepository

    @Before
    fun setUp() {
        repository = FakePanelRepository(delayMs = 0L)
    }

    @Test
    fun getPanelStateReturnsInitialState() = runTest {
        val result = repository.getPanelState("mc2")
        assertTrue(result.isSuccess)
        val panelResult = (result as AppResult.Success).value
        assertEquals("mc2", panelResult.panel.machineId)
        assertEquals(PanelState.LOCKED, panelResult.panel.state)
        assertFalse(panelResult.isNotModified)
        assertNotNull(panelResult.etag)
    }

    @Test
    fun getPanelStateWithMatchingEtagReturnsNotModified() = runTest {
        val first = repository.getPanelState("mc1")
        val etag = (first as AppResult.Success).value.etag

        val second = repository.getPanelState("mc1", ifNoneMatch = etag)
        assertTrue(second.isSuccess)
        val secondResult = (second as AppResult.Success).value
        assertTrue(secondResult.isNotModified)
        assertEquals(etag, secondResult.etag)
    }

    @Test
    fun requestOverrideValidatesDurationAndReason() = runTest {
        // Reason too short
        val shortReason = repository.requestOverride("mc2", "u_rahim", 60, "no", "k1")
        assertTrue(shortReason.isFailure)

        // Duration too long (> 480 min)
        val longDuration = repository.requestOverride("mc2", "u_rahim", 500, "Valid reason", "k2")
        assertTrue(longDuration.isFailure)

        // Valid override
        val valid = repository.requestOverride("mc2", "u_rahim", 60, "Production urgent", "k3")
        assertTrue(valid.isSuccess)
        val override = (valid as AppResult.Success).value
        assertEquals("Production urgent", override.reason)
        assertEquals(60, override.minutes)

        // Verify state changed to OVERRIDE
        val stateRes = repository.getPanelState("mc2")
        val state = (stateRes as AppResult.Success).value.panel.state
        assertEquals(PanelState.OVERRIDE, state)
    }

    @Test
    fun revokeOverrideRestoresState() = runTest {
        val overrideRes = repository.requestOverride("mc2", null, 60, "Emergency batch", "k4")
        val overrideId = (overrideRes as AppResult.Success).value.id

        val revokeRes = repository.revokeOverride("mc2", overrideId)
        assertTrue(revokeRes.isSuccess)

        val updated = repository.getPanelState("mc2")
        val panel = (updated as AppResult.Success).value.panel
        assertEquals(PanelState.LOCKED, panel.state)
        assertNull(panel.override)
    }

    @Test
    fun recordPresenceUpdatesActiveWorker() = runTest {
        val res = repository.recordPresence("mc2", "u_rahim")
        assertTrue(res.isSuccess)

        val updated = repository.getPanelState("mc2")
        val panel = (updated as AppResult.Success).value.panel
        assertEquals("u_rahim", panel.activeWorkerId)
        assertEquals("Rahim Uddin", panel.activeWorkerName)
    }

    @Test
    fun requestOverrideWithTheSameKeyReturnsTheFirstOverride() = runTest {
        val first = repository.requestOverride("mc2", null, 60, "Emergency batch", "same-key")
        val second = repository.requestOverride("mc2", null, 120, "Another reason", "same-key")

        assertEquals((first as AppResult.Success).value, (second as AppResult.Success).value)
    }
}
