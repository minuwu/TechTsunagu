package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Shift
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeShiftAssignmentRepositoryTest {

    private lateinit var repository: FakeShiftAssignmentRepository

    @Before
    fun setUp() {
        repository = FakeShiftAssignmentRepository(delayMs = 0L)
    }

    @Test
    fun getAssignmentsReturnsInitialList() = runTest {
        val result = repository.getAssignments("2026-11-23")
        assertTrue(result.isSuccess)
        val list = (result as AppResult.Success).value
        assertEquals(3, list.size)
        val mc1 = list.find { it.machineId == "mc1" }
        assertNotNull(mc1)
        assertEquals("u_rahim", mc1?.workerId)
    }

    @Test
    fun assignUpdatesWorkerForMachine() = runTest {
        val result = repository.assign(
            machineId = "mc1",
            shift = Shift.DAY,
            date = "2026-11-23",
            workerId = "u_nguyen"
        )
        assertTrue(result.isSuccess)
        val updated = (result as AppResult.Success).value
        assertNotNull(updated)
        assertEquals("u_nguyen", updated?.workerId)
        assertEquals("Nguyen Van An", updated?.workerName)
    }

    @Test
    fun assignWithNullWorkerClearsAssignment() = runTest {
        val result = repository.assign(
            machineId = "mc1",
            shift = Shift.DAY,
            date = "2026-11-23",
            workerId = null
        )
        assertTrue(result.isSuccess)
        assertNull((result as AppResult.Success).value)

        val list = (repository.getAssignments() as AppResult.Success).value
        assertNull(list.find { it.machineId == "mc1" })
    }
}
