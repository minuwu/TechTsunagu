package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AnalyticsKey : NavKey

@Serializable
data object SkillsKey : NavKey

@Serializable
data object FollowUpsKey : NavKey

@Serializable
data class LearningKey(val userId: String? = null) : NavKey
