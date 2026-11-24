package com.techtsunagu.wakatta

import android.app.Application
import com.techtsunagu.wakatta.feature.notifications.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WakattaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Android shows a push itself while the app is closed: its channel must already exist.
        NotificationChannels.createAll(this)
    }
}
