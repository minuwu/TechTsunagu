package com.techtsunagu.wakatta.core.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.io.File

/**
 * My QR as a shareable card: the brand, the QR, and what scanning it does, so a QR sent in another chat app
 * tells its own story. Needs Android graphics; [QrPng] is the plain-JVM fallback.
 */
object QrCard {
    private const val W = 720
    private const val H = 1040
    private const val QR = 520
    private const val BRAND = 0xFF1E6FB8.toInt()

    fun write(content: String, file: File, name: String?): File {
        val m = ZxingQrEncoder().encode(content, 1, 1)
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

        text.color = BRAND
        text.textSize = 56f
        text.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        c.drawText("Wakatta?", W / 2f, 96f, text)

        val left = (W - QR) / 2f
        val top = 130f
        val cell = QR.toFloat() / m.width
        val dark = Paint().apply { color = Color.BLACK }
        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                if (m.get(x, y)) c.drawRect(left + x * cell, top + y * cell, left + (x + 1) * cell, top + (y + 1) * cell, dark)
            }
        }
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = 0x331E6FB8
        }
        c.drawRoundRect(RectF(left - 8, top - 8, left + QR + 8, top + QR + 8), 24f, 24f, frame)

        val who = name?.trim().orEmpty()
        text.color = 0xFF1B1B1F.toInt()
        text.textSize = 34f
        c.drawText(if (who.isEmpty()) "Scan to chat with me" else "Scan to chat with $who", W / 2f, 730f, text)
        text.typeface = Typeface.DEFAULT
        text.textSize = 30f
        text.color = 0xFF44474E.toInt()
        c.drawText("読み取ると、翻訳つきチャットでつながります", W / 2f, 784f, text)
        c.drawText("Point your phone camera here. Each of you", W / 2f, 850f, text)
        c.drawText("reads the other in your own language.", W / 2f, 892f, text)
        text.textSize = 24f
        text.color = 0xFF74777F.toInt()
        c.drawText("Only your name and language are shared.", W / 2f, 980f, text)

        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        return file
    }
}
