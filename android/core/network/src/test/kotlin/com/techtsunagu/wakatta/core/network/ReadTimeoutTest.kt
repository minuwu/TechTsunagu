package com.techtsunagu.wakatta.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

/** R1: the app waits longer than the server's AI budget (25 s, G1), so a slow draft is not lost to a timeout. */
class ReadTimeoutTest {

    @Test fun readTimeoutOutlastsTheServersAiBudget() {
        val client = ApiClientFactory.okHttp("1.0", { "en" }, TestSessionStore(), debug = false)
        assertEquals(60_000, client.readTimeoutMillis)
    }
}
