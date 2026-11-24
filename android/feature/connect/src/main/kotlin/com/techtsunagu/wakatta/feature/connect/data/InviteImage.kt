package com.techtsunagu.wakatta.feature.connect.data

import android.content.Context
import com.techtsunagu.wakatta.core.media.QrCard
import com.techtsunagu.wakatta.core.media.QrPng
import com.techtsunagu.wakatta.core.network.ApiConfig
import java.io.File

/** What My QR holds: the App Link a phone camera opens straight in the app (the API host shows a download page otherwise). */
fun inviteLink(token: String): String = "${ApiConfig.BASE_URL}/c/$token"

/** The line sent with the QR image: chat apps make the link tappable, and it opens the app. */
fun shareText(link: String, code: String): String =
    "Chat with me on Wakatta? (each of us reads in our own language). Tap to open: $link or enter my code $code\n" +
        "Wakatta?でチャットしましょう（翻訳つき）。タップして開くか、コード $code を入力: $link"

/** The share-sheet image: the captioned card on a device, the bare QR in JVM tests (no Android graphics there). */
internal fun writeInviteImage(context: Context?, token: String, name: String?, dir: File): File {
    val file = File(dir, "wakatta-my-qr.png")
    return if (context != null) QrCard.write(inviteLink(token), file, name, code = token) else QrPng.write(inviteLink(token), file)
}
