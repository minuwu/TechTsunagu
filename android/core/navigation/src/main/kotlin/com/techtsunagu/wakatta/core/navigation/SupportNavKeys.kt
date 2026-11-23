package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class SupportLogKey(val workerId: String? = null) : NavKey

@Serializable
data class InterviewKey(val workerId: String? = null) : NavKey
