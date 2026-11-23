package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object BriefingNewKey : NavKey

@Serializable
data class BriefingBoardKey(val id: String? = null) : NavKey

@Serializable
data class BriefingCardKey(val id: String? = null) : NavKey
