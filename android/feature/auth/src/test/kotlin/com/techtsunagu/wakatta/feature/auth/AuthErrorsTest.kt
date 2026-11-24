package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppError
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorsTest {

    @Test
    fun unlockTimeIsShownInTokyoTime() = assertEquals("19:15", formatUnlockTime("2026-12-22T10:15:00Z"))

    @Test
    fun unreadableUnlockTimeIsShownAsIs() = assertEquals("soon", formatUnlockTime("soon"))

    @Test
    fun rateLimitWithoutHeaderFallsBackToSixtySeconds() =
        assertEquals(AuthErrorKind.RATE_LIMITED to "60", AppError.RateLimited(null).toAuthError())

    @Test
    fun offlineAndServerAreSeparate() {
        assertEquals(AuthErrorKind.OFFLINE, AppError.Offline.toAuthError().first)
        assertEquals(AuthErrorKind.SERVER, AppError.Busy.toAuthError().first)
    }
}
