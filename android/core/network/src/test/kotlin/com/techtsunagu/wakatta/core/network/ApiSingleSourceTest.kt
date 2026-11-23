package com.techtsunagu.wakatta.core.network

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** The host name and every `/api/` path live in ApiConfig.kt only. */
class ApiSingleSourceTest {

    private val androidRoot: File = findUp("module-rules.json").parentFile

    @Test fun noOtherMainSourceHoldsTheHostOrAnApiPath() {
        val offenders = androidRoot.walkTopDown()
            .onEnter { it.name != "build" && it.name != ".gradle" }
            .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
            .filter { "/src/main/" in it.invariantSeparatorsPath && it.name != "ApiConfig.kt" }
            .filter { file ->
                val text = file.readText(Charsets.UTF_8)
                ApiConfig.BASE_URL.substringAfter("://").substringBefore('/') in text ||
                    "\"/api/" in text
            }
            .map { it.relativeTo(androidRoot).invariantSeparatorsPath }
            .toList()
        assertTrue("host or api path outside ApiConfig.kt: $offenders", offenders.isEmpty())
    }

    @Test fun apiConfigStillHoldsThem() {
        val file = androidRoot.walkTopDown().first { it.name == "ApiConfig.kt" && "/src/main/" in it.invariantSeparatorsPath }
        assertTrue("\"/api/" in file.readText(Charsets.UTF_8))
    }
}
