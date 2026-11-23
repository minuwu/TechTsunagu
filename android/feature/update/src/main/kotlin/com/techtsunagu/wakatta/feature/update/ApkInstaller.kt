package com.techtsunagu.wakatta.feature.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import java.io.File

interface ApkInstaller {
    /** False until the user allows "Install unknown apps" for this app. */
    fun canRequestInstalls(): Boolean

    /** Starts a `PackageInstaller` session; the system then asks the user to confirm. */
    fun install(apk: File)
}

internal class PackageInstallerApkInstaller(private val context: Context) : ApkInstaller {
    override fun canRequestInstalls(): Boolean = context.packageManager.canRequestPackageInstalls()

    override fun install(apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("wakatta.apk", 0, apk.length()).use { out ->
                    input.copyTo(out)
                    session.fsync(out)
                }
            }
            // Mutable on purpose: the system adds the status extras to this explicit intent.
            val intent = Intent(context, InstallResultReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
            session.commit(pending.intentSender)
        }
    }
}

/** Debug only. */
internal class FakeApkInstaller : ApkInstaller {
    override fun canRequestInstalls(): Boolean = true
    override fun install(apk: File) = Unit
}
