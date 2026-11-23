package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ConnectKey : NavKey

@Serializable
data object ConnectScanKey : NavKey

@Serializable
data class PChatKey(val threadId: String) : NavKey
