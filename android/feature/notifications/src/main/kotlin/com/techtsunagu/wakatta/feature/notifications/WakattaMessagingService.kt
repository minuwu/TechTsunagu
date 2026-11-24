package com.techtsunagu.wakatta.feature.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** FCM messages that arrive while the app is open; Android shows the others itself. Inactive until the app has a Firebase project. */
@AndroidEntryPoint
class WakattaMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var handler: PushHandler

    override fun onMessageReceived(message: RemoteMessage) =
        handler.onMessage(message.data, message.notification?.title, message.notification?.body)

    override fun onNewToken(token: String) = handler.onNewToken(token)
}
