package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** [newTalk] opens the New talk dialog once the screen has loaded (the Chats "New talk" chip). */
@Serializable
data class ConnectKey(val newTalk: Boolean = false) : NavKey

/** [token] is set when an App Link `/c/{token}` opened the app: the screen asks to add that person at once. */
@Serializable
data class ConnectScanKey(val token: String? = null) : NavKey

@Serializable
data class PChatKey(val threadId: String) : NavKey
