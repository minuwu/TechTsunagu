package com.techtsunagu.wakatta.feature.nearmiss

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.feature.nearmiss.data.FakeNearMissRepository
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissCreateRequest
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import com.techtsunagu.wakatta.feature.nearmiss.domain.PhotoMark
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeNearMissRepositoryTest {

    private lateinit var repository: FakeNearMissRepository

    @Before
    fun setUp() {
        repository = FakeNearMissRepository(delayMs = 0L)
    }

    @Test
    fun getNearMissesReturnsSeededList() = runTest {
        val result = repository.getNearMisses()
        assertTrue(result is AppResult.Success)
        val list = (result as AppResult.Success).value
        assertTrue(list.size >= 3)
        assertEquals("nm_s1", list[0].id)
        assertEquals(NearMissSeverity.HIGH, list[0].severity)
        assertEquals(NearMissStatus.OPEN, list[0].status)
    }

    @Test
    fun getNearMissReturnsCorrectItem() = runTest {
        val result = repository.getNearMiss("nm_s2")
        assertTrue(result is AppResult.Success)
        val item = (result as AppResult.Success).value
        assertEquals("nm_s2", item.id)
        assertEquals(NearMissSeverity.LOW, item.severity)
        assertEquals(NearMissStatus.ACK, item.status)
        assertEquals("u_tanaka", item.acknowledgedBy)
    }

    @Test
    fun getNearMissNotFoundReturnsError() = runTest {
        val result = repository.getNearMiss("unknown_id")
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }

    @Test
    fun createNearMissWithCannedTranslation() = runTest {
        val request = NearMissCreateRequest(
            machineId = "mc1",
            text = "A metal chip flew out when I opened the chuck cover.",
            severity = NearMissSeverity.MID,
            photoMark = PhotoMark(50f, 50f),
            idempotencyKey = "key_123",
        )
        val result = repository.createNearMiss(request)
        assertTrue(result is AppResult.Success)
        val created = (result as AppResult.Success).value
        assertEquals("mc1", created.machineId)
        assertEquals(NearMissSeverity.MID, created.severity)
        assertTrue(created.text["ja"]?.contains("チャックのカバーを開けたとき") == true)
        assertEquals(NearMissStatus.OPEN, created.status)
    }

    @Test
    fun createNearMissWithFallbackTranslation() = runTest {
        val request = NearMissCreateRequest(
            machineId = "mc2",
            text = "Custom incident text without canned dictation",
            severity = NearMissSeverity.HIGH,
            idempotencyKey = "key_custom",
        )
        val result = repository.createNearMiss(request)
        assertTrue(result is AppResult.Success)
        val created = (result as AppResult.Success).value
        assertTrue(created.text["ja"]?.startsWith("（自動翻訳）") == true)
        assertEquals("Custom incident text without canned dictation", created.text["en"])
    }

    @Test
    fun acknowledgeMarksReportAsAck() = runTest {
        val result = repository.acknowledge("nm_s1", "u_tanaka")
        assertTrue(result is AppResult.Success)
        val ackResult = (result as AppResult.Success).value
        assertEquals("nm_s1", ackResult.id)
        assertEquals(NearMissStatus.ACK, ackResult.status)
        assertEquals("u_tanaka", ackResult.acknowledgedBy)

        val updated = (repository.getNearMiss("nm_s1") as AppResult.Success).value
        assertEquals(NearMissStatus.ACK, updated.status)
        assertNotNull(updated.acknowledgedAt)
    }

    @Test
    fun acknowledgeNotFoundReturnsError() = runTest {
        val result = repository.acknowledge("non_existent", "u_tanaka")
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
    }

    @Test
    fun linkTaskUpdatesTaskIdAndMarksAck() = runTest {
        val result = repository.linkTask("nm_s3", "task_999")
        assertTrue(result is AppResult.Success)
        val updated = (result as AppResult.Success).value
        assertEquals("task_999", updated.taskId)
        assertEquals(NearMissStatus.ACK, updated.status)
    }

    @Test
    fun simulateErrorReturnsFailure() = runTest {
        repository.simulateError(AppError.Offline)
        val result = repository.getNearMisses()
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Offline, (result as AppResult.Failure).error)
    }
}
