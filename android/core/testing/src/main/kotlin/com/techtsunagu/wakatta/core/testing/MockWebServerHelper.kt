package com.techtsunagu.wakatta.core.testing

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer

object MockWebServerHelper {
    /**
     * Builds standard API envelope JSON string: {"success": true/false, "data": ..., "message": ...}
     */
    fun apiEnvelopeJson(
        dataJson: String = "{}",
        success: Boolean = true,
        message: String? = null
    ): String {
        val msgStr = if (message != null) "\"$message\"" else "null"
        return """{"success":$success,"data":$dataJson,"message":$msgStr}"""
    }

    /**
     * Creates a MockResponse with API envelope payload.
     */
    fun envelopeResponse(
        dataJson: String = "{}",
        success: Boolean = true,
        message: String? = null,
        code: Int = 200
    ): MockResponse {
        val bodyText = apiEnvelopeJson(dataJson = dataJson, success = success, message = message)
        return MockResponse.Builder()
            .code(code)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .body(bodyText)
            .build()
    }
}

/**
 * Extension on MockWebServer to enqueue an envelope response.
 */
fun MockWebServer.enqueueEnvelope(
    dataJson: String = "{}",
    success: Boolean = true,
    message: String? = null,
    code: Int = 200
) {
    this.enqueue(MockWebServerHelper.envelopeResponse(dataJson, success, message, code))
}
