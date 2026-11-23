package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeCheckRepositoryTest {

    private val signOffRepo = FakeSignOffRepository(delayMs = 0L)
    private val checkRepo = FakeCheckRepository(delayMs = 0L, signOffRepository = signOffRepo)

    @Test
    fun submitCheck_allCorrect_passesAndGrantsSignOffForNote() = runTest {
        val result = checkRepo.submitCheck(
            targetKind = CheckTargetKind.NOTE,
            targetId = "mc1",
            language = "ja",
            answers = listOf(0, 0),
            idempotencyKey = "key_1"
        )
        assertTrue(result is AppResult.Success)
        val checkResult = (result as AppResult.Success).value
        assertTrue(checkResult.passed)
        assertTrue(checkResult.wrongQuestions.isEmpty())
        assertTrue(checkResult.signOffGranted)

        // SignOff should have been granted to worker
        val passportResult = signOffRepo.getPassport("u_rahim")
        assertTrue(passportResult is AppResult.Success)
        val passport = (passportResult as AppResult.Success).value
        assertTrue(passport.signOffs.any { it.machineId == "mc1" })
    }

    @Test
    fun submitCheck_hasWrongAnswer_failsAndListsWrongQuestions() = runTest {
        val result = checkRepo.submitCheck(
            targetKind = CheckTargetKind.NOTE,
            targetId = "mc1",
            language = "ja",
            answers = listOf(0, 1),
            idempotencyKey = "key_2"
        )
        assertTrue(result is AppResult.Success)
        val checkResult = (result as AppResult.Success).value
        assertFalse(checkResult.passed)
        assertEquals(listOf("q_mc1_2"), checkResult.wrongQuestions)
        assertFalse(checkResult.signOffGranted)
    }

    @Test
    fun submitCheck_noticeTarget_doesNotGrantSignOffEvenIfPassed() = runTest {
        val result = checkRepo.submitCheck(
            targetKind = CheckTargetKind.NOTICE,
            targetId = "notif_1",
            language = "ja",
            answers = listOf(0),
            idempotencyKey = "key_3"
        )
        assertTrue(result is AppResult.Success)
        val checkResult = (result as AppResult.Success).value
        assertTrue(checkResult.passed)
        assertFalse(checkResult.signOffGranted)
    }
}
