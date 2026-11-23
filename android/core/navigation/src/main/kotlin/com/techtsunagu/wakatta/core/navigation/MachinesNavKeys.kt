package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MachinesKey : NavKey

@Serializable
data class MachineKey(val machineId: String) : NavKey

@Serializable
data object ScanKey : NavKey

@Serializable
data class PanelKey(val machineId: String) : NavKey

@Serializable
data class NoteKey(val machineId: String) : NavKey
