package com.techtsunagu.wakatta.core.network

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMappingTest {

    /** Serves one error and returns what `safeApiCall { me() }` makes of it. */
    private fun errorFor(
        status: Int,
        code: String,
        details: String = "{}",
        vararg headers: Pair<String, String>,
    ): AppError = Harness().use { h ->
        h.enqueueError(status, code, details, *headers)
        val result = runBlocking { safeApiCall { h.api.me() } }
        (result as AppResult.Failure).error
    }

    @Test fun badRequestIsUnknown() = assertTrue(errorFor(400, "BAD_REQUEST") is AppError.Unknown)

    @Test fun unauthorizedOutsideLoginIsSessionExpired() =
        assertEquals(AppError.SessionExpired, errorFor(401, "UNAUTHORIZED"))

    @Test fun forbiddenIsForbidden() = assertEquals(AppError.Forbidden, errorFor(403, "FORBIDDEN"))

    @Test fun pinChangeRequired() = assertEquals(AppError.PinChangeRequired, errorFor(403, "PIN_CHANGE_REQUIRED"))

    @Test fun notFound() = assertEquals(AppError.NotFound, errorFor(404, "NOT_FOUND"))
    @Test fun gone() = assertEquals(AppError.Gone, errorFor(410, "QR_REPLACED"))

    @Test fun methodNotAllowedIsUnknown() = assertTrue(errorFor(405, "METHOD_NOT_ALLOWED") is AppError.Unknown)

    @Test fun conflictKeepsCode() = assertEquals(AppError.Conflict("CONFLICT"), errorFor(409, "CONFLICT"))

    @Test fun versionChanged() = assertEquals(AppError.Conflict("VERSION_CHANGED"), errorFor(409, "VERSION_CHANGED"))

    @Test fun preconditionFailed() = assertEquals(AppError.PreconditionFailed, errorFor(412, "VERSION_CONFLICT"))

    @Test fun payloadTooLarge() = assertEquals(AppError.PayloadTooLarge, errorFor(413, "PAYLOAD_TOO_LARGE"))

    @Test fun unsupportedMedia() = assertEquals(AppError.UnsupportedMedia, errorFor(415, "UNSUPPORTED_MEDIA_TYPE"))

    @Test fun validationListsFields() {
        val details = """{"fields":[{"loc":["body","pin"],"msg":"x"},{"loc":["body","userId"],"msg":"y"}]}"""
        assertEquals(AppError.Validation(listOf("pin", "userId")), errorFor(422, "VALIDATION_ERROR", details))
    }

    @Test fun pinLockedCarriesUntil() {
        val details = """{"until":"2026-12-22T10:15:00Z"}"""
        assertEquals(AppError.PinLocked("2026-12-22T10:15:00Z"), errorFor(423, "PIN_LOCKED", details))
    }

    @Test fun upgradeRequired() = assertEquals(AppError.UpgradeRequired, errorFor(426, "UPGRADE_REQUIRED"))

    @Test fun retryAfterIsParsed() =
        assertEquals(AppError.RateLimited(60), errorFor(429, "RATE_LIMITED", "{}", "Retry-After" to "60"))

    @Test fun rateLimitedWithoutHeader() = assertEquals(AppError.RateLimited(null), errorFor(429, "RATE_LIMITED"))

    @Test fun busy() = assertEquals(AppError.Busy, errorFor(503, "BUSY", "{}", "Retry-After" to "2"))

    @Test fun notReadyIsServer() = assertEquals(AppError.Server, errorFor(503, "NOT_READY"))

    @Test fun internalErrorIsServer() = assertEquals(AppError.Server, errorFor(500, "INTERNAL_ERROR"))

    @Test fun nonJsonErrorBodyStillMapsByStatus() = Harness().use { h ->
        h.enqueue(502, "<html>bad gateway</html>")
        val result = runBlocking { safeApiCall { h.api.me() } }
        assertEquals(AppError.Server, (result as AppResult.Failure).error)
    }

    @Test fun unreachableServerIsOffline() {
        val h = Harness()
        h.server.close()
        val result = runBlocking { safeApiCall { h.api.me() } }
        assertEquals(AppError.Offline, (result as AppResult.Failure).error)
    }

    @Test fun silentServerIsTimeout() = Harness(clientTweak = { it.readTimeout(300, java.util.concurrent.TimeUnit.MILLISECONDS) })
        .use { h ->
            h.server.enqueue(MockResponse.Builder().code(200).headersDelay(3, java.util.concurrent.TimeUnit.SECONDS).build())
            val result = runBlocking { safeApiCall { h.api.me() } }
            assertEquals(AppError.Timeout, (result as AppResult.Failure).error)
        }

    @Test fun successUnwrapsData() = Harness().use { h ->
        h.enqueueOk("""{"id":"7","displayName":"A","role":"worker","language":"en","shopId":"1","districtId":null}""")
        val result = runBlocking { safeApiCall { h.api.me() } }
        assertEquals("7", (result as AppResult.Success).value.id)
    }

    @Test fun nullDataForUnitRouteIsSuccess() = Harness().use { h ->
        h.enqueueOk("null")
        val result = runBlocking { safeApiCallUnit { h.api.logout() } }
        assertTrue(result.isSuccess)
    }

    @Test fun unreadableSuccessBodyDoesNotCrash() = Harness().use { h ->
        h.enqueue(200, "not json")
        val result = runBlocking { safeApiCall { h.api.me() } }
        assertTrue((result as AppResult.Failure).error is AppError.Unknown)
    }
}
