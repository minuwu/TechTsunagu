package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ConnectKey : NavKey

/** [token] is set when an App Link `/c/{token}` opened the app: the screen asks to add that person at once. */
@Serializable
data class ConnectScanKey(val token: String? = null) : NavKey

@Serializable
data class PChatKey(val threadId: String) : NavKey
