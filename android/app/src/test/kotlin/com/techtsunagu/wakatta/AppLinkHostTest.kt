package com.techtsunagu.wakatta

import com.techtsunagu.wakatta.core.network.ApiConfig
import java.io.File
import java.net.URI
import org.junit.Assert.assertTrue
import org.junit.Test

/** The App Links filter and the matcher in MainActivity must name the same host (core/deeplink/README.md). */
class AppLinkHostTest {

    @Test fun manifestFilterUsesTheApiHost() {
        val manifest = File("src/main/AndroidManifest.xml").readText(Charsets.UTF_8)
        val host = URI(ApiConfig.BASE_URL).host
        assertTrue("App Links host in AndroidManifest.xml is not $host", "android:host=\"$host\"" in manifest)
    }
}
