package com.techtsunagu.wakatta.feature.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat

/** Receives the session result and opens the system confirmation dialog when it asks for one. */
internal class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status != PackageInstaller.STATUS_PENDING_USER_ACTION) return
        val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java) ?: return
        context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
