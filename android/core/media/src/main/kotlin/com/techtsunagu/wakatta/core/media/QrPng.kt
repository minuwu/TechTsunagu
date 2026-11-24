package com.techtsunagu.wakatta.core.media

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.DeflaterOutputStream

/** Writes a QR as a black-on-white PNG anyone can scan from a chat app. Plain JVM, so it also runs in unit tests. */
object QrPng {
    fun write(content: String, file: File, scale: Int = 12): File {
        val m = ZxingQrEncoder().encode(content, 1, 1) // zxing keeps its 4-module quiet zone
        val side = m.width * scale
        val raw = ByteArrayOutputStream()
        DeflaterOutputStream(raw).use { z ->
            for (y in 0 until side) {
                z.write(0) // filter: none
                for (x in 0 until side) z.write(if (m.get(x / scale, y / scale)) 0 else 255)
            }
        }
        DataOutputStream(file.outputStream().buffered()).use { out ->
            out.write(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 13, 10, 26, 10))
            val ihdr = ByteArrayOutputStream().also { DataOutputStream(it).apply { writeInt(side); writeInt(side); write(byteArrayOf(8, 0, 0, 0, 0)) } }
            chunk(out, "IHDR", ihdr.toByteArray())
            chunk(out, "IDAT", raw.toByteArray())
            chunk(out, "IEND", ByteArray(0))
        }
        return file
    }

    private fun chunk(out: DataOutputStream, type: String, data: ByteArray) {
        val t = type.toByteArray(Charsets.US_ASCII)
        out.writeInt(data.size)
        out.write(t)
        out.write(data)
        out.writeInt(CRC32().apply { update(t); update(data) }.value.toInt())
    }
}
