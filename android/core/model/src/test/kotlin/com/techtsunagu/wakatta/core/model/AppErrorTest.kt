package com.techtsunagu.wakatta.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppErrorTest {

    @Test
    fun appError_subtypesVerify() {
        val offline: AppError = AppError.Offline
        val timeout: AppError = AppError.Timeout
        val wrongPin: AppError = AppError.WrongPin
        val locked: AppError = AppError.PinLocked("2026-12-22T10:00:00Z")
        val validation: AppError = AppError.Validation(listOf("pin", "userId"))

        assertTrue(offline is AppError.Offline)
        assertTrue(timeout is AppError.Timeout)
        assertTrue(wrongPin is AppError.WrongPin)
        assertEquals("2026-12-22T10:00:00Z", (locked as AppError.PinLocked).until)
        assertEquals(2, (validation as AppError.Validation).fields.size)
    }

    @Test
    fun appResult_successAndFailure() {
        val success: AppResult<String> = AppResult.Success("data")
        val failure: AppResult<String> = AppResult.Failure(AppError.NotFound)

        assertTrue(success.isSuccess)
        assertEquals("data", success.getOrNull())

        assertTrue(failure.isFailure)
        assertEquals(AppError.NotFound, failure.errorOrNull())
    }
}
