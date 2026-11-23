package com.techtsunagu.wakatta.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ZxingQrEncoderTest {

    private val encoder = ZxingQrEncoder()

    @Test
    fun encode_validPayload_returnsQrMatrixWithExpectedDimensions() {
        val matrix = encoder.encode("https://wakatta.app/m/lathe-1?s=shopA", 200, 200)
        assertNotNull(matrix)
        assertEquals(200, matrix.width)
        assertEquals(200, matrix.height)
        assertEquals(200 * 200, matrix.data.size)

        // At least some cells should be true (black modules) and some false (white quiet zone/background)
        assertTrue(matrix.data.any { it })
        assertTrue(matrix.data.any { !it })
    }

    @Test(expected = IllegalArgumentException::class)
    fun encode_emptyString_throwsIllegalArgumentException() {
        encoder.encode("", 200, 200)
    }
}
