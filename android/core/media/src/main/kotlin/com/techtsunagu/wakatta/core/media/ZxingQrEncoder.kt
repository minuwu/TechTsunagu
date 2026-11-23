package com.techtsunagu.wakatta.core.media

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.techtsunagu.wakatta.core.model.QrEncoder
import javax.inject.Inject
import javax.inject.Singleton

data class QrMatrix(
    val width: Int,
    val height: Int,
    val data: BooleanArray
) {
    fun get(x: Int, y: Int): Boolean {
        require(x in 0 until width && y in 0 until height) { "Out of bounds ($x, $y) for ${width}x${height}" }
        return data[y * width + x]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QrMatrix) return false
        return width == other.width && height == other.height && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@Singleton
class ZxingQrEncoder @Inject constructor() : QrEncoder {
    override fun encode(content: String, width: Int, height: Int): QrMatrix {
        require(content.isNotEmpty()) { "Content cannot be empty" }
        require(width > 0 && height > 0) { "Dimensions must be positive" }
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val data = BooleanArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                data[y * w + x] = bitMatrix.get(x, y)
            }
        }
        return QrMatrix(width = w, height = h, data = data)
    }
}
