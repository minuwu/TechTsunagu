package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LifeKey : NavKey

@Serializable
data class NoticeKey(val id: String) : NavKey

@Serializable
data object NoticeNewKey : NavKey
