package com.techtsunagu.wakatta.feature.update

import org.junit.Assert.assertEquals
import org.junit.Test

class VersionGateTest {

    data class TestCase(
        val current: Long,
        val minSupported: Long,
        val latest: Long,
        val expected: VersionGateResult,
    )

    private val testTable = listOf(
        // Below minimum supported -> Hard upgrade required
        TestCase(current = 1, minSupported = 5, latest = 10, expected = VersionGateResult.Hard),
        TestCase(current = 4, minSupported = 5, latest = 10, expected = VersionGateResult.Hard),
        // At or above minimum, but below latest -> Soft upgrade recommended
        TestCase(current = 5, minSupported = 5, latest = 10, expected = VersionGateResult.Soft),
        TestCase(current = 9, minSupported = 5, latest = 10, expected = VersionGateResult.Soft),
        // At or above latest -> Up to date
        TestCase(current = 10, minSupported = 5, latest = 10, expected = VersionGateResult.UpToDate),
        TestCase(current = 12, minSupported = 5, latest = 10, expected = VersionGateResult.UpToDate),
    )

    @Test
    fun versionGateTableVerification() {
        for (case in testTable) {
            val result = VersionGate.evaluate(
                versionCode = case.current,
                minSupportedVersionCode = case.minSupported,
                latestVersionCode = case.latest,
            )
            assertEquals(
                "Failed for current=${case.current}, min=${case.minSupported}, latest=${case.latest}",
                case.expected,
                result,
            )
        }
    }

    @Test
    fun upToDateWhenVersionCodesMatch() {
        val result = VersionGate.evaluate(
            versionCode = 10L,
            minSupportedVersionCode = 5L,
            latestVersionCode = 10L,
        )
        assertEquals(VersionGateResult.UpToDate, result)
    }

    @Test
    fun upToDateWhenVersionNamesMatch() {
        val result = VersionGate.evaluate(
            current = "1.4.0",
            minSupported = "1.0.0",
            latest = "1.4.0",
        )
        assertEquals(VersionGateResult.UpToDate, result)
    }

    @Test
    fun versionNameComparisonTable() {
        val table = listOf(
            Triple("1.9.0", "1.10.0", -1),
            Triple("1.10.0", "1.9.0", 1),
            Triple("1.0.0", "1.0.0", 0),
            Triple("1.2.0-debug", "1.2.0", 0),
            Triple("1.2", "1.2.0", 0),
            Triple("2.0.0", "1.99.99", 1),
        )
        for ((a, b, expected) in table) {
            assertEquals("$a vs $b", expected, VersionGate.compareVersions(a, b).coerceIn(-1, 1))
        }
        assertEquals(VersionGateResult.Hard, VersionGate.evaluate("1.1.0", "1.2.0", "1.4.0"))
        assertEquals(VersionGateResult.Soft, VersionGate.evaluate("1.2.0", "1.2.0", "1.4.0"))
        assertEquals(VersionGateResult.UpToDate, VersionGate.evaluate("1.4.0", "1.2.0", "1.4.0"))
        assertEquals(VersionGateResult.UpToDate, VersionGate.evaluate("1.10.0", "1.2.0", "1.9.0"))
    }
}
