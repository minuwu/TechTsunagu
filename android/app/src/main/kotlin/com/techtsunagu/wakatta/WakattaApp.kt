package com.techtsunagu.wakatta

import android.app.Application
import android.content.Intent
import android.os.SystemClock
import com.techtsunagu.wakatta.core.common.CrashGuard
import kotlin.system.exitProcess
import com.techtsunagu.wakatta.feature.notifications.DeviceRegistrar
import com.techtsunagu.wakatta.feature.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WakattaApp : Application() {
    // Built at start so it watches sign-in and registers this phone for push (PUT /api/devices/me).
    @Inject lateinit var deviceRegistrar: DeviceRegistrar

    override fun onCreate() {
        super.onCreate()
        // Android shows a push itself while the app is closed: its channel must already exist.
        NotificationChannels.createAll(this)
        CrashGuard.rethrow = BuildConfig.DEBUG
        if (!BuildConfig.DEBUG) restartOnCrash()
    }

    /**
     * Release only: a crash the screens could not catch reopens the app on Home without a dialog.
     * A second crash within [LOOP_MS] of starting falls back to the system so it never loops.
     */
    private fun restartOnCrash() {
        val system = Thread.getDefaultUncaughtExceptionHandler()
        val prefs = getSharedPreferences("crash_guard", MODE_PRIVATE)
        val startedAt = System.currentTimeMillis()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            val looping = startedAt - prefs.getLong("restarted_at", 0) < LOOP_MS &&
                System.currentTimeMillis() - startedAt < LOOP_MS
            if (looping) {
                system?.uncaughtException(thread, error)
                return@setDefaultUncaughtExceptionHandler
            }
            prefs.edit().putLong("restarted_at", System.currentTimeMillis()).commit()
            // the app is still in front, so it may start its own activity; a fresh process takes it
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            )
            exitProcess(EXIT_CRASH)
        }
    }

    private companion object {
        const val LOOP_MS = 10_000L
        const val EXIT_CRASH = 10
    }
}
