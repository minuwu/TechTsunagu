package com.techtsunagu.wakatta.feature.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Data-only FCM messages. Inactive until the app has a Firebase project and a backend. */
@AndroidEntryPoint
class WakattaMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var handler: PushHandler

    override fun onMessageReceived(message: RemoteMessage) = handler.onMessage(message.data)

    override fun onNewToken(token: String) = handler.onNewToken(token)
}
