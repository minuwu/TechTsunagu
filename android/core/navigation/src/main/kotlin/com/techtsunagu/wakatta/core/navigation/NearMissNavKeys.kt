package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class NearMissNewKey(val machineId: String? = null) : NavKey

@Serializable
data object NearMissListKey : NavKey

@Serializable
data class NearMissDetailKey(val id: String) : NavKey
