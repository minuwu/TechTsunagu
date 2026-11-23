package com.techtsunagu.wakatta.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrPayloadTest {

    @Test
    fun parse_validMachineSticker_returnsMachineSticker() {
        val raw = "https://wakatta.app/m/lathe-1?s=shopA"
        val payload = QrPayload.parse(raw)
        assertNotNull(payload)
        assertTrue(payload is QrPayload.MachineSticker)
        val sticker = payload as QrPayload.MachineSticker
        assertEquals("wakatta.app", sticker.host)
        assertEquals("lathe-1", sticker.machineId)
        assertEquals("shopA", sticker.shopCode)
    }

    @Test
    fun parse_validMachineStickerWithPort_returnsMachineSticker() {
        val raw = "https://localhost:8080/m/press-2?s=factory1"
        val payload = QrPayload.parse(raw)
        assertNotNull(payload)
        assertTrue(payload is QrPayload.MachineSticker)
        val sticker = payload as QrPayload.MachineSticker
        assertEquals("localhost:8080", sticker.host)
        assertEquals("press-2", sticker.machineId)
        assertEquals("factory1", sticker.shopCode)
    }

    @Test
    fun parse_validMyQr_returnsMyQr() {
        val raw = "wk:tok_abc123_xyz"
        val payload = QrPayload.parse(raw)
        assertNotNull(payload)
        assertTrue(payload is QrPayload.MyQr)
        val myQr = payload as QrPayload.MyQr
        assertEquals("tok_abc123_xyz", myQr.token)
    }

    @Test
    fun parse_foreignUrl_returnsNull() {
        assertNull(QrPayload.parse("https://google.com/search?q=test"))
        assertNull(QrPayload.parse("https://wakatta.app/other/path"))
        assertNull(QrPayload.parse("http://insecure.com/m/1?s=shop"))
    }

    @Test
    fun parse_machineStickerWithExtraParamsOrUserId_returnsNull() {
        // "no user id ever"
        assertNull(QrPayload.parse("https://wakatta.app/m/mc1?s=shopA&user=u_123"))
        assertNull(QrPayload.parse("https://wakatta.app/m/mc1?s=shopA&userId=u_123"))
    }

    @Test
    fun parse_emptyOrInvalidCodes_returnsNull() {
        assertNull(QrPayload.parse(""))
        assertNull(QrPayload.parse("   "))
        assertNull(QrPayload.parse("wk:"))
        assertNull(QrPayload.parse("wk:has space"))
        assertNull(QrPayload.parse("random_string_12345"))
        assertNull(QrPayload.parse("user:u_rahim"))
    }
}
