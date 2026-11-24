package com.techtsunagu.wakatta.feature.update

sealed interface VersionGateResult {
    data object UpToDate : VersionGateResult
    data object Soft : VersionGateResult
    data object Hard : VersionGateResult
}

object VersionGate {
    fun evaluate(
        versionCode: Long,
        minSupportedVersionCode: Long,
        latestVersionCode: Long,
    ): VersionGateResult = when {
        versionCode < minSupportedVersionCode -> VersionGateResult.Hard
        versionCode < latestVersionCode -> VersionGateResult.Soft
        else -> VersionGateResult.UpToDate
    }

    /** Same rule on `MAJOR.MINOR.PATCH` names (the `/app/config` source). */
    fun evaluate(current: String, minSupported: String, latest: String): VersionGateResult = when {
        compareVersions(current, minSupported) < 0 -> VersionGateResult.Hard
        compareVersions(current, latest) < 0 -> VersionGateResult.Soft
        else -> VersionGateResult.UpToDate
    }

    /**
     * Compare both if available, prioritizing versionCode if provided.
     */
    fun evaluate(
        currentVersionName: String,
        minSupportedVersionName: String,
        latestVersionName: String,
        currentVersionCode: Long? = null,
        minSupportedVersionCode: Long? = null,
        latestVersionCode: Long? = null,
    ): VersionGateResult {
        if (currentVersionCode != null && minSupportedVersionCode != null && latestVersionCode != null) {
            return evaluate(currentVersionCode, minSupportedVersionCode, latestVersionCode)
        }
        return evaluate(currentVersionName, minSupportedVersionName, latestVersionName)
    }

    /** Numeric per part (`1.10.0` > `1.9.0`); a suffix such as `-debug` and missing parts count as 0. */
    fun compareVersions(a: String, b: String): Int {
        val pa = parts(a)
        val pb = parts(b)
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val c = pa.getOrElse(i) { 0 }.compareTo(pb.getOrElse(i) { 0 })
            if (c != 0) return c
        }
        return 0
    }

    private fun parts(v: String): List<Int> =
        v.substringBefore('-').split('.').map { it.trim().toIntOrNull() ?: 0 }
}
